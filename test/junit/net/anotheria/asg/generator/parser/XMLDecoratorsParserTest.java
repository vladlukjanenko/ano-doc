package net.anotheria.asg.generator.parser;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import net.anotheria.asg.generator.view.meta.MetaDecorator;
import net.anotheria.util.IOUtils;

import org.junit.Test;

public class XMLDecoratorsParserTest {
	@Test public void testDecoratorsParser() throws IOException{
		String content = IOUtils.readFileAtOnceAsString("test/xmldataset/decorators.xml");
		
		assertNotNull(content);
		assertFalse(content.length()==0);

		List<MetaDecorator> decorators = XMLDecoratorsParser.parseDecorators(content);
		
		assertEquals(2, decorators.size());
		
		MetaDecorator a = decorators.get(0);
		MetaDecorator b = decorators.get(1);
		
		assertEquals("decoratorA", a.getName());
		assertEquals("net.anotheria.asg.util.decorators.DecoratorA", a.getClassName());
		assertEquals("DecoratorA", a.getClassNameOnly());

		assertEquals("bDecorator", b.getName());
		assertEquals("net.anotheria.asg.util.decorators.BDecorator", b.getClassName());
		assertEquals("BDecorator", b.getClassNameOnly());
}
}
