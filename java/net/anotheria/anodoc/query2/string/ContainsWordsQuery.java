package net.anotheria.anodoc.query2.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.Property;
import net.anotheria.anodoc.query2.DocumentQuery;
import net.anotheria.anodoc.query2.QueryResultEntry;
import net.anotheria.asg.data.DataObject;
import net.anotheria.util.StringUtils;

public class ContainsWordsQuery implements DocumentQuery{

	public static final int OFFSET = 40;
	
	private static final Logger log = Logger.getLogger(ContainsWordsQuery.class);

	private String[] criteria;
	private Set<String> propertiesToSearch = Collections.emptySet();
	
	public ContainsWordsQuery(String aCriteria){
		this(aCriteria, new String[]{});
	}
	
	public ContainsWordsQuery(String aCriteria, String... aPropertiesToSearch){
		criteria = StringUtils.tokenize(aCriteria.toLowerCase(),' ');
		propertiesToSearch = new HashSet<String>();
		for(String prop: aPropertiesToSearch)
			propertiesToSearch.add(prop);
	}
	
  
	public List<QueryResultEntry> match(DataObject obj) {
		log.debug("Match DataObject  " + obj.getDefinedName() + " with ID " + obj.getId() + "." + this);
		List<QueryResultEntry> ret = new ArrayList<QueryResultEntry>();
		if (!(obj instanceof Document))
			throw new AssertionError("Supports only search in a Document instance!");
		Document doc = (Document)obj;
		List<Property> properties = doc.getProperties();
			
		for (Property p: properties){
			QueryResultEntry entry = matchProperty(doc, p);
			if(entry != null)
				ret.add(entry);
		}
		return ret;
	}

	private QueryResultEntry matchProperty(Document doc, Property p){
		//If is not property to search then check next property. Empty properties to search is any.
		if(propertiesToSearch.size() > 0 && !propertiesToSearch.contains(p.getId()))
			return null;
		
		if(p == null || p.getValue() == null){
			log.debug("Wrong property: " + p);
			return null;
		}
		
		String value = p.getValue().toString();
		log.trace("Value: " + value);
		
		int preIndex = Integer.MAX_VALUE;
		int postIndex = -1;
		for(String c:criteria){
			int i = value.toLowerCase().indexOf(c);
			if(i == -1)
				//criteria not found!
				return null;
			postIndex = Math.max(postIndex, i + c.length());
			preIndex = Math.min(preIndex, i);
		}
		
		QueryResultEntry e = new QueryResultEntry();
		e.setMatchedDocument(doc);
		e.setMatchedProperty(p);
		
		String pre = value.substring(Math.max(0, preIndex - OFFSET), preIndex);
		String post = value.substring(postIndex, Math.min(value.length(), postIndex + OFFSET));
		
		e.setInfo(new StringMatchingInfo(pre, value.substring(preIndex, postIndex), post));
		
		return e;
	}
	
	@Override
	public String toString() {
		return "ContainsAllQuery [criteria=" + Arrays.toString(criteria) + ", propertiesToSearch=" + propertiesToSearch + "]";
	}
	
}
