package net.anotheria.asg.generator.meta;
/**
 * Represents a document mapping in the federation.
 * @author lrosenberg
 *
 */
public class FederatedDocumentMapping {
	/**
	 * The source document name.
	 */
	private String sourceDocument;
	/**
	 * The target document.
	 */
	private String targetDocument;
	/**
	 * The key used as key for the target document.
	 */
	private String targetKey;
	public String getSourceDocument() {
		return sourceDocument;
	}
	public void setSourceDocument(String sourceDocument) {
		this.sourceDocument = sourceDocument;
	}
	public String getTargetDocument() {
		return targetDocument;
	}
	public void setTargetDocument(String targetDocument) {
		this.targetDocument = targetDocument;
	}
	public String getTargetKey() {
		return targetKey;
	}
	public void setTargetKey(String targetKey) {
		this.targetKey = targetKey;
	}
	
	@Override public String toString(){
		return sourceDocument+" -> "+targetKey+"."+targetDocument;
	}
}
