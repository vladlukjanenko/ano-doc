package net.anotheria.anodoc.query2.string;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.Property;
import net.anotheria.anodoc.query2.DocumentQuery;
import net.anotheria.anodoc.query2.QueryResultEntry;
import net.anotheria.asg.data.DataObject;

public class SimpleContainsStringQuery implements DocumentQuery{
	
	public static final int OFFSET = 40;

	private String criteria;
	private Set<String> propertiesToSearch = Collections.emptySet();
	
	public SimpleContainsStringQuery(String aCriteria){
		this(aCriteria, null);
	}
	
	public SimpleContainsStringQuery(String aCriteria, Collection<String> aPropertiesToSearch){
		criteria = aCriteria.toLowerCase();
		Set<String> emptyProperties = Collections.emptySet();
		propertiesToSearch = aPropertiesToSearch != null? new HashSet<String>(aPropertiesToSearch): emptyProperties;
	}
  
	public List<QueryResultEntry> match(DataObject obj) {
		
		List<QueryResultEntry> ret = new ArrayList<QueryResultEntry>();
		if (!(obj instanceof Document))
			throw new AssertionError("Supports only search in a Document instance!");
		Document doc = (Document)obj;
		List<Property> properties = doc.getProperties();
			
		for (Property p: properties){
			//If is not property to search then check next property. Empty properties to search is any.
			if(propertiesToSearch.size() > 0 && !propertiesToSearch.contains(p.getId()))
				continue;
			String value = p.getValue().toString();
			int index = value.toLowerCase().indexOf(criteria);
			//If property doesn't contains criteria then check next property 
			if (index ==-1)
				continue;
			QueryResultEntry e = new QueryResultEntry();
			e.setMatchedDocument(doc);
			e.setMatchedProperty(p);
			
			String pre = value.substring(0, index);
			if(pre.length() > OFFSET)
				pre = pre.substring(pre.length() - OFFSET, pre.length());
				
			String post = value.substring(index + criteria.length(), value.length());
			if(post.length() > OFFSET)
				post = post.substring(0, OFFSET);
			
			e.setInfo(new StringMatchingInfo(pre, value.substring(index, index+criteria.length()), post));
			ret.add(e);
		}
		return ret;
	}
	
	public String toString(){
		return criteria;
	}

}
