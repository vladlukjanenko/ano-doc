package net.anotheria.asg.service.remote;

import java.rmi.RemoteException;

import net.anotheria.asg.exception.ASGRuntimeException;

abstract public class BaseRemoteServiceStub <T extends RemoteService>{
	
	abstract protected void notifyDelegateFailed();
	
	abstract protected T getDelegate() throws ASGRuntimeException;
	
	/**
	 * Sends echo request to remote service and receives response. Return duration off echo request/response in mills
	 */
	public long ping() throws RemoteException{
		return ping(0);
	}
	
	/**
	 * Sends echo request to remote service and receives response. Return duration off echo request/response in mills
	 */
	public long ping(int packetSize) throws RemoteException{
		try{
			EchoRequest req = new EchoRequest();
			req.setEchoDataSize(packetSize);
			req.setRequestTime(System.currentTimeMillis());
			
			EchoResponse res = getDelegate().getEcho(req);
			
			long duration = System.currentTimeMillis() - res.getResponseTime();
			return duration;
		} catch (RemoteException e){
			notifyDelegateFailed();
			throw e;
		} catch (ASGRuntimeException e){
			throw new RemoteException("Server is unreachable: ",e);
		}
	}
	
	
	
}
