package net.anotheria.asg.generator.view.meta;

/**
 * A view element which is tied to a document attribute and presented as edit-field.
 * @author another
 */
public class MetaFieldElement extends MetaViewElement{
	
	public MetaFieldElement(String name){
		super(name);
	}
	
	
	
	public String toString(){
		return "Field "+getName();
	}

	public String getVariableName(){
		return getName();
	}
}
