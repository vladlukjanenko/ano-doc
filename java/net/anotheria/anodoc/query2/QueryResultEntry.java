package net.anotheria.anodoc.query2;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.Property;

public class QueryResultEntry {
	private Document matchedDocument;
	private Property matchedProperty;
	private MatchingInfo info;
	public MatchingInfo getInfo() {
		return info;
	}
	public void setInfo(MatchingInfo info) {
		this.info = info;
	}
	public Document getMatchedDocument() {
		return matchedDocument;
	}
	public void setMatchedDocument(Document matchedDocument) {
		this.matchedDocument = matchedDocument;
	}
	public Property getMatchedProperty() {
		return matchedProperty;
	}
	public void setMatchedProperty(Property matchedProperty) {
		this.matchedProperty = matchedProperty;
	}
	
	public String toString(){
		return "Doc: "+getMatchedDocument()+", prop: "+getMatchedProperty()+", matchinfo: "+getInfo();
	}
}
