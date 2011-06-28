package net.anotheria.asg.generator.meta;

/**
 * Representation of the Integer type.
 * @author another
 */
public class IntType extends AbstractType{

	/* (non-Javadoc)
 	 * @see net.anotheria.anodoc.generator.meta.IMetaType#toJava()
 	 */
	@Override public String toJava() {
		return "int";
	}
	@Override public String toJavaObject(){
		return "Integer";
	}

}
