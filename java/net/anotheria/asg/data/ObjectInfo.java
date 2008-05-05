package net.anotheria.asg.data;

public class ObjectInfo {
	
	
	/**
	 * The id of the document
	 */
	private String id;
	
	/**
	 * The author of the document
	 */
	private String author;
	
	/**
	 * The last change timestamp of the document
	 */
	private long lastChangeTimestamp;

	/**
	 * The footprint of the document (built over all attribute-values in all languages).
	 */
	private String footprint;

	public ObjectInfo(){
		
	}
	
	public ObjectInfo(DataObject object){
		setId(object.getId());
		setLastChangeTimestamp(object.getLastUpdateTimestamp());
		setFootprint(object.getFootprint());
	}

	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public long getLastChangeTimestamp() {
		return lastChangeTimestamp;
	}

	public void setLastChangeTimestamp(long lastChangeTimestamp) {
		this.lastChangeTimestamp = lastChangeTimestamp;
	}

	public String getFootprint() {
		return footprint;
	}

	public void setFootprint(String footprint) {
		this.footprint = footprint;
	}
	
	public String toString(){
		return "Id: "+getId()+", Ts: "+getLastChangeTimestamp()+", Footprint: "+getFootprint()+", Author: "+getAuthor();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
