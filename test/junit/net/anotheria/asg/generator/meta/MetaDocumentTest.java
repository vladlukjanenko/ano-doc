package net.anotheria.asg.generator.meta;

import org.junit.Test;
import static junit.framework.Assert.*;

public class MetaDocumentTest {
	@Test public void testPropertyUniqueness(){
		MetaDocument d = new MetaDocument("test");
		
		d.addProperty(new MetaProperty("a", "string"));
		d.addLink(new MetaLink("b"));
		assertEquals(1, d.getProperties().size());
		assertEquals(1, d.getLinks().size());

		try{
			d.addProperty(new MetaProperty("a", "string"));
			fail("exception expected");
		}catch(IllegalArgumentException e){}
		assertEquals(1, d.getProperties().size());
		assertEquals(1, d.getLinks().size());

		try{
			d.addLink(new MetaLink("a"));
			fail("exception expected");
		}catch(IllegalArgumentException e){}
		assertEquals(1, d.getProperties().size());
		assertEquals(1, d.getLinks().size());
	}
}
