package net.anotheria.asg.generator.meta;

import org.junit.Test;
import static junit.framework.Assert.*;

public class MetaDocumentTest {
	@Test public void testPropertyUniqueness(){
		MetaDocument d = new MetaDocument("test");
		
		d.addProperty(new MetaProperty("a", MetaProperty.Type.STRING));
		d.addLink(new MetaLink("b"));
		assertEquals(1, d.getProperties().size());
		assertEquals(1, d.getLinks().size());
		
		String str = d.toString();

		try{
			d.addProperty(new MetaProperty("a", MetaProperty.Type.STRING));
			fail("exception expected");
		}catch(IllegalArgumentException e){}
		assertEquals(1, d.getProperties().size());
		assertEquals(1, d.getLinks().size());
		assertEquals(str, d.toString());

		try{
			d.addLink(new MetaLink("a"));
			fail("exception expected");
		}catch(IllegalArgumentException e){}
		assertEquals(1, d.getProperties().size());
		assertEquals(1, d.getLinks().size());
		assertEquals("String representation of the object should remain the same", str, d.toString());
	}
	
	@Test public void testFullName(){
		MetaDocument d = new MetaDocument("Test");
		assertEquals("?.Test", d.getFullName());
		d.setParentModule(new MetaModule("Test"));
		assertEquals("Test.Test", d.getFullName());
	}
	
	@Test public void testMultilinguality(){
		MetaDocument d = new MetaDocument("Test");
		assertFalse(d.isMultilingual());
		d.addProperty(new MetaProperty("a",MetaProperty.Type.STRING));
		assertFalse(d.isMultilingual());
		MetaProperty p = new MetaProperty("b", MetaProperty.Type.STRING);
		p.setMultilingual(true);
		d.addProperty(p);
		assertTrue(d.isMultilingual());
		
		d = new MetaDocument("AnotherTest");
		assertFalse(d.isMultilingual());
		d.addLink(new MetaLink("a"));
		assertFalse(d.isMultilingual());
		MetaLink l = new MetaLink("b");
		l.setMultilingual(true);
		d.addLink(l);
		assertTrue(d.isMultilingual());
	}
	
	@Test public void testHardcodedFields(){
		MetaDocument d = new MetaDocument("x");
		assertNotNull(d.getField("id"));
		assertNotNull(d.getField("plainId"));
		assertNotNull(d.getField("documentLastUpdateTimestamp"));
		assertNotNull(d.getField("multilingualInstanceDisabled"));
		try{
			d.getField("none");
			fail("Unknown fields should lead to exception.");
		}catch(Exception e){}
	}
	
	@Test public void testGetField(){
		MetaProperty p = new MetaProperty("property", MetaProperty.Type.STRING);
		MetaLink l = new MetaLink("link");
		MetaDocument d = new MetaDocument("Document");
		try{
			d.getField("property");
			fail("property doesn't exist, should throw an exception");
		}catch(Exception e){}
		try{
			d.getField("link");
			fail("link doesn't exist, should throw an exception");
		}catch(Exception e){}
		
		d.addLink(l);
		d.addProperty(p);
		
		assertNotNull(d.getField("property"));
		assertEquals("property", d.getField("property").getName());

		assertNotNull(d.getField("link"));
		assertEquals("link", d.getField("link").getName());
	}
	
	//Tests that function which provide string constants are working and not returning null. The content is not checked.
	@Test public void testForNotNullity(){
		MetaDocument doc = new MetaDocument("Document");
		assertNotNull(doc.getIdHolderName());
		assertNotNull(doc.getListConstantValue());
		assertNotNull(doc.getListName());
		assertNotNull(doc.getVariableName());
		assertNotNull(doc.getTemporaryVariableName());
	}
}
