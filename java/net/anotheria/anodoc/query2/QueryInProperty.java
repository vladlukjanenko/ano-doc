package net.anotheria.anodoc.query2;

import java.util.Collection;

import net.anotheria.anodoc.query2.QueryProperty;
import net.anotheria.asg.data.DataObject;
import net.anotheria.util.StringUtils;

/**
 * Difference with QueryProperty is collection of possible values instead of single value.
 * 
 * <b>IMPORTANT:<i>Tested only in postgressql!</i></b>
 * 
 * @author denis
 *
 */
public class QueryInProperty extends QueryProperty{
	
	public <T> QueryInProperty(String aName, Collection<T> aValues){
		super(aName, aValues);
	}

	@Override
	public boolean doesMatch(Object o) {
		return o== null ?getOriginalValue() == null :
			getListValue().contains(((DataObject)o).getId());
	}

	@Override
	public String getComparator() {
		return " IN ";
	}

	@Override
	public Object getValue() {
		Collection<String> values = getListValue(); 
		return StringUtils.surroundWith(StringUtils.concatenateTokens(values, ',', '\'', '\''), '(', ')');
	}
	
	@SuppressWarnings("unchecked")
	private Collection<String> getListValue(){
		return (Collection<String>) getOriginalValue();
	}

	@Override
	public boolean unprepaireable() {
		return true;
	}
	
	
}
