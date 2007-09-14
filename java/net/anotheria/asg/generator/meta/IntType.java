package net.anotheria.asg.generator.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class IntType extends AbstractType{

	/* (non-Javadoc)
 	 * @see net.anotheria.anodoc.generator.meta.IMetaType#toJava()
 	 */
	public String toJava() {
		return "int";
	}
	public String toJavaObject(){
		return "Integer";
	}

}