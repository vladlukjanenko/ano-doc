package net.anotheria.asg.generator.meta;

import java.util.ArrayList;
import java.util.List;

public class MetaGenericProperty extends MetaProperty{
	
	private List<MetaProperty> containedProperties;
	
	public MetaGenericProperty(String name, String type, MetaProperty... contained){
		super(name, type);
		containedProperties = new ArrayList<MetaProperty>();
		for (MetaProperty p : contained)
			containedProperties.add(p);
	}
	
	public String toJavaType(){
		return super.toJavaType()+"<"+getGenericTypeDeclaration()+">";
	}
	
	protected String getGenericTypeDeclaration(){
		String ret = "";
		for (MetaProperty p : containedProperties){
			if (ret.length()>0)
				ret += ",";
			ret += p.toJavaType();
		}
			
		return ret;
	}
	
	protected List<MetaProperty> getContainedProperties(){
		return containedProperties;
	}
	
	public String toPropertyGetterCall(){
		throw new RuntimeException("Not supported :-(");
	}
	
	public String toPropertySetterCall(){
		throw new RuntimeException("Not supported :-(");
	}
}
