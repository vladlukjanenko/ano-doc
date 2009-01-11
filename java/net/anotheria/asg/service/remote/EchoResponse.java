package net.anotheria.asg.service.remote;

import java.io.Serializable;

public class EchoResponse implements Serializable{

	private static final long serialVersionUID = 6461115896678559676L;
	
	private long responseTime;
//	private String serviceClass;
	private byte[] data;
	
	public long getResponseTime() {
		return responseTime;
	}
	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
		
}
