package net.anotheria.asg.generator.parser;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.*;

import java.io.File;
import java.util.List;

import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaLink;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.util.StringUtils;

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
		checkLinks(docA, modules);
	}
	
	private MetaModule findModule(String name, List<MetaModule> modules){
		for (MetaModule m : modules)
			if (m.getName().equals(name))
				return m;
		throw new AssertionError("Can't find module "+name);
	}
	
	private void checkLinks(MetaDocument document, List<MetaModule> modules){
		MetaLink linkA = (MetaLink)document.getField("a2");
		MetaLink linkB = (MetaLink)document.getField("b2");
		
		assertTrue(linkA.isLinked()); 
		assertTrue(linkB.isLinked());
		
		MetaDocument targetA = findModule("SimpleModuleA", modules).getDocumentByName("DocumentA2");
		MetaDocument targetB = findModule("SimpleModuleB", modules).getDocumentByName("DocumentB2");
		
		System.out.println("TargetA: "+targetA);
		System.out.println("TargetA Module: "+targetA.getParentModule());
		System.out.println("TargetB: "+targetB);
		System.out.println("TargetB Module: "+targetB.getParentModule());
		
		//assertTrue(linkA.doesTargetMatch(targetA));
		assertTrue(linkB.doesTargetMatch(targetB));
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
		assertEquals(java, p.toJavaErasedType());
		assertEquals(java, p.getMetaType().toJava());
		assertEquals(javaObject, p.toJavaObjectType());
		assertFalse(p.isLinked());
		assertFalse(p.isReadonly());
		
		assertEquals("get"+StringUtils.capitalize(java), p.toPropertyGetter());
		assertEquals("set"+StringUtils.capitalize(java), p.toPropertySetter());
	}
	
}
