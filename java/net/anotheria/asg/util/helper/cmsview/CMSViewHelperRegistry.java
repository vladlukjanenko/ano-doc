package net.anotheria.asg.util.helper.cmsview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for the view helpers.
 * @author lrosenberg
 *
 */
public class CMSViewHelperRegistry {
	/**
	 * A map with the helpers.
	 */
	private static Map<String, List<CMSViewHelper>> helperMap;
	/**
	 * An empty list as null object.
	 */
	private static final ArrayList<CMSViewHelper> EMPTY_LIST = new ArrayList<CMSViewHelper>(0);

	private CMSViewHelperRegistry() {
	}

	static{
		helperMap = Collections.synchronizedMap(new HashMap<String, List<CMSViewHelper>>());
	}
	
	public static void addCMSViewHelper(String documentPath, CMSViewHelper helper){
		List<CMSViewHelper> helpers = helperMap.get(documentPath);
		if (helpers==null){
			helpers = new ArrayList<CMSViewHelper>();
			helperMap.put(documentPath, helpers);
		}
		helpers.add(helper);
		
	}
	
	public static List<CMSViewHelper> getCMSViewHelpers(String documentPath){
		List<CMSViewHelper> ret = helperMap.get(documentPath);
		return ret == null ? EMPTY_LIST : ret;
	}
}
