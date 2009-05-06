package net.anotheria.anodoc.data;


/**
 * Utility class to for unique document ids.
 * @author lrosenberg
 */
public class IDHolder extends Document{
	public static final String DOC_ID_HOLDER_PRE = "anodoc.id.holder.";
	public static final String ATT_LAST_ID = "lastId";
	public static final String TYPE_IDENTIFIER = "type.id.holder"; 
	
	public IDHolder(String id){
		super(id);
		setTypeIdentifier(TYPE_IDENTIFIER);
	}
	 
	public String getNextIdString(){
		String id = ""+getNextIdInt();
		while(id.length()<4)
			id = "0"+id;
		return id;
	}
	
	public int getNextIdInt(){
		int id = getInt(ATT_LAST_ID);
		//System.out.println("next id requested:"+id);
		id+=1;
		setInt(ATT_LAST_ID, id);
		return id;
	}
	
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
