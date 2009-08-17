package net.anotheria.asg.util.decorators;

import net.anotheria.asg.data.DataObject;
import net.anotheria.util.NumberUtils;

/**
 * Decorates the attribute with the given attribute name in the given document
 * with help of standart itoa routine. Number of characters is given in the rule parameter.
 * Example: decorate(doc, "count", "3"), with value of attribute count being 7 in the given document
 * will result in "007". 
 * This decorator is useful to make integer values stored as strings sortable.
 * @author another
 */
public class ItoADecorator implements IAttributeDecorator{
	
	@Override public String decorate(DataObject doc, String attributeName, String rule) {
		int fillage = Integer.parseInt(rule);
		int value = Integer.parseInt(""+doc.getPropertyValue(attributeName));
		return ""+NumberUtils.itoa(value, fillage);
	}

}
