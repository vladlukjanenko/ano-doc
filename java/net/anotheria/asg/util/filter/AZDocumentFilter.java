package net.anotheria.asg.util.filter;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.anodoc.data.NoSuchPropertyException;
import net.anotheria.asg.data.DataObject;

public class AZDocumentFilter implements DocumentFilter{
	
	private static List<FilterTrigger> triggerer;
	
	static{
		triggerer = new ArrayList<FilterTrigger>();
		triggerer.add(new FilterTrigger("All",""));
		for (int i='A'; i<='Z'; i++){
			triggerer.add(new FilterTrigger(""+(char)i,""+(char)i));
		}
	}

	public List<FilterTrigger> getTriggerer(String storedFilterParameter) {
		return triggerer;
	}

	public boolean mayPass(DataObject document, String attributeName, String filterParameter) {
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

	public static void main(String a[]){
		System.out.println(triggerer);
	}
}
