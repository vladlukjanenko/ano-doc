package net.anotheria.asg.service.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteService extends Remote{
	
	/**
	 * Converts echo request to echo response and return it. Is useful for for checking remote object availability
	 * @return converted EchoRequest to EchoResponse
	 */
	EchoResponse getEcho(EchoRequest echoRequest) throws RemoteException;
	
}
