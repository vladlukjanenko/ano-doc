package net.anotheria.anodoc.query2.string;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.Property;
import net.anotheria.anodoc.data.StringProperty;
import net.anotheria.anodoc.query2.DocumentQuery;
import net.anotheria.anodoc.query2.QueryResultEntry;
import net.anotheria.asg.data.DataObject;
import net.anotheria.util.IOUtils;
import net.anotheria.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ContainsWordsQuery implements DocumentQuery{

	/**
	 * {@link Logger} instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ContainsWordsQuery.class);

	public static class Index{
		Map<String, Occurrenece> index = new HashMap<String, ContainsWordsQuery.Occurrenece>();
		int size = 0;
		
		public void indexWord(String word, int position){
			if(index.containsKey(word))
				index.get(word).addOccurrence(position);
			else
				index.put(word, new Occurrenece(position));
			size = Math.max(size, position);
		}
		
		public boolean containsWord(String word){
			return index.containsKey(word);
		}
		
		public Occurrenece getWordOccuerrence(String word){
			return index.get(word);
		}
		
		public String rebuildText(){
			StringBuilder builder = new StringBuilder();
			for(int i = 1; i <= size; i++){
				for(String word: index.keySet()){
					Occurrenece occurrenece = index.get(word);
					if(occurrenece.containsPosistion(i))
						builder.append(word).append(" ");
				}
			}
			return builder.toString();
		}
		
		@Override
		public String toString() {
			return "Index [size=" + size + ", index=" + index + "]";
		}
	}
	
	private static class Occurrenece{
		private Set<Integer> positions = new HashSet<Integer>();

		public Occurrenece(int aFirstPosition){
			positions.add(aFirstPosition);
		}
		
		
		public void addOccurrence(int position){
			positions.add(position);
		}

		public boolean containsPosistion(int position){
			return positions.contains(position);
		}

		@Override
		public String toString() {
			return "Occurrenece [positions=" + positions + "]";
		}
		
	}
	
	public static final Map<String, String> WORDS_SEPARATORS = new HashMap<String, String>();
	static{
		WORDS_SEPARATORS.put("\n", " ");
		WORDS_SEPARATORS.put("\t", " ");
		WORDS_SEPARATORS.put(".", " ");
		WORDS_SEPARATORS.put(",", " ");
		WORDS_SEPARATORS.put("!", " ");
		WORDS_SEPARATORS.put("?", " ");
		WORDS_SEPARATORS.put("-", " ");
		WORDS_SEPARATORS.put("_", " ");
	}
	

	private String[] criteria;
	private Set<String> propertiesToSearch = Collections.emptySet();
	
	public ContainsWordsQuery(String aCriteria){
		this(aCriteria, new String[]{});
	}
	
	public ContainsWordsQuery(String aCriteria, String... aPropertiesToSearch){
		aCriteria = aCriteria.toLowerCase().trim();
		aCriteria = StringUtils.replace(aCriteria, WORDS_SEPARATORS);
		criteria = StringUtils.tokenize(aCriteria,' ');
		propertiesToSearch = new HashSet<String>();
		for(String prop: aPropertiesToSearch)
			propertiesToSearch.add(prop);
	}
	
  
	public List<QueryResultEntry> match(DataObject obj) {
		LOGGER.debug("Match DataObject  " + obj.getDefinedName() + " with ID " + obj.getId() + "." + this);
		if (!(obj instanceof Document))
			throw new AssertionError("Supports only search in a Document instance!");
		return match((Document)obj);
	}
	
	public List<QueryResultEntry> match(Document doc) {
		List<QueryResultEntry> ret = new ArrayList<QueryResultEntry>();
		List<Property> properties = doc.getProperties();
		
		Set<String> matchRegression = new HashSet<String>(Arrays.asList(criteria));
		int i = 0;
		for (Property p: properties){
			i++;
			Index propertyIndex = buildPropertyIndex(p);
			Set<String> toRemove = new HashSet<String>();
			for(String match: matchRegression)
				if(propertyIndex.containsWord(match))
					toRemove.add(match);
			matchRegression.removeAll(toRemove);
			
			if(matchRegression.isEmpty()){
				QueryResultEntry res = new QueryResultEntry();
				res.setMatchedDocument(doc);
				res.setMatchedProperty(p);
				//TODO: sophisticated relevance calculation
				res.setRelevance(100/i);
				ret.add(res);
				return ret;
			}
		}
		return ret;
	}
	
	private Index buildPropertyIndex(Property p){
		Index ret = new Index();
		String valueStr = p.getValue().toString().toLowerCase().trim();
		if(StringUtils.isEmpty(valueStr))
			return ret;
		
		valueStr = StringUtils.removeChar(valueStr, '\r');
		valueStr = StringUtils.replace(valueStr, WORDS_SEPARATORS);
		String[] wordTokens = StringUtils.tokenize(valueStr, true, ' ');
		
		for(int i = 0; i < wordTokens.length; i++){
			String word = wordTokens[i];
			ret.indexWord(word, i + 1);
		}
		
		return ret;
	}
	
	@Override
	public String toString() {
		return "ContainsAllQuery [criteria=" + Arrays.toString(criteria) + ", propertiesToSearch=" + propertiesToSearch + "]";
	}

	//TODO: write Unit Tests
	public static void main(String[] args) throws Exception{
		ContainsWordsQuery query = new ContainsWordsQuery("search query indexing matrix Ноутбук");
		
		Document doc = new Document("sample");

		String sample = IOUtils.readInputStreamBufferedAsString(query.getClass().getClassLoader().getResourceAsStream("sampletext.txt"), "utf-8");
		StringProperty p = new StringProperty("sampleProp1");
		p.setValue(sample);
		doc.putStringProperty(p);
		
		sample = IOUtils.readInputStreamBufferedAsString(query.getClass().getClassLoader().getResourceAsStream("sampletext2.txt"), "utf-8");
		p = new StringProperty("sampleProp2");
		p.setValue(sample);
		doc.putStringProperty(p);
		
		p = new StringProperty("sampleProp3");
		p.setValue(null);
		doc.putStringProperty(p);
		
		System.out.println("** RESULT: **");
		List<QueryResultEntry> result = query.match(doc);
		System.out.println(result);
	}
	
}
