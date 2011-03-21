package net.anotheria.anodoc.query2.string;

import static org.junit.Assert.assertEquals;

import java.util.List;

import net.anotheria.anodoc.data.StringProperty;
import net.anotheria.anodoc.query2.DocumentQuery;
import net.anotheria.anodoc.query2.QueryResultEntry;

import org.junit.Test;

public class ContainsWordsQueryTest {

	@Test public void testMatchSingleWord() {

		String criteria = "Anotheria software is";

		DataObjectAdapter doc = new DataObjectAdapter("2010");
		doc.putStringProperty(new StringProperty("property1", "This is Anotheria software"));
		doc.putStringProperty(new StringProperty("property2", "This is to loooooooooooooooooooooooooooooooooooooooooooooooooong Anotheria soooooooooooooooooooooooooooooooooooooooooooftware"));
		doc.putStringProperty(new StringProperty("property3", "This is Naotheria software"));
		doc.putStringProperty(new StringProperty("property4", "Anotheria Develment"));
		doc.putStringProperty(new StringProperty("property5", "This is Anotheria"));
		doc.putStringProperty(new StringProperty("property6", "Anotheria"));

		assertEquals(7, doc.getProperties().size());

		DocumentQuery query = new ContainsWordsQuery(criteria);
		List<QueryResultEntry> result = query.match(doc);
		assertEquals(1, result.size());
		
		query = new ContainsWordsQuery(criteria, "property1");
		result = query.match(doc);
		assertEquals(1, result.size());
//		StringMatchingInfo info = (StringMatchingInfo) result.get(0).getInfo();
//		assertEquals(criteria, info.getMatch());
//		assertEquals("This is ", info.getPre());
//		assertEquals(" software", info.getPost());
		
//		query = new ContainsWordsQuery(criteria, "property2");
//		result = query.match(doc);
//		assertEquals(1, result.size());
//		info = (StringMatchingInfo) result.get(0).getInfo();
//		assertEquals(criteria, info.getMatch());
//		assertEquals("ooooooooooooooooooooooooooooooooooooong ", info.getPre());
//		assertEquals(" soooooooooooooooooooooooooooooooooooooo", info.getPost());
//		
//		query = new ContainsWordsQuery(criteria, "property3");
//		result = query.match(doc);
//		assertEquals(0, result.size());
//		
//		query = new ContainsWordsQuery(criteria, "property4");
//		result = query.match(doc);
//		assertEquals(1, result.size());
//		info = (StringMatchingInfo) result.get(0).getInfo();
//		assertEquals(criteria, info.getMatch());
//		assertEquals("", info.getPre());
//		assertEquals(" Develment", info.getPost());
//		
//		query = new ContainsWordsQuery(criteria, "property5");
//		result = query.match(doc);
//		assertEquals(1, result.size());
//		info = (StringMatchingInfo) result.get(0).getInfo();
//		assertEquals(criteria, info.getMatch());
//		assertEquals("This is ", info.getPre());
//		assertEquals("", info.getPost());
//		
//		query = new ContainsWordsQuery(criteria, "property6");
//		result = query.match(doc);
//		assertEquals(1, result.size());
//		info = (StringMatchingInfo) result.get(0).getInfo();
//		assertEquals(criteria, info.getMatch());
//		assertEquals("", info.getPre());
//		assertEquals("", info.getPost());
	}
	
	@Test public void testMatchMultiWords() {

		String criteria = "Anotheria Software";

		DataObjectAdapter doc = new DataObjectAdapter("2010");
		doc.putStringProperty(new StringProperty("property1", "This is Anotheria Company:  Software Develment"));
		doc.putStringProperty(new StringProperty("property2", "This isssssssssssssssssssssssssssssssssssssssssss Anotheria Company:  Software Deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeevelment"));
		doc.putStringProperty(new StringProperty("property3", "This is Naotheria Software"));
		doc.putStringProperty(new StringProperty("property4", "Anotheria Company:  Software Develment"));
		doc.putStringProperty(new StringProperty("property5", "This is Anotheria Company:  Software"));
		doc.putStringProperty(new StringProperty("property6", "Anotheria Company:  Software"));

		assertEquals(7, doc.getProperties().size());

		DocumentQuery query = new ContainsWordsQuery(criteria);
		List<QueryResultEntry> result = query.match(doc);
		assertEquals(1, result.size());
//
//		query = new ContainsWordsQuery(criteria, "property1");
//		result = query.match(doc);
//		assertEquals(1, result.size());
//		StringMatchingInfo info = (StringMatchingInfo) result.get(0).getInfo();
//		assertEquals("Anotheria Company:  Software", info.getMatch());
//		assertEquals("This is ", info.getPre());
//		assertEquals(" Develment", info.getPost());
//		
//		query = new ContainsWordsQuery(criteria, "property2");
//		result = query.match(doc);
//		assertEquals(1, result.size());
//		info = (StringMatchingInfo) result.get(0).getInfo();
//		assertEquals("Anotheria Company:  Software", info.getMatch());
//		assertEquals("sssssssssssssssssssssssssssssssssssssss ", info.getPre());
//		assertEquals(" Deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeevelm", info.getPost());
////		
//		query = new ContainsWordsQuery(criteria, "property3");
//		result = query.match(doc);
//		assertEquals(0, result.size());
//		
//		query = new ContainsWordsQuery(criteria, "property4");
//		result = query.match(doc);
//		assertEquals(1, result.size());
//		info = (StringMatchingInfo) result.get(0).getInfo();
//		assertEquals("Anotheria Company:  Software", info.getMatch());
//		assertEquals("", info.getPre());
//		assertEquals(" Develment", info.getPost());
//		
//		query = new ContainsWordsQuery(criteria, "property5");
//		result = query.match(doc);
//		assertEquals(1, result.size());
//		info = (StringMatchingInfo) result.get(0).getInfo();
//		assertEquals("Anotheria Company:  Software", info.getMatch());
//		assertEquals("This is ", info.getPre());
//		assertEquals("", info.getPost());
//		
//		query = new ContainsWordsQuery(criteria, "property6");
//		result = query.match(doc);
//		assertEquals(1, result.size());
//		info = (StringMatchingInfo) result.get(0).getInfo();
//		assertEquals("Anotheria Company:  Software", info.getMatch());
//		assertEquals("", info.getPre());
//		assertEquals("", info.getPost());
	}

}
