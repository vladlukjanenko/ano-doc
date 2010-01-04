package net.anotheria.asg.generator.types;

import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.types.meta.EnumerationType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class EnumTypeGeneratorTest {
	private static final String PACKAGE_NAME = "net.anotheria.anosite.gen.shared.data";
	private static final String ENUM = "enum";

	@Before
	public void setup() {
		Context c = new Context();
		c.setPackageName(PACKAGE_NAME);
		GeneratorDataRegistry.getInstance().setContext(c);
	}

	@Test
	public void shouldGenerateEnum() throws IOException {
		// given
		EnumerationType dataType = new EnumerationType("Priority");
		for (int valIndex = 1; valIndex < 3; valIndex++) {
			dataType.addValue("P" + valIndex);
		}
		IGenerator generator = new EnumerationGenerator();

		// when
		final List<FileEntry> entries = generator.generate(dataType);

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