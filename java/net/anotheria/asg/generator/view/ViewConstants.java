package net.anotheria.asg.generator.view;

/**
 * Some constants used in the view. 
 * @author another
 */
public interface ViewConstants {
	/**
	 * The sort type parameter.
	 */
	String PARAM_SORT_TYPE = "pSortType";
	/**
	 * The sort order parameter.
	 */
	String PARAM_SORT_ORDER = "pSortOrder";
	/**
	 * The name of the sort type.
	 */
	String PARAM_SORT_TYPE_NAME = "pSortName";
	
	/**
	 * The value for the sort order.
	 */
	String VALUE_SORT_ORDER_ASC = "ASC";
	/**
	 * The value for the sort order.
	 */
	String VALUE_SORT_ORDER_DESC = "DESC";
	
	/**
	 * The prefix for session attributes.
	 */
	String SA_PREFIX = "asg.sa.";
	/**
	 * Prefix for the session attributes for the sort type.
	 */
	String SA_SORT_TYPE_PREFIX = "sorttype.";
	/**
	 * Prefix for the session attributes for the filter state.
	 */
	String SA_FILTER_PREFIX = "filter.";
}
