package net.anotheria.anodoc.query2;


/**
 * Difference with QueryProperty is property value of queried object must be more then specified in this query.
 * 
 * <b>IMPORTANT:<i>Tested only in postgressql!</i></b>
 * 
 * @author denis
 *
 */
public class QueryMoreThenProperty <P extends Comparable<P>> extends QueryProperty{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1772130142649741117L;

	public QueryMoreThenProperty(String name, P value) {
		super(name, value);
	}

	public String toString(){
		return getName() + ">" +getValue();
	}
	
	public String getComparator(){
		return " > ";
	}
	
	@Override
	public boolean doesMatch(Object o){
		if(getValue() == null || o == null)
			return false;
		P value = (P)getValue();
		if(!(o instanceof Comparable))
			throw new RuntimeException("Matched object must implement interface Comprable!");
		P anotherValue = (P) o;
		return value.compareTo(anotherValue) < 0;
	}
}
