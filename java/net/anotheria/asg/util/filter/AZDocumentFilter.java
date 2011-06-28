package net.anotheria.asg.util.filter;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.anodoc.data.NoSuchPropertyException;
import net.anotheria.asg.data.DataObject;

/**
 * Expose documents filtering by first letter of document name.
 * 
 * (TEST COMMIT)
 */
public class AZDocumentFilter implements DocumentFilter{
	
	/**
	 * List of filter triggeres (one for each letter).
	 */
	private static List<FilterTrigger> triggerer;
	
	static{
		triggerer = new ArrayList<FilterTrigger>();
		triggerer.add(new FilterTrigger("All",""));
		for (int i='A'; i<='Z'; i++){
			triggerer.add(new FilterTrigger(""+(char)i,""+(char)i));
		}
	}

	@Override public List<FilterTrigger> getTriggerer(String storedFilterParameter) {
		return triggerer;
	}

	@Override public boolean mayPass(DataObject document, String attributeName, String filterParameter) {
		if (filterParameter==null || filterParameter.length()==0)
			return true;
		String propertyValue = null;
		try{
			propertyValue = ""+document.getPropertyValue(attributeName);
			if (propertyValue.startsWith(filterParameter) || propertyValue.startsWith(filterParameter.toLowerCase()))
				return true;
			return false;
		}catch(NoSuchPropertyException e){
			return false;
		}catch(Exception e){
			return false;
		}
	}
}
