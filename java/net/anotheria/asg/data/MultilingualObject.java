package net.anotheria.asg.data;

/**
 * Describes a multilingual object as 
 * @author lrosenberg.
 *
 */
public interface MultilingualObject {
	/**
	 * Returns true if the multilingual features are disabled for this instance.
	 * @return
	 */
	boolean isMultilingualDisabledInstance();
	
	/**
	 * Disables support for multilingual instances for this object.
	 * @param value
	 */
	void setMultilingualDisabledInstance(boolean value);
}
