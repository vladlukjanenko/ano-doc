package net.anotheria.asg.generator.parser;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.*;

import java.io.File;
import java.util.List;

import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;

import org.junit.Test;

public class XMLDataParserTest {
	@Test public void testDataParser() throws Exception{
		File f = new File("test/xmldataset/datadef.xml");
		String content = XMLPreprocessor.loadFile(f);
		
		assertNotNull(content);
		assertFalse(content.length()==0);
		
		/////////
		
		List<MetaModule> modules = XMLDataParser.parseModules(content);
		performBasicChecks(modules);
	}
	
	private void performBasicChecks(List<MetaModule> modules){
		MetaModule modA = modules.get(0);
		assertEquals("SimpleModuleA", modA.getName());
		
		MetaDocument docA = modA.getDocumentByName("DocumentA1");
		checkProperties(docA);
	}
	
	private void checkProperties(MetaDocument document){
		checkProperty(document.getField("booleanproperty"), "boolean", "Boolean");
		checkProperty(document.getField("intproperty"), "int", "Integer");
		checkProperty(document.getField("longproperty"), "long", "Long");
		checkProperty(document.getField("doubleproperty"), "double", "Double");
		checkProperty(document.getField("floatproperty"), "float", "Float");
		checkProperty(document.getField("stringproperty"), "String", "String");
		checkProperty(document.getField("textproperty"), "String", "String");

	}
	
	private void checkProperty(MetaProperty p, String java, String javaObject){
		assertNotNull(p);
		assertEquals(java, p.toJavaType());
		assertEquals(javaObject, p.toJavaObjectType());
	}
	
}
