package net.anotheria.anodoc.query2;

import java.util.ArrayList;
import java.util.List;

public class QueryResult {
	private List<QueryResultEntry> entries;
	
	public QueryResult(){
		entries = new ArrayList<QueryResultEntry>();
	}
	
	public void add(QueryResultEntry entry){
		entries.add(entry);
	}
	
	public void add(List<QueryResultEntry> someEntries){
		entries.addAll(someEntries);
	}
	
	public List<QueryResultEntry> getEntries(){
		return entries;
	}
	
	public String toString(){
		return "QueryResult with "+entries.size()+" entries: \n"+entries;
	}
}
