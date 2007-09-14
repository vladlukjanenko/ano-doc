package net.anotheria.asg.generator.meta;

public class FederatedDocumentMapping {
	private String sourceDocument;
	private String targetDocument;
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
	
	public String toString(){
		return sourceDocument+" -> "+targetKey+"."+targetDocument;
	}
}
