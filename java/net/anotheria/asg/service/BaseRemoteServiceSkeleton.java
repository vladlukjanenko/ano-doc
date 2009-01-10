package net.anotheria.asg.service;


abstract public class BaseRemoteServiceSkeleton implements RemoteService{
	
	public byte[] getEcho(byte[] echoRequest){
		return echoRequest;
	}
	
}
