package net.anotheria.anodoc.data;


/**
 * Utility class to for unique document ids.
 * @author lrosenberg
 */
public class IDHolder extends Document{
	/**
	 * Prefix for the document name.
	 */
	public static final String DOC_ID_HOLDER_PRE = "anodoc.id.holder.";
	/**
	 * Attribute name of the last id storage.
	 */
	public static final String ATT_LAST_ID = "lastId";
	/**
	 * Constant for type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "type.id.holder"; 
	/**
	 * Creates a new idholder object with given id.
	 * @param id
	 */
	public IDHolder(String id){
		super(id);
		setTypeIdentifier(TYPE_IDENTIFIER);
	}
	/**
	 *
	 * @return next id as string. The id is unique
	 */
	public String getNextIdString(){
		String id = ""+getNextIdInt();
		while(id.length()<4)
			id = "0"+id;
		return id;
	}
	/**
	 * Returns the next id. Increased and saves the internal value.
	 * @return
	 */
	public int getNextIdInt(){
		int id = getInt(ATT_LAST_ID);
		//System.out.println("next id requested:"+id);
		id+=1;
		setInt(ATT_LAST_ID, id);
		return id;
	}
	/**
	 * Returns the next id without increasing it.
	 * @return
	 */
	public int getNextIdToGive(){
		return getInt(ATT_LAST_ID)+1;
	}
	
	public void adjustTill(int value){
		int id = getInt(ATT_LAST_ID);
		if (id<value)
			setInt(ATT_LAST_ID, value);
	}
 
	public void adjustTill(String value){
		adjustTill(Integer.parseInt(value));
	}
}
