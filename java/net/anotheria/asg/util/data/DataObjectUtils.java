package net.anotheria.asg.util.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.anotheria.asg.data.DataObject;
import net.anotheria.util.ArrayUtils;

/**
 * Utilities for handling and manipulating single or multiple objects of any subclass of DataObject.
 * 
 * @author denis
 *
 */
public class DataObjectUtils {

	private DataObjectUtils() {
	}

	/**
	 * Creates for the List of dataObjects same ordered List of dataObjects' IDs except the case when null id was happened.
	 * 
	 * @param <T> - type of dataObjects that extends DataObject
	 * @param dataObjects - objects for which IDs List is created
	 * @return List of ID's
	 */
	public static <T extends DataObject> List<String> getIds(List<T> dataObjects){
		List<String> ret = new ArrayList<String>(dataObjects.size());
		for(T d: dataObjects)
			ret.add(d.getId());
		return ret;
	}
	
	/**
	 * Returns for the dataObjects the List that is filled with specified property values of each dataObject.
	 * 
	 * @param <T> - type of dataObjects that extends DataObject 
	 * @param <P> - type of dataObject property
	 * @param propertyName - name of the property to get from dataObjects
	 * @param propertyClass - class of the property
	 * @param dataObjects - dataObjects to get property values.
	 * @return List that is filled with specified property values
	 */
	public static <T extends DataObject, P> List<P> getProperties(String propertyName, Class<P> propertyClass, List<T> dataObjects){
		List<P> ret = new ArrayList<P>(dataObjects.size());
		for(T d: dataObjects)
			ret.add(propertyClass.cast(d.getPropertyValue(propertyName)));
		return ret;
	}
	
	/**
	 * Creates Map of dataObjects by their IDs.
	 * 
	 * @param <T> - type of dataObjects that extends DataObject
	 * @param dataObjects - dataObjects to create map.
	 * @return Map of pairs ID -> dataObject
	 */
	public static <T extends DataObject> Map<String,T> createMapById(List<T> dataObjects){
		Map<String,T> ret = new HashMap<String, T>();
		for(T d: dataObjects)
			ret.put(d.getId(), d);
		return ret;
	}
	
	/**
	 * Creates Map of dataObjects by specified property. Property value must be unique (key property) for each dataObject.
	 * In other case only one dataObject with not unique property will be added to the map.
	 * 
	 * @param <T> - type of dataObjects that extends DataObject 
	 * @param <P> - type of the dataObject property
	 * @param propertyName - name of the property by which Map is created
	 * @param propertyClass - class of the property
	 * @param dataObjects - dataObjects to create map.
	 * @return Map of pairs Property Value -> dataObject
	 */
	
	public static <T extends DataObject, P> Map<P,T> createMapByKeyProperty(String propertyName, Class<P> propertyClass, List<T> dataObjects){
		Map<P,T> ret = new HashMap<P, T>();
		for(T d: dataObjects)
			ret.put(propertyClass.cast(d.getPropertyValue(propertyName)), d);
		return ret;
	}
	
	/**
	 * Creates Map of groups (arrays) of dataObjects by specified property. DataObjects with equal property values are put to the same group.
	 * 
	 * @param <T> - type of dataObjects that extends DataObject 
	 * @param <P> - type of the dataObject property
	 * @param propertyName - name of the property by which Map is created
	 * @param propertyClass - class of the property
	 * @param dataObjects - dataObjects to create map.
	 * @return Map of pairs Property Value -> dataObjects group
	 */
	
	@SuppressWarnings("unchecked")
	public static <T extends DataObject, P> Map<P,T[]> createMapByProperty(String propertyName, Class<P> propertyClass, List<T> dataObjects){
		Map<P,T[]> ret = new HashMap<P, T[]>();
		for(T d: dataObjects){
			P propertyValue = propertyClass.cast(d.getPropertyValue(propertyName));
			T[] data = ret.get(propertyValue);
			if(data == null)
				data = (T[])java.lang.reflect.Array.newInstance(d.getClass(), 0);
			ret.put(propertyValue, ArrayUtils.addToArray(data, d));
		}
		return ret;
	}
	
}
