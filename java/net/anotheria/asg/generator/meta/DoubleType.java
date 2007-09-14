package net.anotheria.asg.generator.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class DoubleType extends AbstractType{

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.meta.IMetaType#toJava()
	 */
	public String toJava() {
		return "double";
	}
	public String toJavaObject(){
		return "Double";
	}

}