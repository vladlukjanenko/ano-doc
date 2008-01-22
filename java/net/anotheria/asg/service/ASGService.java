package net.anotheria.asg.service;

import net.anotheria.asg.util.listener.IServiceListener;

public interface ASGService {
	public void addServiceListener(IServiceListener listener);

	public void removeServiceListener(IServiceListener listener);

	public boolean hasServiceListeners();

}
