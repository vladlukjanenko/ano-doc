package net.anotheria.anodoc.query2;

import net.anotheria.anodoc.query2.QueryProperty;

/**
 * Difference with QueryProperty is property value of queried object must be less then specified in this query.
 * 
 * <b>IMPORTANT:<i>Tested only in postgressql!</i></b>
 * 
 * @author denis
 *
 */
public class QueryLessThenProperty<P extends Comparable<P>> extends QueryProperty{
	
	private static final long serialVersionUID = -4870076734697577172L;

	public QueryLessThenProperty(String name, P value) {
		super(name, value);
	}

	public String toString(){
		return getName() + "<" +getValue();
	}
	
	public String getComparator(){
		return " < ";
	}
	
	@Override
	public boolean doesMatch(Object o){
		if(getValue() == null || o == null)
			return false;
		P value = (P)getValue();
		if(!(o instanceof Comparable))
			throw new RuntimeException("Matched object must implement interface Comprable!");
		P anotherValue = (P) o;
		return value.compareTo(anotherValue) > 0;
	}
	
}
