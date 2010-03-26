package net.anotheria.asg.util.filter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import net.anotheria.asg.data.DataObject;
import net.anotheria.asg.data.ObjectInfo;
import net.anotheria.util.xml.XMLNode;

import org.junit.Before;
import org.junit.Test;

public class MissingTranslationFilterTest {

	private MissingTranslationFilter filter;
	
	private class MockDataObject implements DataObject {

		Map<String,String> propertys = new HashMap<String, String>();
		
		public void clearPropertys() {
			propertys.clear();
		}
		
		public void setProperty(String name, String value) {
			propertys.put(name, value);
		}
		
		@Override
		public String getDefinedName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDefinedParentName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getFootprint() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getLastUpdateTimestamp() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public ObjectInfo getObjectInfo() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object getPropertyValue(String propertyName) { 
			return propertys.get(propertyName);
		}

		@Override
		public XMLNode toXMLNode() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object clone() throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			return super.clone();
		}
		
		
		
	}
	
	@Before
	public void setUp() throws Exception {
		List<String> supportedLanguages = new ArrayList<String>();
		supportedLanguages.add("EN");
		supportedLanguages.add("DE");
		supportedLanguages.add("UA");
		supportedLanguages.add("RU");
		
		filter = new MissingTranslationFilter(supportedLanguages, "RU");	
		
		
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
		
		MockDataObject mock = new MockDataObject();
		
		//--------------------------------
		// Default language 
		//--------------------------------
		
		// Pass "All" trigger ("")		
		mock.setProperty("name_RU", "");				
		assertTrue(filter.mayPass(mock, "name", ""));
				
		mock.setProperty("name_RU", "Not Empty");				
		assertTrue(filter.mayPass(mock, "name", ""));
		
		
		// Pass only empty if trigger = default language
		mock.setProperty("name_RU", "");				
		assertTrue(filter.mayPass(mock, "name", "RU"));
		
		mock.setProperty("name_RU", "NotEmpty");				
		assertFalse(filter.mayPass(mock, "name", "RU"));
		
				
		// Not pass empty default language with any other language trigger		
		mock.setProperty("name_RU", "");
		mock.setProperty("name_EN", "NotEmpty");
		assertFalse(filter.mayPass(mock, "name", "EN"));
		
		mock.setProperty("name_RU", "");
		mock.setProperty("name_EN", "");
		assertFalse(filter.mayPass(mock, "name", "EN"));
		
		
		// Pass only empty other language when default language is not empty
		mock.setProperty("name_RU", "NotEmpty");
		mock.setProperty("name_EN", "");
		assertTrue(filter.mayPass(mock, "name", "EN"));
		
		mock.setProperty("name_RU", "NotEmpty");
		mock.setProperty("name_EN", "NotEmpty");
		assertFalse(filter.mayPass(mock, "name", "EN"));

		// Not pass incorrect property		
		mock.setProperty("name_EN", "NotEmpty");				
		assertFalse(filter.mayPass(mock, "IncorrectPropertyName", "EN"));
		
		
	}

}
