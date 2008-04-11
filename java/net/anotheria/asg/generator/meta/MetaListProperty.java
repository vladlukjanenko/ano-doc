package net.anotheria.asg.generator.meta;


/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaListProperty extends MetaContainerProperty{
	
	
	private MetaProperty containedProperty;
	
	public MetaListProperty(String name){
		super(name, "list");
	}

	public MetaListProperty(String name, MetaProperty aContainedProperty){
		super(name, "list");
		containedProperty = aContainedProperty;
	}

	/**
	 * @return
	 */
	public MetaProperty getContainedProperty() {
		return containedProperty;
	}

	/**
	 * @param property
	 */
	public void setContainedProperty(MetaProperty property) {
		containedProperty = property;
	}

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.meta.MetaContainerProperty#getContainerEntryName()
	 */
	public String getContainerEntryName() {
		return "Element";
	}
	
	public String toJavaType(){
		return super.toJavaType()+"<"+containedProperty.toJavaType()+">";
	}
	
	public String toJavaErasedType(){
		return super.toJavaType();
	}

	
	
//	@Override
//	public String toJavaObjectType(){
//		return super.toJavaObjectType()+getGenericTypeDeclaration();
//	}
//	
//	protected String getGenericTypeDeclaration(){			
//		return containedProperty != null? StringUtils.surroundWith(containedProperty.toJavaObjectType(), '<', '>'): "";
//	}

}
