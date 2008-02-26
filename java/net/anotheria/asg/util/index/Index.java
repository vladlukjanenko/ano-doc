package net.anotheria.asg.util.index;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.anotheria.asg.data.DataObject;

/**
 * This class is yet completely unsynchronized, so use in multithreaded environments with CARE.
 * @author another
 *
 * @param <T>
 */
public class Index <T>{
	
	
	private String propertyName;
	private Map<T, Set<String>> index;
	
	public Index(String aPropertyName){
		propertyName = aPropertyName;
		index = new HashMap<T, Set<String>>();
	}

	
	public void addObjectToIndex(DataObject object){
		T value = (T) (object.getPropertyValue(propertyName));
		Set<String> ids = index.get(value);
		if (ids==null){
			ids = new HashSet<String>();
			index.put(value, ids);
		}
		ids.add(object.getId());
	}
	
	public void removeObjectFromIndex(DataObject object){
		T value = (T) (object.getPropertyValue(propertyName));
		Set<String> ids = index.get(value);
		if (ids==null)
			return;
		ids.remove(object.getId());
	}
	
	public Set<String> getIds(Object o){
		return index.get((T)o);
	}

	public String toString(){
		return "Index "+propertyName+" "+index.size()+" values";
	}

}
