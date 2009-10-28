package net.anotheria.asg.service;

import net.anotheria.asg.data.DataObject;

public class InMemoryObjectWrapper <T extends DataObject> {
	/**
	 * The wrapped object.
	 */
	private T t ;
	
	/**
	 * True if it's a newly created object.
	 */
	private boolean created;
	/**
	 * True if it's an updated object.
	 */
	private boolean updated;
	/**
	 * True if it's a deleted object.
	 */
	private boolean deleted;
	/**
	 * Timestamp of object creation.
	 */
	private long createdTimestamp;
	/**
	 * Timestamp of object update.
	 */
	private long updatedTimestamp;
	/**
	 * Timestmap of object deletion.
	 */
	private long deletedTimestamp;
	
	public InMemoryObjectWrapper(T aT){
		this(aT, false);
	}
	
	public InMemoryObjectWrapper(T aT, boolean created){
		t = aT;
		this.created = created;
		createdTimestamp = System.currentTimeMillis();
	}

	public String getId(){
		return t.getId();
	}
	
	public T get(){
		return t;
	}
	
	public void update(T aT){
		t = aT;
		updated = true;
		updatedTimestamp = System.currentTimeMillis();
	}
	
	public void delete(){
		t = null;
		deleted = true;
		deletedTimestamp = System.currentTimeMillis();
	}
	
	public String toString(){
		return ""+t;
	}

	public boolean isCreated() {
		return created;
	}

	public boolean isUpdated() {
		return updated;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public long getCreatedTimestamp() {
		return createdTimestamp;
	}

	public long getUpdatedTimestamp() {
		return updatedTimestamp;
	}

	public long getDeletedTimestamp() {
		return deletedTimestamp;
	}
	
	
}
