package net.anotheria.anodoc.query2;

/**
 * A bean which contains one result entry. 
 * @author lrosenberg
 */
public class ResultEntryBean {
	/**
	 * The link for document edit dialog.
	 */
	private String editLink;
	/**
	 * The id of the document/object which was found.
	 */
	private String documentId;
	/**
	 * The name of the document/object which was found.
	 */
	private String documentName;
	/**
	 * The name of the property which matched.
	 */
	private String propertyName;
	/**
	 * The matching info (additional information).
	 */
	private String info;
	
	public String getDocumentId() {
		return documentId;
	}
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	public String getEditLink() {
		return editLink;
	}
	public void setEditLink(String editLink) {
		this.editLink = editLink;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}
	public String getDocumentName() {
		return documentName;
	}
	
	
}
