package net.anotheria.asg.util.helper.cmsview;

import net.anotheria.asg.data.DataObject;

public interface CMSViewHelper {
	/**
	 * 
	 * @param documentPath Module.Document from the data definition
	 * @param object the edited object (if any)
	 * @param property the property of the object which should be explained
	 * @return
	 */
	public String getFieldExplanation(String documentPath, DataObject object, String property);
		
	
}
