package net.anotheria.asg.generator.meta;

public class MetaGenericListProperty extends MetaGenericProperty{
	public MetaGenericListProperty(String name, MetaProperty... contained){
		super(name, "list", contained);
	}

	public String toPropertyGetterCall(){
		return "copyTo"+getContainedProperties().get(0).toJavaType()+"List(getList("+toNameConstant()+"))";
	}

	public String toPropertySetterCall(){
		return "setList("+toNameConstant()+", copyFrom"+getContainedProperties().get(0).toJavaType()+"List(value))";
	}
}
