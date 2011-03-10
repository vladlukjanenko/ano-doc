package net.anotheria.asg.generator.meta;

import org.junit.Test;
import static junit.framework.Assert.*;

public class MetaPropertyTest {
	@Test public void testNaming(){
		MetaProperty p = new MetaProperty("test", MetaProperty.Type.STRING);
		
		assertEquals("test", p.getName());
		
		assertEquals("PROP_TEST", p.toNameConstant());
		assertEquals("PROP_TEST_DE", p.toNameConstant("DE"));
		assertEquals("PROP_TEST_EN", p.toNameConstant("EN"));
		
		assertEquals("getTest", p.toBeanGetter());
		assertEquals("getTest", p.toBeanGetter(null));
		assertEquals("getTestDE", p.toBeanGetter("DE"));

		assertEquals("setTest", p.toBeanSetter());
		assertEquals("setTest", p.toBeanSetter(null));
		assertEquals("setTestDE", p.toBeanSetter("DE"));
	}
}
