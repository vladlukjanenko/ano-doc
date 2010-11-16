package net.anotheria.asg.util.listener;

import net.anotheria.asg.data.DataObject;
/**
 * An adapter class for ServiceListeners. 
 * @author another
 *
 */
public abstract class ServiceListenerAdapter implements IServiceListener{

	@Override public void documentCreated(DataObject doc) {
	}

	@Override public void documentDeleted(DataObject doc) {
	}

	@Override public void documentUpdated(DataObject oldVersion, DataObject newVersion) {
	}

    @Override public void documentImported(DataObject doc) {
	}

	@Override public void persistenceChanged() {
	}

}
