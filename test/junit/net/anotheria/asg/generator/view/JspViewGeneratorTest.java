package net.anotheria.asg.generator.view;

import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.cms20.view.JspViewCms20Generator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaEnumerationProperty;
import net.anotheria.asg.generator.meta.MetaListProperty;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.view.meta.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JspViewGeneratorTest {
	private static final String PACKAGE_NAME = "net.anotheria.anosite.gen.shared.data";
	private static final String ENUM = "enum";

	@Before
	public void setup() {
		Context c = new Context();
		c.setPackageName(PACKAGE_NAME);
		GeneratorDataRegistry.getInstance().setContext(c);		
	}

	@Test @Ignore
	public void shouldViewWithEnumAsSelect() throws IOException {
		// given
		List<MetaViewElement> metaViewElements = new ArrayList<MetaViewElement>();
		MetaViewElement field = new MetaFieldElement("prioTestList");
		metaViewElements.add(field);
		MetaView view = new MetaView("Priority");
		MetaModuleSection metaSection = new MetaModuleSection("test");
		MetaListElement listElement = new MetaListElement();
		listElement.setElements(metaViewElements);
		metaSection.addElement(listElement);


		MetaDialog dialog = new MetaDialog("test");
		dialog.addElement(listElement);
		metaSection.addDialog(dialog);
		view.addSection(metaSection);


		MetaModule module = new MetaModule("test");
		MetaDocument metaDocument = new MetaDocument("test");
		MetaListProperty listProperty = new MetaListProperty("prioTestList", new MetaEnumerationProperty("prio", "string"));
		metaDocument.addProperty(listProperty);
		module.addDocument(metaDocument);
		metaSection.setModule(module);
		metaSection.setDocument(metaDocument);

		IGenerator generator = new JspViewCms20Generator();

		// when
		final List<FileEntry> entries = generator.generate(view);

		String enumContent = null;
		for (FileEntry fileEntry : entries) {
			if (fileEntry.getContent().contains(ENUM)) {
				enumContent = fileEntry.getContent();
			}
		}

		// then
		Assert.assertNotNull(enumContent);
	}
}