package net.anotheria.asg.generator.view.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaListElement extends MetaViewElement{
	private List<MetaViewElement> elements;
	
	public MetaListElement(){
		super("");
		elements = new ArrayList<MetaViewElement>();
	}
	
	/**
	 * @return
	 */
	public List<MetaViewElement> getElements() {
		return elements;
	}

	/**
	 * @param list
	 */
	public void setElements(List<MetaViewElement> list) {
		elements = list;
	}

	public void addElement(MetaViewElement element){
		elements.add(element);
	}
	
	public String toString(){
		return ""+elements;
	}
	
	public boolean isComparable(){
		return false;
	}
	
}
