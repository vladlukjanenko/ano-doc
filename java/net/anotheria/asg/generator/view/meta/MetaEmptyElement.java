package net.anotheria.asg.generator.view.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaEmptyElement extends MetaViewElement{
	public MetaEmptyElement(){
		super(null);
	}
	
	public String toString(){
		return "empty";
	}
	
	public boolean isComparable(){
		return false;
	}
}
