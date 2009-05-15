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
	
	private static final long serialVersionUID = 4846334131763003505L;

	public QueryNotEqualProperty(String name, Object value) {
		super(name, value);
	}

	public String toString(){
		return getName() + "!=" +getValue();
	}
	
	public String getComparator(){
		return " != ";
	}
	
	public boolean doesMatch(Object o){
		return !super.doesMatch(o);
	}
}
