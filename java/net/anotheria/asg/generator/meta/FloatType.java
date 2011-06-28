package net.anotheria.asg.generator.meta;

/**
 * Internal representation of the FloatType.
 * @author lrosenberg
 */
public class FloatType extends AbstractType{

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.meta.IMetaType#toJava()
	 */
	@Override public String toJava() {
		return "float";
	}
	
	@Override public String toJavaObject(){
		return "Float";
	}

}
