package net.anotheria.asg.generator.view.meta;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;

/**
 * A MetaSection which is tied to a module and a document.
 * @author another
 */
public class MetaModuleSection extends MetaSection{
	/**
	 * The target module.
	 */
	private MetaModule module;
	/**
	 * The target document (int the module).
	 */
	private MetaDocument document;
	/**
	 * Elements of the view.
	 */
	private List<MetaViewElement> elements;
	/**
	 * Dialogs. Currently only one is supported.
	 */
	private List<MetaDialog> dialogs;
	/**
	 * The default sortable element.
	 */
	private MetaViewElement defaultSortable;
	/**
	 * Filters in the view part of this section.
	 */
	private List<MetaFilter> filters;
	
	/**
	 * Creates a new MetaModuleSection with the given title.
	 * @param title
	 */
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

	@Override public String toString(){
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
