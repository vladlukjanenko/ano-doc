package net.anotheria.asg.util.decorators;

import net.anotheria.asg.data.DataObject;
import net.anotheria.util.NumberUtils;

/**
 * Same as ItoADecorator, but instead of decorating the attribute with the name 
 * "attributeName", the id of the given document is taken. This decorator is needed, since
 * the id of the document isn't an official attribute. 
 * The parameter attribute name is ignored. 
 * @author another
 */
public class IDItoADecorator implements IAttributeDecorator{
	

	@Override public String decorate(DataObject doc, String attributeName, String rule) {
		int fillage = Integer.parseInt(rule);
		int value = Integer.parseInt(""+doc.getId());
		return ""+NumberUtils.itoa(value, fillage);
	}

}
