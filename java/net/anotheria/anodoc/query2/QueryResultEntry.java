package net.anotheria.anodoc.query2;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.Property;
import net.anotheria.util.BasicComparable;
import net.anotheria.util.sorter.IComparable;

/**
 * An entry in a query result list.
 * @author lrosenberg
 *
 */
public class QueryResultEntry implements IComparable{
	/**
	 * Link to the matched document.
	 */
	private Document matchedDocument;
	/**
	 * Link to the matched property.
	 */
	private Property matchedProperty;
	
	private int relevance;
	
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
	
	public int getRelevance() {
		return relevance;
	}
	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}
	
	
	@Override public String toString(){
		return "Rel:" + relevance + ", doc: "+getMatchedDocument()+", prop: "+getMatchedProperty()+", matchinfo: "+getInfo();
	}
	@Override
	public int compareTo(IComparable anotherObject, int method) {
		QueryResultEntry anotherEntry = (QueryResultEntry)anotherObject;
		return BasicComparable.compareInt(relevance, anotherEntry.relevance);
	}
}
