package net.anotheria.asg.service;

import java.rmi.RemoteException;
import java.util.Arrays;

import net.anotheria.asg.exception.ASGRuntimeException;

abstract public class BaseRemoteServiceStub <T extends RemoteService>{
	
	abstract protected void notifyDelegateFailed();
	
	abstract protected T getDelegate() throws ASGRuntimeException;
	
	/**
	 * Sends echo request to remote service and receives response. Return duration off echo request/response in mills
	 */
	public long ping(int packetSize) throws RemoteException{
		byte[] packet = new byte[packetSize];
		Arrays.fill(packet,(byte)1);
		try{
			long duration = System.currentTimeMillis();
			getDelegate().getEcho(packet);
			duration = System.currentTimeMillis() - duration;
			return duration;
		} catch (RemoteException e){
			notifyDelegateFailed();
			throw e;
		} catch (ASGRuntimeException e){
			throw new RemoteException("Server is unreachable: ",e);
		}
	}
	
}
