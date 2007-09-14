package net.anotheria.anodoc.query2.string;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.Property;
import net.anotheria.anodoc.data.StringProperty;
import net.anotheria.anodoc.query2.DocumentQuery;
import net.anotheria.anodoc.query2.QueryResultEntry;
import net.anotheria.asg.data.DataObject;

public class ContainsStringQuery implements DocumentQuery{
	
	public static final int OFFSET = 40;
	
	private String criteria;
	
	public ContainsStringQuery(String aCriteria){
		criteria = aCriteria;
	}
  
	public List<QueryResultEntry> match(DataObject obj) {
		
		List<QueryResultEntry> ret = new ArrayList<QueryResultEntry>();
		if (!(obj instanceof Document))
			return ret;
		Document doc = (Document)obj;
		List<Property> properties = doc.getProperties();
		for (int i=0; i<properties.size(); i++){
			Property p = properties.get(i);
			if (p instanceof StringProperty){
				String value = ((StringProperty)p).getString();
				int index = value.toLowerCase().indexOf(criteria.toLowerCase());
				if (index!=-1){
					QueryResultEntry e = new QueryResultEntry();
					e.setMatchedDocument(doc);
					e.setMatchedProperty(p);
					
					String pre = null;
					if (index==0){
						pre = "";
					}else{
						int preStart = index-OFFSET;
						if (preStart<0)
							preStart = 0;
						pre = value.substring(preStart, index);
						//pre = "["+(index-preStart)+", "+index+", "+preStart+"] "+pre;
					}
					
					String post = null;
					int length = criteria.length();
					if (index+length>=value.length()){
						post = "";
					}else{
						int postStart = index+length;
						int postEnd = index+length+OFFSET;
						if (postEnd>=value.length())
							postEnd = value.length()-1;
						post = value.substring(postStart, postEnd);
						//post += "["+postStart+", "+postEnd+", "+(postEnd-postStart)+"]";
					}
					
					StringMatchingInfo info = new StringMatchingInfo(pre, value.substring(index, index+criteria.length()), post); 
					e.setInfo(info);
					ret.add(e);
				}
			}
		}
		return ret;
	}
	
	public String toString(){
		return criteria;
	}

}
