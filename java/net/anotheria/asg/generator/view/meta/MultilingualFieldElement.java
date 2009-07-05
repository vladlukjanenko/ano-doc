package net.anotheria.asg.generator.view.meta;

import net.anotheria.util.StringUtils;

/**
 * If an element is specified to be multilingual, it's replaced by a multilingualfield element for each language. This way 
 * generation of language specific data for each language is guaranteed.
 * @author another
 *
 */
public class MultilingualFieldElement extends MetaFieldElement{
	/**
	 * Elements language.
	 */
	private String language;
	/**
	 * The element this copy refers to.
	 */
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
	
	@Override
	public boolean isRich() {
		return mappedElement.isRich();
	}
	
	@Override
	public String getVariableName(){
		return getName()+StringUtils.capitalize(language);
	}
	
	

}
