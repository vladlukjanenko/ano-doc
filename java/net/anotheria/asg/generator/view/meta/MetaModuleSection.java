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

	/**
	 * Adds dialog.
	 *
	 * @param d dialog to add
	 */
	public void addDialog(MetaDialog d){
		dialogs.add(d);
	}
	
	/**
	 * @return target module
	 */
	public MetaModule getModule() {
		return module;
	}

	/**
	 * Sets target module.
	 * @param module module to set
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
	 * @return elements of the view
	 */
	public List<MetaViewElement> getElements() {
		return elements;
	}

	/**
	 * Sets elements to view.
	 * @param list elements
	 */
	public void setElements(List<MetaViewElement> list) {
		elements = list;
	}
	/**
	 * Adds element to view.
	 * @param element element to add
	 */
	public void addElement(MetaViewElement element){
		elements.add(element);
	}

	/**
	 * @return dialogs list
	 */
	public List<MetaDialog> getDialogs() {
		return dialogs;
	}

	/**
	 * Sets dialogs.
	 * @param list dialogs to set
	 */
	public void setDialogs(List<MetaDialog> list) {
		dialogs = list;
	}

	/**
	 * @return default sortable element
	 */
	public MetaViewElement getDefaultSortable() {
		return defaultSortable;
	}

	/**
	 * Sets default sortable element.
	 * @param element element to set
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

	/**
	 * Adds filter in the view part of this section.
	 * @param aFilter filter to add
	 */
	public void addMetaFilter(MetaFilter aFilter){
		filters.add(aFilter);
	}

	/**
	 * @return filters in the view part of this section.
	 */
	public List<MetaFilter> getFilters() {
		return filters;
	}

	/**
	 * Sets filters in the view part of this section.
	 * @param filters filter to set
	 */
	public void setFilters(List<MetaFilter> filters) {
		this.filters = filters;
	}
	
	public boolean isValidatedOnSave() {
		if (dialogs != null && dialogs.size() > 0){
			List<MetaViewElement> dialogElements = dialogs.get(0).getElements(); 
			if (dialogElements != null) {
				for (MetaViewElement element : dialogElements){
					if (element.isValidated()) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
}
