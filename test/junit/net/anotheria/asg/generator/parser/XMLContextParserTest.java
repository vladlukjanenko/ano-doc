package net.anotheria.asg.generator.parser;

import java.io.IOException;

import net.anotheria.asg.generator.Context;
import net.anotheria.util.IOUtils;

import org.junit.Test;

import static junit.framework.Assert.*;

public class XMLContextParserTest {
	@Test public void testContextParser() throws IOException{
		String content = IOUtils.readFileAtOnceAsString("test/xmldataset/context.xml");
		
		assertNotNull(content);
		assertFalse(content.length()==0);
		
		Context context = XMLContextParser.parseContext(content);
		
		assertEquals("anotheria", context.getOwner());
		assertEquals("net.anotheria.anosite.gen", context.getPackageName());
		assertEquals("testappname", context.getApplicationName());
		assertEquals("testappurlpath", context.getApplicationURLPath());
		assertEquals("cms", context.getServletMapping());
		assertEquals("iso-8859-15", context.getEncoding());
		
		assertTrue(context.getLanguages().size()>0);
		assertTrue(context.getLanguages().contains("DE"));
		assertTrue(context.getLanguages().contains("CH_DE"));
		
		assertEquals("DE", context.getDefaultLanguage());
		
	}
}
