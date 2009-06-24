package net.anotheria.asg.generator.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public interface IMetaType {

	String toJava();
	
	String toJavaObject();
	
	String toPropertySetter();
	
	String toPropertyGetter();
	
	String toBeanGetter(String name);
	
	String toBeanSetter(String name);	
}
