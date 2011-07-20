package net.anotheria.asg.generator.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Utility class for date converting.
 * //TODO move to right place
 * @author dsilenko
 */
public final class DateUtils {

	/**
	 * Default format for date.
	 */
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

	/**
	 * Converts milliseconds to string
	 * @param milliseconds milliseconds of date
	 * @return
	 */
	public static String getString(long milliseconds){
		return SIMPLE_DATE_FORMAT.format(milliseconds);
	}

	/**
	 * Converts string that represents date to milliseconds.
	 * If some error occurred during converting then 0 will be returned.
	 * @param date string to convert
	 * @return milliseconds
	 */
	public static long getLong(String date){
		try {
			return SIMPLE_DATE_FORMAT.parse(date).getTime();
		} catch (ParseException e) {
			return 0;
		}
	}
}
