package net.anotheria.asg.util.bean;

import net.anotheria.anodoc.data.NoSuchPropertyException;
import net.anotheria.asg.data.DataObject;
import net.anotheria.util.StringUtils;

/**
 * A bean which represents an incoming link to a document (as a result of a query). 
 * @author lrosenberg
 */
public class LinkToMeBean {
	/**
	 * The type of the linking document.
	 */
	private String targetDocumentType;
	/**
	 * The id of the linking document.
	 */
	private String targetDocumentId;
	/**
	 * A description of the linking document.
	 */
	private String targetDocumentDescription;
	/**
	 * The linking property.
	 */
	private String targetDocumentProperty;
	/**
	 * A link for the edit tool for the linking document.
	 */
	private String targetDocumentLink;
	
	public LinkToMeBean(){
		
	}
	
	public LinkToMeBean(DataObject doc, String propertyName){
		targetDocumentType = doc.getDefinedName();
		targetDocumentId = doc.getId();
		targetDocumentLink = doc.getDefinedParentName().toLowerCase()+StringUtils.capitalize(doc.getDefinedName())+"Edit?ts="+System.currentTimeMillis()+"&pId="+doc.getId();
		targetDocumentProperty = propertyName;
		try{
			targetDocumentDescription = ""+doc.getPropertyValue("name");
		}catch(NoSuchPropertyException e){
			
		}catch(RuntimeException e){
			//temporarly, as long as VO objects are throwing exceptions of this type instead of something meaningful.
		}
	}
	
	public boolean isDescriptionAvailable(){
		return targetDocumentDescription!=null && targetDocumentDescription.length()>0;
	}
	
	public String getTargetDocumentType() {
		return targetDocumentType;
	}
	public void setTargetDocumentType(String targetDocumentType) {
		this.targetDocumentType = targetDocumentType;
	}
	public String getTargetDocumentId() {
		return targetDocumentId;
	}
	public void setTargetDocumentId(String targetDocumentId) {
		this.targetDocumentId = targetDocumentId;
	}
	public String getTargetDocumentDescription() {
		return targetDocumentDescription;
	}
	public void setTargetDocumentDescription(String targetDocumentDescription) {
		this.targetDocumentDescription = targetDocumentDescription;
	}
	public String getTargetDocumentProperty() {
		return targetDocumentProperty;
	}
	public void setTargetDocumentProperty(String targetDocumentProperty) {
		this.targetDocumentProperty = targetDocumentProperty;
	}
	public String getTargetDocumentLink() {
		return targetDocumentLink;
	}
	public void setTargetDocumentLink(String targetDocumentLink) {
		this.targetDocumentLink = targetDocumentLink;
	}
	
	
}
