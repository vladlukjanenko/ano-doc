package net.anotheria.anodoc.query2;

import java.util.ArrayList;
import java.util.List;
/**
 * The result of the query.
 * @author lrosenberg
 */
public class QueryResult {
	/**
	 * Entries which are part of the QueryResult.
	 */
	private List<QueryResultEntry> entries;
	/**
	 * Creates a new query result.
	 */
	public QueryResult(){
		entries = new ArrayList<QueryResultEntry>();
	}
	
	/**
	 * Adds a result entry.
	 * @param entry
	 */
	public void add(QueryResultEntry entry){
		entries.add(entry);
	}
	/**
	 * Adds some result entries.
	 * @param someEntries
	 */
	public void add(List<QueryResultEntry> someEntries){
		entries.addAll(someEntries);
	}
	/**
	 * Returns the entries.
	 * @return
	 */
	public List<QueryResultEntry> getEntries(){
		return entries;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override public String toString(){
		return "QueryResult with "+entries.size()+" entries: \n"+entries;
	}
}
