package net.anotheria.asg.util.filter;

import java.util.List;

import net.anotheria.asg.data.DataObject;

/**
 * A filter which reduces the documents showed in the cms.
 * @author another
 *
 */
public interface DocumentFilter {
	/**
	 * Returns true if the document may pass the filtering defined by applying the filter parameter to the attribute
	 * with filters internal rule.
	 * @param document
	 * @param attributeName
	 * @param filterParameter
	 * @return
	 */
	boolean mayPass(DataObject document, String attributeName, String filterParameter);
	
	/**
	 * @param storedFilterParameter
	 * @return the list of triggerers
	 */
	List<FilterTrigger> getTriggerer(String storedFilterParameter);
	
	
}
