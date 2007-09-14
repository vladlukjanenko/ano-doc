package net.anotheria.asg.generator.view.meta;

/**
 * TODO please remined another to comment this class
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
