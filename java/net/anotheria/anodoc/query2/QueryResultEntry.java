package net.anotheria.anodoc.query2;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.Property;

/**
 * An entry in a query result list.
 * @author lrosenberg
 *
 */
public class QueryResultEntry {
	/**
	 * Link to the matched document.
	 */
	private Document matchedDocument;
	/**
	 * Link to the matched property.
	 */
	private Property matchedProperty;
	/**
	 * Info what matched and how.
	 */
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
	
	@Override public String toString(){
		return "Doc: "+getMatchedDocument()+", prop: "+getMatchedProperty()+", matchinfo: "+getInfo();
	}
}
