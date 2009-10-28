package net.anotheria.asg.service.remote;

import java.io.Serializable;

/**
 * Response to an echo request.
 * @author another
 *
 */
public class EchoResponse implements Serializable{
	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = 6461115896678559676L;
	//TODO ? wie berechnet?
	/**
	 * Duration of the response.
	 */
	private long responseTime;
//	private String serviceClass;
	/**
	 * Sent data.
	 */
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
