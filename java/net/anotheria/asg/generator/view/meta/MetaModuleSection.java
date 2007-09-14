package net.anotheria.asg.generator.view.meta;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaModuleSection extends MetaSection{
	
	private MetaModule module;
	private MetaDocument document;
	private List<MetaViewElement> elements;
	private List<MetaDialog> dialogs;
	private MetaViewElement defaultSortable;
	private List<MetaFilter> filters;
	
	public MetaModuleSection(String title){
		super(title);
		elements = new ArrayList<MetaViewElement>();
		dialogs = new ArrayList<MetaDialog>();
		defaultSortable = null;
		filters = new ArrayList<MetaFilter>();
	}
	
	public void addDialog(MetaDialog d){
		dialogs.add(d);
	}
	
	/**
	 * @return
	 */
	public MetaModule getModule() {
		return module;
	}

	/**
	 * @param module
	 */
	public void setModule(MetaModule module) {
		this.module = module;
	}

	public String toString(){
		return super.toString()+" "+module+" elements: "+elements+" D: "+dialogs;
	}
    /**
     * @return Returns the document.
     */
    public MetaDocument getDocument() {
        return document;
    }
    /**
     * @param document The document to set.
     */
    public void setDocument(MetaDocument document) {
        this.document = document;
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

	/**
	 * @return
	 */
	public List<MetaDialog> getDialogs() {
		return dialogs;
	}

	/**
	 * @param list
	 */
	public void setDialogs(List<MetaDialog> list) {
		dialogs = list;
	}

	/**
	 * @return
	 */
	public MetaViewElement getDefaultSortable() {
		return defaultSortable;
	}

	/**
	 * @param element
	 */
	public void setDefaultSortable(MetaViewElement element) {
		defaultSortable = element;
	}

	public boolean containsComparable(){
		for (MetaViewElement element : elements){
			if (element.isComparable())
				return true;
		}
		return false;
	}
	
	public void addMetaFilter(MetaFilter aFilter){
		filters.add(aFilter);
	}

	public List<MetaFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<MetaFilter> filters) {
		this.filters = filters;
	}
	
	
}
