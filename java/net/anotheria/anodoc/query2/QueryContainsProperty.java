package net.anotheria.anodoc.query2;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.anotheria.anodoc.data.Property;
import net.anotheria.util.StringUtils;

/**
 * Query to List/Array Property. Passes only if Property contains all queried values.
 * 
 * <b>IMPORTANT:<i>Not properly tested yet. Use on own risk!!!!</i></b>
 * 
 * @author denis
 *
 */
public class QueryContainsProperty <T>extends QueryProperty{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8649073486730051958L;
	
	public QueryContainsProperty(String aName, T... aValues){
		this(aName, Arrays.asList(aValues));
	}

	public QueryContainsProperty(String aName, Collection<T> aValues){
		super(aName, aValues);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean doesMatch(Object o) {
		if(o== null)
			return getOriginalValue() == null;
		List<Property> properties = ((List<Property>)o);
		Set<Object> toCompare = new HashSet<Object>();
		for(Property p: properties)
			toCompare.add(p.getValue());
		
		return toCompare.containsAll(getListValue());
	}

	@Override
	public String getComparator() {
		return " @> ";
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
