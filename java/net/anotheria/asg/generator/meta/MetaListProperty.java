package net.anotheria.asg.generator.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaListProperty extends MetaContainerProperty{
	
	private MetaProperty containedProperty;
	
	public MetaListProperty(String name){
		super(name);
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

}
