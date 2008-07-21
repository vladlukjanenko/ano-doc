package net.anotheria.anodoc.query2;


/**
 * 
 * <b>IMPORTANT:<i>Tested only in postgressql!</i></b>
 * 
 * @author denis
 *
 */
public class QueryModProperty extends QueryProperty{
	
	private long mod;
	
	public <T> QueryModProperty(String aName, long aMod, long aValue){
		super(aName, aValue);
		mod = aMod;
		
	}

	@Override
	public boolean doesMatch(Object o) {
		return o == null ?getOriginalValue() == null :
			((Long)o) % mod == (Long)getValue();
	}

	@Override
	public String getComparator() {
		return " %  " + mod + " = ";
	}
	
	@Override
	public boolean unprepaireable() {
		return false;
	}

	public long getMod() {
		return mod;
	}

	public void setMod(long mod) {
		this.mod = mod;
	}
	
	
}
