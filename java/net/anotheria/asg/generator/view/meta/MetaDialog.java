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
	 * Link to customization javascript
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
	
	@Override public String toString(){
		return name+" "+elements;
	}


	public String getJavascript() {
		return javascript;
	}


	public void setJavascript(String javascript) {
		this.javascript = javascript;
	}

}
