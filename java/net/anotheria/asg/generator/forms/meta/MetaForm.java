package net.anotheria.asg.generator.forms.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * The MetaInformation object which holds all data for a web-feedback-form.
 * @author lrosenberg
 */
public class MetaForm {
	/**
	 * Recipients of the submitted form.
	 */
    private List<String> targets;
    /**
     * Elements of the form.
     */
    private List<MetaFormField> elements;
    /**
     * Id of the form.
     */
    private String id;
    /**
     * Action of the form.
     */
    private String action;
    
    private String path;

	/**
	 * Default constructor.
	 * @param anId
	 */
    public MetaForm(String anId){
        this.id = anId;
        targets = new ArrayList<String>();
        elements = new ArrayList<MetaFormField>();
    }

	/**
	 * Adds recipients of the submitted form.
	 * @param target target to add
	 */
    public void addTarget(String target){
        targets.add(target);
    }

	/**
	 * Adds element of the form.
	 * @param element element to add
	 */
    public void addElement(MetaFormField element){
        elements.add(element);
    }
    /**
     * @return Returns the elements.
     */
    public List<MetaFormField> getElements() {
        return elements;
    }
    /**
     * @param elements The elements to set.
     */
    public void setElements(List<MetaFormField> elements) {
        this.elements = elements;
    }
    /**
     * @return Returns the targets.
     */
    public List<String> getTargets() {
        return targets;
    }
    /**
     * @param targets The targets to set.
     */
    public void setTargets(List<String> targets) {
        this.targets = targets;
    }
    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }
    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }
    /**
     * @param path The path to set.
     */
    public void setPath(String path) {
        this.path = path;
    }
    /**
     * @return Returns the action.
     */
    public String getAction() {
        return action;
    }
    /**
     * @param action The action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }

	@Override
	public String toString(){
		return id+", targets: "+targets+", elements: "+elements;
	}

}
