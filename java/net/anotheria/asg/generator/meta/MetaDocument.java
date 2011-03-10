package net.anotheria.asg.generator.meta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.anotheria.asg.generator.IGenerateable;

/**
 * Internal representation of a document.
 * @author another
 */
public class MetaDocument implements IGenerateable{
	/**
	 * Name of the document.
	 */
	private String name;
	/**
	 * List of properties contained in this document.
	 */
	private List<MetaProperty> properties;
	/**
	 * List of links.
	 */
	private List<MetaProperty> links;
	
	/**
	 * Saves the property names to prevent double properties or links.
	 */
	private Set<String> propertyNames;
	/**
	 * Module i belong to.
	 */
	private MetaModule parentModule;
	
	/**
	 * Creates a new document with the given name.
	 * @param aName
	 */
	public MetaDocument(String aName){
		name = aName;
		properties = new ArrayList<MetaProperty>();
		links = new ArrayList<MetaProperty>();
		propertyNames = new HashSet<String>();
	}
	
	/**
	 * Adds a property to the document.
	 * @param p property to add.
	 */
	public void addProperty(MetaProperty p){
		if (propertyNames.contains(p.getName()))
			throw new IllegalArgumentException("This document already contains a property or link with name "+p.getName());
				
		propertyNames.add(p.getName());
		properties.add(p);
	}
	
	/**
	 * Adds a link to the document.
	 * @param l
	 */
	public void addLink(MetaLink l){
		if (propertyNames.contains(l.getName()))
			throw new IllegalArgumentException("This document already contains a property or link with name "+l.getName());
		propertyNames.add(l.getName());
		links.add(l);
	}
		
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	public String getName(boolean multiple){
		return multiple? getMultiple(): getName();
	}

	/**
	 * Returns all contained properties.
	 */
	public List<MetaProperty> getProperties() {
		return properties;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	@Override public String toString(){
		return "D "+name+" "+properties;
	}
	
	public String getListName(){
		return "LIST_"+getName().toUpperCase();
	}
	
	public String getListConstantValue(){
		return "list_"+getName().toLowerCase();
	}
	
	public String getMultiple(){
		return getName()+"s";
	}
	
	public String getTemporaryVariableName(){
		return "tmp_"+getVariableName();
	}
	
	/**
	 * Returns the name of the variable to use in generated code for variables of this documents type.
	 * @return
	 */
	public String getVariableName(){
		if (getName().length()<3)
			return ""+getName().toLowerCase().charAt(0);
		String vName = getName().toLowerCase();
		if (vName.equals("new") ||
			vName.equals("int")
		)
			vName = "_"+vName;
		 
		return vName;
	}
	/**
	 * Returns the links.
	 * @return
	 */
	public List<MetaProperty> getLinks(){
		return links;
	}
	/**
	 * Return the name constant for the id holder for this document for cms storage.
	 * @return
	 */
	public String getIdHolderName(){
		return "ID_HOLDER_"+getName().toUpperCase();
	}
	
	/**
	 * Returns a field with given name. This can be a virtual field for the presentation (id, plainId, documentLastUpdateTimestamp, multilingualInstanceDisabled), a 
	 * MetaProperty or a MetaLink. If nothing is found an IllegalArgumentException is thrown.
	 * @param name name of the field.
	 * @return
	 */
	public MetaProperty getField(String name){
		
		if (name.equals("id"))
			return new MetaProperty("id",MetaProperty.Type.STRING);
		
		if (name.equals("plainId"))
			return new MetaProperty("plainId",MetaProperty.Type.STRING);

		if (name.equals("documentLastUpdateTimestamp"))
			return new MetaProperty("documentLastUpdateTimestamp",MetaProperty.Type.STRING);
		
		if (name.equals("multilingualInstanceDisabled"))
			return new MetaProperty("multilingualInstanceDisabled", MetaProperty.Type.BOOLEAN);

		for (MetaProperty p : properties)
			if (p.getName().equals(name))
				return p; 
		


		for (MetaProperty p :  links)
			if (p.getName().equals(name))
				return p; 
		
		
		throw new IllegalArgumentException("No such field: "+name+" in document "+getFullName());
	}
	
	/**
	 * Returns true if the document contains at list one comparable property or link.
	 * TODO (leon) I have serious doubts whether this method does what its intended to do.
	 * @return
	 */
	public boolean isComparable(){
		for (int i=0; i<properties.size(); i++){
			if (! (properties.get(i) instanceof MetaContainerProperty))
				return true;
		}

		for (int i=0; i<links.size(); i++){
			if (! (links.get(i) instanceof MetaContainerProperty))
				return true;
		}

		return false;
	}


	public String getFullName(){
		return getParentModule() == null ? 
				"?."+getName() : getParentModule().getName()+"."+getName();
	}

	public MetaModule getParentModule() {
		return parentModule;
	}



	public void setParentModule(MetaModule parentModule) {
		this.parentModule = parentModule;
	}
	
	/**
	 * Returns all internal links to the given document if any. This is useful if you need to find out at generation time whether one document is linked to another document.
	 * @param anotherDocument the target document.
	 * @return list of MetaLinks to the targetDocument or an empty list.
	 */
	public List<MetaLink> getLinksToDocument(MetaDocument anotherDocument){
		List<MetaLink> ret = new ArrayList<MetaLink>();
		for (MetaProperty p : links){
			MetaLink l = (MetaLink)p;
			if (l.doesTargetMatch(anotherDocument))
				ret.add(l);
		}
		return ret;
	}
	
	/**
	 * Returns true if at least one of the properties or links are multilingual, false otherwise.
	 * @return
	 */
	public boolean isMultilingual(){
		for (MetaProperty p : properties){
			if (p.isMultilingual())
				return true;
		}
		for (MetaProperty p : links){
			if (p.isMultilingual())
				return true;
		}
		return false;
	}
	

}
