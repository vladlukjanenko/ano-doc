package net.anotheria.anodoc.query2.string;

import java.util.ArrayList;
import java.util.Arrays;
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
import net.anotheria.util.StringUtils;

public class ContainsWordsQuery implements DocumentQuery{
	
	public static final int OFFSET = 40;

	private String[] criteria;
	private Set<String> propertiesToSearch = Collections.emptySet();
	private static final Set<String> emptyProperties = Collections.emptySet();
	
	public ContainsWordsQuery(String aCriteria){
		this(aCriteria, null);
	}
	
	public ContainsWordsQuery(String aCriteria, Collection<String> aPropertiesToSearch){
		criteria = StringUtils.tokenize(aCriteria.toLowerCase(),' ');
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
			System.out.println("Value: " + value);
			
			int preIndex = Integer.MAX_VALUE;
			int postIndex = -1;
			for(String c:criteria){
				int i = value.toLowerCase().indexOf(c);
				if(i == -1){
					System.out.println("Criteria " + c + " not found!");
					preIndex = -1;
					postIndex = -1;
					break;
				}
				postIndex = Math.max(postIndex, i + c.length());
				preIndex = Math.min(preIndex, i);
			}
			
			//If property doesn't contains criteria then check next property 
			if (postIndex ==-1)
				continue;
			System.out.println("PreIndex: " + preIndex);
			System.out.println("PostIndex: " + postIndex);
			
			System.out.println("");
			
			QueryResultEntry e = new QueryResultEntry();
			e.setMatchedDocument(doc);
			e.setMatchedProperty(p);
			
			String pre = value.substring(0, preIndex);
			if(pre.length() > OFFSET)
				pre = pre.substring(pre.length() - OFFSET, pre.length());
				
			String post = value.substring(postIndex, value.length());
			if(post.length() > OFFSET)
				post = post.substring(0, OFFSET);
			
			e.setInfo(new StringMatchingInfo(pre, value.substring(preIndex, postIndex), post));
			ret.add(e);
		}
		return ret;
	}

	@Override
	public String toString() {
		return "ContainsAllQuery [criteria=" + Arrays.toString(criteria) + ", propertiesToSearch=" + propertiesToSearch + "]";
	}
	

}
