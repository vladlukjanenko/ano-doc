package net.anotheria.asg.generator.meta;

public class ObjectType implements IMetaType{
	
	private String clazz;
	
	public ObjectType(String aClazz){
		clazz = aClazz;
	}

	public String toBeanGetter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public String toBeanSetter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public String toJava() {
		return clazz;
	}

	public String toJavaObject() {
		return clazz;
	}

	public String toPropertyGetter() {
		// TODO Auto-generated method stub
		return null;
	}

	public String toPropertySetter() {
		return null;
	}

}
