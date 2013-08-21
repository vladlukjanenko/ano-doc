package net.anotheria.anodoc.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class contains a reentrant read-write lock and offers operations on it. It is used to synchronize the shutdown of the VM and to allow the threads to finish the writes prior to the shutdown,
 * and to block new writers to keep the data consistent.
 * @author lrosenberg
 *
 */
public class LockHolder {

	/**
	 * {@link Logger} instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(LockHolder.class);

	private static ReadWriteLock lock = new ReentrantReadWriteLock();

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
				LOGGER.info("Shutdown detecting, blocking writing threads");
				blockAll();
				LOGGER.info("Proceeding with shutdown");
			}
		});
	}

}
