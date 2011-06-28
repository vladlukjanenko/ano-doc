package net.anotheria.anodoc.service;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

/**
 * This class contains a reentrant read-write lock and offers operations on it. It is used to synchronize the shutdown of the VM and to allow the threads to finish the writes prior to the shutdown,
 * and to block new writers to keep the data consistent.
 * @author lrosenberg
 *
 */
public class LockHolder {
	private static ReadWriteLock lock = new ReentrantReadWriteLock();
	
	private static Logger log = Logger.getLogger(LockHolder.class);

	private LockHolder() {
	}

	public static void prepareForSave(){
		lock.readLock().lock();
	}
	
	public static void notifySaved(){
		lock.readLock().unlock();
	}

	private static void blockAll() {
		lock.writeLock().lock();
	}
	
	public static final void addShutdownHook(){
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				log.info("Shutdown detecting, blocking writing threads");
				blockAll();
				log.info("Proceeding with shutdown");
			}
		});
	}

}
