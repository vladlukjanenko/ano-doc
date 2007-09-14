package net.anotheria.asg.util.filter;

import java.util.List;

import net.anotheria.asg.data.DataObject;

public interface DocumentFilter {
	/**
	 * Returns true if the document may pass the filtering defined by applying the filter parameter to the attribute
	 * with filters internal rule.
	 * @param document
	 * @param attributeName
	 * @param filterParameter
	 * @return
	 */
	public boolean mayPass(DataObject document, String attributeName, String filterParameter);
	
	public List<FilterTrigger> getTriggerer(String storedFilterParameter);
	
	
}
