package net.anotheria.asg.generator.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public interface IMetaType {

	public String toJava();
	
	public String toJavaObject();
	
	public String toPropertySetter();
	
	public String toPropertyGetter();
	
	public String toBeanGetter(String name);
	
	public String toBeanSetter(String name);	
}
