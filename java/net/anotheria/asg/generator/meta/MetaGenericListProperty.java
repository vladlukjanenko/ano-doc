package net.anotheria.asg.generator.meta;

public class MetaGenericListProperty extends MetaGenericProperty{
	public MetaGenericListProperty(String name, MetaProperty... contained){
		super(name, MetaProperty.Type.LIST, contained);
	}

	public String toPropertyGetterCall(){
		return "copyTo"+getContainedProperties().get(0).toJavaObjectType()+"List(getList("+toNameConstant()+"))";
	}

	public String toPropertyGetterCall(String language){
		return "copyTo"+getContainedProperties().get(0).toJavaObjectType()+"List(getList("+toNameConstant(language)+"))";
	}

	public String toPropertySetterCall(){
		return "setList("+toNameConstant()+", copyFrom"+getContainedProperties().get(0).toJavaObjectType()+"List(value))";
	}

	public String toPropertySetterCall(String language){
		return "setList("+toNameConstant(language)+", copyFrom"+getContainedProperties().get(0).toJavaObjectType()+"List(value))";
	}

	public String toPropertyGetterCallForCurrentLanguage(String langVariable){
		return "copyTo"+getContainedProperties().get(0).toJavaObjectType()+"List(getList(\""+getName()+"_\"+"+langVariable+"))";
	}

	public String toPropertySetterCallForCurrentLanguage(String langVariable){
		return "setList("+"\""+getName()+"_\"+"+langVariable+", copyFrom"+getContainedProperties().get(0).toJavaObjectType()+"List(value))";
	}


}
