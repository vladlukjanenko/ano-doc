package net.anotheria.asg.util.decorators;

import net.anotheria.asg.data.DataObject;

/**
 * This interface declares an attribute decorator, which can be configured to be used
 * by the generated ShowActions. The attribute decorator provides a string decoration for an object to produce a 
 * more readable form of attribute description, for example decorating link targets with names of the linked attributes.  
 * @author lrosenberg
 */
public interface IAttributeDecorator {
	/**
	 * Returns the decorated form.
	 * @param doc target document.
	 * @param attributeName name of the attribute for decoration.
	 * @param rule evtl decoration rule (if configured in view definition file).
	 * @return
	 */
	String decorate(DataObject doc, String attributeName, String rule);
}
