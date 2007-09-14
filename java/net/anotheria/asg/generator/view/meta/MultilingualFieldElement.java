package net.anotheria.asg.generator.view.meta;

import net.anotheria.util.StringUtils;


public class MultilingualFieldElement extends MetaFieldElement{
	private String language;
	private MetaFieldElement mappedElement;
	
	public MultilingualFieldElement(String aLanguage, MetaFieldElement aMappedElement){
		super(aMappedElement.getName());
		language = aLanguage;
		mappedElement = aMappedElement;
	}
	
	public String getLanguage(){
		return language;
	}
	
	public MetaFieldElement getMappedElement(){
		return mappedElement;
	}

	@Override
	public MetaDecorator getDecorator() {
		
		return mappedElement.getDecorator();
	}

	@Override
	public boolean isComparable() {
		return mappedElement.isComparable();
	}

	@Override
	public boolean isReadonly() {
		return mappedElement.isReadonly();
	}
	
	public String getVariableName(){
		return getName()+StringUtils.capitalize(language);
	}
	
	

}
