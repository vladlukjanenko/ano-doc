package net.anotheria.asg.util.helper.cmsview;

import java.util.List;

import net.anotheria.asg.data.DataObject;

public class CMSViewHelperUtil {

	private CMSViewHelperUtil() {
	}

	public static String getFieldExplanation(String documentPath, DataObject object, String property){
		List<CMSViewHelper> helpers = CMSViewHelperRegistry.getCMSViewHelpers(documentPath);
		boolean moreThanOne = false;
		String explanation = "";
		for (CMSViewHelper h : helpers){
			String message = h.getFieldExplanation(documentPath, object, property);
			if (message!=null){
				if (explanation.length()>0){
					moreThanOne = true;
					explanation+=", ";
				}
				explanation += message;
			}
		}
		if (moreThanOne)
			explanation = "CONFLICT: "+explanation;
		return explanation.length()>0 ? 
				explanation : null;
	}

}
