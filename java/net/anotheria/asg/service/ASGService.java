package net.anotheria.asg.service;

import net.anotheria.anoprise.metafactory.Service;
import net.anotheria.asg.util.listener.IServiceListener;

/**
 * Interface for the basic ASGService. 
 * @author lrosenberg
 *
 */
public interface ASGService extends Service{
	/**
	 * Adds a service listener to this service.
	 * @param listener the listener to add.
	 */
	void addServiceListener(IServiceListener listener);

	/**
	 * Removes the service listener from the service.
	 * @param listener the listener to remove.
	 */
	void removeServiceListener(IServiceListener listener);

	/**
	 * Returns true if there are service listeners connected to this service.
	 * @return true if there are service listeners attached.
	 */
	boolean hasServiceListeners();
}
