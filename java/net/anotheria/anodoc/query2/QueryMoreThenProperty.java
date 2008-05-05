package net.anotheria.anodoc.query2;

import net.anotheria.anodoc.query2.QueryProperty;

/**
 * Difference with QueryProperty is property value of queried object must be more then specified in this query.
 * 
 * <b>IMPORTANT:<i>Tested only in postgressql!</i></b>
 * 
 * @author denis
 *
 */
public class QueryMoreThenProperty extends QueryProperty{
	
	public QueryMoreThenProperty(String name, Object value) {
		super(name, value);
	}

	public String toString(){
		return getName() + ">" +getValue();
	}
	
	public String getComparator(){
		return " > ";
	}
	
	public boolean doesMatch(Object o){
		throw new RuntimeException("Not Implemented");
	}
}
