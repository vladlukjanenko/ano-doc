package net.anotheria.asg.util.decorators;

import net.anotheria.asg.data.DataObject;
import net.anotheria.asg.exception.ASGRuntimeException;

/**
 * This interface declares an attribute decorator, which can be configured to be used
 * by the generated ShowActions.  
 * @author another
 */
public interface IAttributeDecorator {
	public String decorate(DataObject doc, String attributeName, String rule);
}
