package net.anotheria.asg.generator.view.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaDialog {
	private String name;
	private String title;
	private List<MetaViewElement> elements;
	
	public MetaDialog(String aName){
		this.name = aName;
		elements = new ArrayList<MetaViewElement>();
	}
	
	
	public void addElement(MetaViewElement element){
		elements.add(element);
	}
	
	

	/**
	 * @return
	 */
	public List<MetaViewElement> getElements() {
		return elements;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param list
	 */
	public void setElements(List<MetaViewElement> list) {
		elements = list;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string
	 */
	public void setTitle(String string) {
		title = string;
	}
	
	public String toString(){
		return name+" "+elements;
	}

}
