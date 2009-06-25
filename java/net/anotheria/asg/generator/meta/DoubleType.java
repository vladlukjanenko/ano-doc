package net.anotheria.asg.generator.meta;

/**
 * Representation of the DoubleType.
 * @author another
 */
public class DoubleType extends AbstractType{

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.meta.IMetaType#toJava()
	 */
	@Override public String toJava() {
		return "double";
	}
	
	@Override public String toJavaObject(){
		return "Double";
	}

}