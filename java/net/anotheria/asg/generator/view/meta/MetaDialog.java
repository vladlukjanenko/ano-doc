package net.anotheria.asg.generator.view.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * The definition of a dialog.
 * @author another
 */
public class MetaDialog {
	/**
	 * Name of the dialog.
	 */
	private String name;
	/**
	 * Title of the dialog.
	 */
	private String title;
	/**
	 * Elements of the dialog.
	 */
	private List<MetaViewElement> elements;
	/**
	 * Link to customization javascript.
	 */
	private String javascript;
	
	/**
	 * Creates a new dialog with the given name.
	 * @param aName the name of the dialog.
	 */
	public MetaDialog(String aName){
		this.name = aName;
		elements = new ArrayList<MetaViewElement>();
	}

	/**
	 * Adds new dialog element.
	 *
	 * @param element element to add
	 */
	public void addElement(MetaViewElement element){
		elements.add(element);
	}
	
	

	/**
	 *
	 * @return list of dialog elements
	 */
	public List<MetaViewElement> getElements() {
		return elements;
	}

	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}

	public void setElements(List<MetaViewElement> list) {
		elements = list;
	}

	public void setName(String string) {
		name = string;
	}

	public void setTitle(String string) {
		title = string;
	}

	public String getJavascript() {
		return javascript;
	}


	public void setJavascript(String javascript) {
		this.javascript = javascript;
	}

	@Override public String toString(){
		return name+" "+elements;
	}

}
