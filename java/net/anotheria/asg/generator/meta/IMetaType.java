package net.anotheria.asg.generator.meta;

/**
 * Represent the type of a property.
 * @author lrosenberg
 */
public interface IMetaType {
	/**
	 * Returns the type representation of this MetaType in java language.
	 * @return
	 */
	String toJava();
	/**
	 * Returns java object for this type (for example Integer for int).
	 * @return
	 */
	String toJavaObject();
	
	String toPropertySetter();
	
	String toPropertyGetter();
	
	String toBeanGetter(String name);
	
	String toBeanSetter(String name);	
}
