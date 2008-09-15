package net.anotheria.anodoc.query2;

import java.util.Collection;

import net.anotheria.util.StringUtils;

/**
 * Difference with QueryProperty is collection of possible values instead of single value.
 * 
 * <b>IMPORTANT:<i>Tested only in postgressql!</i></b>
 * 
 * @author denis
 *
 */
public class QueryInProperty <T>extends QueryProperty{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8649073486730051958L;

	public QueryInProperty(String aName, Collection<T> aValues){
		super(aName, aValues);
	}

	@Override
	public boolean doesMatch(Object o) {
		return o== null ?getOriginalValue() == null :
			getListValue().contains(o);
	}

	@Override
	public String getComparator() {
		return " IN ";
	}

	@Override
	public Object getValue() {
		Collection<T> values = getListValue(); 
		return StringUtils.surroundWith(StringUtils.concatenateTokens(values, ',', '\'', '\''), '(', ')');
	}
	
	@SuppressWarnings("unchecked")
	private Collection<T> getListValue(){
		return (Collection<T>) getOriginalValue();
	}

	@Override
	public boolean unprepaireable() {
		return true;
	}
	
	
}
