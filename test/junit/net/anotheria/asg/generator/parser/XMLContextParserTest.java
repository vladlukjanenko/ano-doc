package net.anotheria.asg.generator.parser;

import java.io.IOException;
import java.util.List;

import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.ContextParameter;
import net.anotheria.util.IOUtils;

import org.junit.Test;

import static junit.framework.Assert.*;

public class XMLContextParserTest {
	@Test public void testContextParser() throws IOException{
		Context context = loadContext();		
		assertEquals("anotheria", context.getOwner());
		assertEquals("net.anotheria.anosite.gen", context.getPackageName());
		assertEquals("testappname", context.getApplicationName());
		assertEquals("/testappurlpath", context.getApplicationURLPath());
		assertEquals("cms", context.getServletMapping());
		assertEquals("iso-8859-15", context.getEncoding());
		
		assertTrue(context.getLanguages().size()>0);
		assertTrue(context.getLanguages().contains("DE"));
		assertTrue(context.getLanguages().contains("CH_DE"));
		
		assertEquals("DE", context.getDefaultLanguage());
		assertTrue(context.areLanguagesSupported());
		
	}
	
	@Test public void testParameters() throws IOException{
		Context context = loadContext();
		List<ContextParameter> parameters = context.getContextParameters();
		assertNotNull(parameters);
		assertEquals(2, parameters.size());
		
		ContextParameter p1 = context.getContextParameter("parameter1");
		assertEquals("parameter1", p1.getName());
		assertEquals("1", p1.getValue());
		
		ContextParameter p2 = context.getContextParameter("parameter2");
		assertEquals("parameter2", p2.getName());
		assertEquals("abcdef", p2.getValue());
		
	}
	
	private Context loadContext() throws IOException{
		String content = IOUtils.readFileAtOnceAsString("test/xmldataset/context.xml");
		
		assertNotNull(content);
		assertFalse(content.length()==0);
		return XMLContextParser.parseContext(content);
	}
}
