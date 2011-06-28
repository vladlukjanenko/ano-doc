package net.anotheria.asg.generator.meta;

/**
 * Representation of the LongType.
 * @author another
 */
public class LongType extends AbstractType{

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.meta.IMetaType#toJava()
	 */
	@Override public String toJava() {
		return "long";
	}
	
	@Override public String toJavaObject(){
		return "Long";
	}
}
