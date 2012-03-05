package net.anotheria.asg.util.decorators;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.anotheria.anodoc.data.NoSuchPropertyException;
import net.anotheria.asg.data.DataObject;

/**
 * This decorator decorates long value to date time representation.
 * 
 * @author Alexandr Bolbat
 */
public class DateTimeDecorator implements IAttributeDecorator {

	/**
	 * Date format.
	 */
	public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm";

	@Override
	public String decorate(DataObject obj, String attributeName, String rule) {
		String caption = "";
		try {
			long time = Long.class.cast(obj.getPropertyValue(attributeName));
			caption = new SimpleDateFormat(DATE_FORMAT).format(new Date(time));
		} catch (NoSuchPropertyException e) {
		} catch (ClassCastException e) {
			caption = "" + obj.getPropertyValue(attributeName);
		}

		return caption;
	}

}
