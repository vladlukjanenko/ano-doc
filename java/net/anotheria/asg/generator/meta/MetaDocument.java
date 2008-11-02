package net.anotheria.asg.generator.meta;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.IGenerateable;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaDocument implements IGenerateable{
	
	private String name;
	private List<MetaProperty> properties;
	private List<MetaProperty> links;
	
	private MetaModule parentModule;
	
	public MetaDocument(String aName){
		name = aName;
		properties = new ArrayList<MetaProperty>();
		links = new ArrayList<MetaProperty>();
	}
	
	
	
	public void addProperty(MetaProperty p){
		properties.add(p);
	}
	
	public void addLink(MetaLink l){
		links.add(l);
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
	public List<MetaProperty> getProperties() {
		return properties;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param list
	 */
	public void setProperties(List<MetaProperty> list) {
		properties = list;
	}
	
	public String toString(){
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
	
	public String getVariableName(){
		if (getName().length()<3)
			return ""+getName().toLowerCase().charAt(0);
		String vName = getName().toLowerCase()/*.substring(0,3)*/;
		if (vName.equals("new") ||
			vName.equals("int")
		)
			vName = "_"+vName;
		 
		return vName;
	}
	
	public List<MetaProperty> getLinks(){
		return links;
	}
	
	public String getIdHolderName(){
		return "ID_HOLDER_"+getName().toUpperCase();
	}
	
	public MetaProperty getField(String name){
		
		if (name.equals("id"))
			return new MetaProperty("id","string");
		
		if (name.equals("plainId"))
			return new MetaProperty("plainId","string");

		if (name.equals("documentLastUpdateTimestamp"))
			return new MetaProperty("documentLastUpdateTimestamp","string");
		
		if (name.equals("multilingualInstanceDisabled"))
			return new MetaProperty("multilingualInstanceDisabled", "boolean");

		for (MetaProperty p : properties)
			if (p.getName().equals(name))
				return p; 
		


		for (MetaProperty p :  links)
			if (p.getName().equals(name))
				return p; 
		
		
		throw new RuntimeException("No such field: "+name+" in document "+getFullName());
	}
	
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
	
	public List<MetaLink> getLinksToDocument(MetaDocument anotherDocument){
		List<MetaLink> ret = new ArrayList<MetaLink>();
		for (MetaProperty p : links){
			MetaLink l = (MetaLink)p;
			if (l.doesTargetMatch(anotherDocument))
				ret.add(l);
		}
		return ret;
	}
	
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
