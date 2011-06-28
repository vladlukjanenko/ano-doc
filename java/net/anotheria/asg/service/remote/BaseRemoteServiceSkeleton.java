package net.anotheria.asg.service.remote;

import java.rmi.RemoteException;
import java.util.Arrays;


abstract class BaseRemoteServiceSkeleton implements RemoteService{
	
	@Override
	public EchoResponse getEcho(EchoRequest req) throws RemoteException{
		EchoResponse res = new EchoResponse();
		res.setResponseTime(req.getRequestTime());
		
		byte[] data = new byte[req.getEchoDataSize()];
		Arrays.fill(data,(byte)1);
		res.setData(data);
		
		return res;
	}
	
}
