package net.anotheria.asg.service;

import net.anotheria.asg.data.DataObject;

public class InMemoryObjectWrapper <T extends DataObject> {
	
	private T t ;
	
	
	private boolean created;
	private boolean updated;
	private boolean deleted;
	
	private long createdTimestamp;
	private long updatedTimestamp;
	private long deletedTimestamp;
	
	public InMemoryObjectWrapper(T aT){
		this(aT, false);
	}
	
	public InMemoryObjectWrapper(T aT, boolean created){
		t = aT;
		created = true;
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
