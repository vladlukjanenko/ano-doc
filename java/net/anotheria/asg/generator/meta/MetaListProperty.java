package net.anotheria.asg.generator.meta;


/**
 * A container property is a container of a property of another type. This is usually used for lists or tables. They may only contain one type of the data - the containedPoperty.
 * @author another
 */
public class MetaListProperty extends MetaContainerProperty{
	
	/**
	 * The property inside this container.
	 */
	private MetaProperty containedProperty;
	
	public MetaListProperty(String name){
		super(name, MetaProperty.Type.LIST);
	}

	public MetaListProperty(String name, MetaProperty aContainedProperty){
		super(name, MetaProperty.Type.LIST);
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
	
	@Override public String toJavaType(){
		return super.toJavaType()+"<"+containedProperty.toJavaObjectType()+">";
	}
	
	@Override public String toJavaErasedType(){
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
