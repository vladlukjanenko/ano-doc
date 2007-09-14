package net.anotheria.asg.generator.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class BooleanType extends AbstractType{

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.meta.IMetaType#toJava()
	 */
	public String toJava() {
		return "boolean";
	}
	
	public String toJavaObject(){
		return "Boolean";
	}


	public String toBeanGetter(String name){
		return "is"+Character.toUpperCase(name.charAt(0))+name.substring(1);
	}
	

}