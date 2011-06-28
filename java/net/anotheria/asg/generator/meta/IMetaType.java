package net.anotheria.asg.generator.meta;

/**
 * Represent the type of a property.
 * @author lrosenberg
 */
public interface IMetaType {
	/**
	 * @return the type representation of this MetaType in java language.
	 */
	String toJava();
	/**
	 * @return java object for this type (for example Integer for int).
	 */
	String toJavaObject();
	
	String toPropertySetter();
	
	String toPropertyGetter();
	
	String toBeanGetter(String name);
	
	String toBeanSetter(String name);	
}
