package net.anotheria.asg.service;

import net.anotheria.asg.data.DataObject;
import net.anotheria.asg.util.listener.IServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractASGService implements ASGService{
	
	/**
	 * {@link Logger} instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractASGService.class);

	/**
	 * List with listeners. This list is a CopyOnWriteArrayList, hence its safe to add a new listener anytime. However, typically you will add a listener on init of some stuff.
	 */
	private List<IServiceListener> listeners = new CopyOnWriteArrayList<IServiceListener>();
	
	/**
	 * Fires the create event. The method returns after all listeners have been notified. Exceptions from listeners are ignored (and logged).
	 * @param created the created object.
	 */
	protected void fireObjectCreatedEvent(DataObject created){
		for (IServiceListener listener : listeners){
			try{
				listener.documentCreated(created);
			}catch(Exception e){
				LOGGER.warn("Caught uncaught exception by the listener " + listener + ", fireObjectCreatedEvent(" + created + ")", e);
			}
		}
	}
	
	/**
	 * Fires the create event. The method returns after all listeners have been notified. Exceptions from listeners are ignored (and logged).
	 * @param oldVersion the previously existed object.
	 * @param newVersion the newly updated object.
	 */
	protected void fireObjectUpdatedEvent(DataObject oldVersion, DataObject newVersion){
		for (IServiceListener listener : listeners){
			try{
				listener.documentUpdated(oldVersion, newVersion);
			}catch(Exception e){
				LOGGER.warn("Caught uncaught exception by the listener " + listener + ", fireObjectUpdatedEvent(" + oldVersion + ", " + newVersion + ")", e);
			}
		}
	}
	
	/**
	 * Fires the delete event. The method returns after all listeners have been notified. Exceptions from listeners are ignored (and logged).
	 * @param deleted the deleted object.
	 */
	protected void fireObjectDeletedEvent(DataObject deleted){
		for (IServiceListener listener : listeners){
			try{
				listener.documentDeleted(deleted);
			}catch(Exception e){
				LOGGER.warn("Caught uncaught exception by the listener " + listener + ", fireObjectDeletedEvent(" + deleted + ")", e);
			}
		}
	}

    /**
	 * Fires the import event. The method returns after all listeners have been notified. Exceptions from listeners are ignored (and logged).
	 * @param imported the imported object.
	 */
	protected void fireObjectImportedEvent(DataObject imported){
		for (IServiceListener listener : listeners){
			try{
				listener.documentImported(imported);
			}catch(Exception e){
				LOGGER.warn("Caught uncaught exception by the listener " + listener + ", fireObjectImportedEvent(" + imported + ")", e);
			}
		}
	}

	/**
	 * Fires the persistence change event. The method returns after all listeners have been notified. Exceptions from listeners are ignored (and logged).
	 */
	protected void firePersistenceChangedEvent(){
		for (IServiceListener listener : listeners){
			try{
				listener.persistenceChanged();
			}catch(Exception e){
				LOGGER.warn("Caught uncaught exception by the listener " + listener + ", firePersistenceChangedEvent()", e);
			}
		}
	}

	/**
	 * Adds a service listener to this service.
	 * @param listener the listener to add.
	 */
	public void addServiceListener(IServiceListener listener){
		listeners.add(listener);
	}

	/**
	 * Removes the service listener from the service.
	 * @param listener the listener to remove.
	 */
	public void removeServiceListener(IServiceListener listener){
		listeners.remove(listener);
	}

	/**
	 * Returns true if there are service listeners connected to this service.
	 * @return true if there are service listeners attached.
	 */
	public boolean hasServiceListeners(){
		return listeners.size()>0;
	}

}
