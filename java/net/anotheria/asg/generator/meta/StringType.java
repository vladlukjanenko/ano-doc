package net.anotheria.asg.generator.meta;

/**
 * Representation of the string type.
 * @author another
 */
public class StringType extends AbstractType{

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.meta.IMetaType#toJava()
	 */
	@Override public String toJava() {
		return "String";
	}

	@Override public String toJavaObject(){
		return toJava();
	}

}
