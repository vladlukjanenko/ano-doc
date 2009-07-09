package net.anotheria.anodoc.query2;


/**
 * Difference with QueryProperty is property value of queried object must be less then specified in this query.
 * 
 * <b>IMPORTANT:<i>Tested only in postgressql!</i></b>
 * 
 * @author denis
 *
 */
public class QueryNotEqualProperty extends QueryProperty{
	/**
	 * svuid.
	 */
	private static final long serialVersionUID = 4846334131763003505L;
	/**
	 * Creates a new QueryNotEqualProperty.
	 * @param name
	 * @param value
	 */
	public QueryNotEqualProperty(String name, Object value) {
		super(name, value);
	}

	@Override public String toString(){
		return getName() + "!=" +getValue();
	}
	
	@Override public String getComparator(){
		return " != ";
	}
	
	@Override public boolean doesMatch(Object o){
		return !super.doesMatch(o);
	}
}
