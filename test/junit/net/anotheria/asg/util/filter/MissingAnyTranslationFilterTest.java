package net.anotheria.asg.util.filter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import net.anotheria.asg.data.AbstractASGDocument;


import org.junit.Before;
import org.junit.Test;

public class MissingAnyTranslationFilterTest {

	private MissingAnyTranslationFilter filter;
		
	@Before
	public void setUp() throws Exception {
		List<String> supportedLanguages = new ArrayList<String>();
		supportedLanguages.add("EN");
		supportedLanguages.add("DE");
		supportedLanguages.add("UA");
		supportedLanguages.add("RU");
		
		filter = new MissingAnyTranslationFilter(supportedLanguages, "RU");	
		
		
	}

	@Test
	public void testConstructor() {
		
		// From test, no CallContext will be founded, so constructor must set default language and supported languages to EN
		MissingTranslationFilter filter2 = new MissingTranslationFilter();
		assertEquals(filter2.getDefaultLanguage(), "EN");
		assertTrue(filter2.getSupportedLanguages().size() == 1);
		assertEquals(filter2.getSupportedLanguages().get(0),"EN");
		
		
	}
	
	@Test
	public void testMayPass() {
		
		AbstractASGDocument document = new AbstractASGDocument("123") {
			
			@Override
			public String getFootprint() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getDefinedParentName() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getDefinedName() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		
			
		// Not pass document without multilangual fields
		document.setString("field1", "field1");		
		document.setString("field2", "field2");
		assertFalse(filter.mayPass(document, null, "EN"));
		
		
		// Pass "All" trigger ("")		
		document.setString("name_RU", "");		
		assertTrue(filter.mayPass(document, null, ""));
		
		document.setString("name_RU", "NotEmpty");		
		assertTrue(filter.mayPass(document, null, ""));
		
		// Pass only empty default if given filter language equals to default
		document.setString("name_RU", "");		
		assertTrue(filter.mayPass(document, null, "RU"));		
		
		document.setString("name_RU", "NotEmpty");		
		assertFalse(filter.mayPass(document, null, "RU"));
		
		
		// Pass only NotEmpty default and empty not default 
		document.setString("name_RU", "NotEmpty");
		document.setString("name_EN", "");
		assertTrue(filter.mayPass(document, null, "EN"));		
		
		document.setString("name_RU", "NotEmpty");
		document.setString("name_EN", "NotEmpty");
		assertFalse(filter.mayPass(document, null, "EN"));
		
		document.setString("name_RU", "");
		document.setString("name_EN", "NotEmpty");
		assertFalse(filter.mayPass(document, null, "EN"));
		
		document.setString("name_RU", "");
		document.setString("name_EN", "");
		assertFalse(filter.mayPass(document, null, "EN"));
		
		
		// Check all properties
		document.setString("name_RU", "NotEmpty");
		document.setString("name_EN", "");
		document.setString("title_RU", "NotEmpty");
		document.setString("title_EN", "");
		assertTrue(filter.mayPass(document, null, ""));
		
		document.setString("name_RU", "NotEmpty");
		document.setString("name_EN", "NotEmptyt");
		document.setString("title_RU", "NotEmpty");
		document.setString("title_EN", "");
		assertTrue(filter.mayPass(document, null, ""));
		
		
				
		
		
		
	}

}
