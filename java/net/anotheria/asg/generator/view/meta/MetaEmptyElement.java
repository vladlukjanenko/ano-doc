package net.anotheria.asg.generator.view.meta;

/**
 * An empty element in a view.
 * @author another
 */
public class MetaEmptyElement extends MetaViewElement{
	/**
	 * Creates a new empty element.
	 */
	public MetaEmptyElement(){
		super(null);
	}
	
	@Override public String toString(){
		return "empty";
	}
	
	@Override public boolean isComparable(){
		return false;
	}
}
