/* ------------------------------------------------------------------------- *
$Source: /work/cvs/ano-doc/java/net/anotheria/asg/generator/forms/meta/MetaForm.java,v $
$Author: lrosenberg $
$Date: 2006/12/28 22:22:04 $
$Revision: 1.4 $


Copyright 2004-2005 by FriendScout24 GmbH, Munich, Germany.
All rights reserved.

This software is the confidential and proprietary information
of FriendScout24 GmbH. ("Confidential Information").  You
shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement
you entered into with FriendScout24 GmbH.
See www.friendscout24.de for details.
** ------------------------------------------------------------------------- */
package net.anotheria.asg.generator.forms.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * The MetaInformation object which holds all data for a web-feedback-form.
 * @author lrosenberg
 * @created on Mar 14, 2005
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
    
    public MetaForm(String anId){
        this.id = anId;
        targets = new ArrayList<String>();
        elements = new ArrayList<MetaFormField>();
    }
    
    public void addTarget(String target){
        targets.add(target);
    }
    
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
    
    public String toString(){
        return id+", targets: "+targets+", elements: "+elements;
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
}
