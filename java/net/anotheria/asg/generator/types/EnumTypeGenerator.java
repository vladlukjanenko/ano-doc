package net.anotheria.asg.generator.types;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.TypeOfClass;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.types.meta.EnumerationType;

/**
 * TODO please remind another to comment this class
 *
 * @author another
 */
public class EnumTypeGenerator extends AbstractGenerator implements IGenerator {

	/* (non-Javadoc)
		  * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
		  */

	public List<FileEntry> generate(IGenerateable g) {
		EnumerationType type = (EnumerationType) g;
		List<FileEntry> ret = new ArrayList<FileEntry>();
		ret.add(new FileEntry(generateEnum(type)));
		return ret;
	}

	private String getPackageName() {
		return getPackageName(GeneratorDataRegistry.getInstance().getContext());
	}

	private static String getPackageName(Context context) {
		return context.getPackageName(MetaModule.SHARED) + ".data";
	}

	private GeneratedClass generateEnum(EnumerationType type) {
		final GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		clazz.setPackageName(getPackageName());
		clazz.addImport("net.anotheria.asg.exception.ConstantNotFoundException");

		clazz.setType(TypeOfClass.ENUM);

		clazz.setName(getEnumClassName(type));

		startClassBody();

		final List<String> values = type.getValues();
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < values.size(); i++) {
			String v = values.get(i).toUpperCase();
			int index = i + 1;
			stringBuilder.append(v).append('(').append(index).append(')');
			if (index != values.size()) {
				stringBuilder.append("," + CRLF + "\t");
			}
		}
		appendStatement(stringBuilder.toString());
		emptyline();

		appendStatement("private final int value");
		emptyline();

		appendString("private " + type.getName() + "Enum(int value) {");
		increaseIdent();
		appendStatement("this.value = value");
		decreaseIdent();
		appendString("}");
		emptyline();

		appendString("public int getValue() {");
		increaseIdent();
		appendStatement("return value");
		decreaseIdent();
		appendString("}");
		emptyline();

		appendString("public static " + getEnumClassName(type) + " getConstantByValue(int value) throws ConstantNotFoundException {");
		increaseIdent();
		appendString("for (" + type.getName() + "Enum e : values()) {");
		increaseIdent();
		appendString("if (e.getValue() == value) {");
		increaseIdent();
		appendStatement("return e");
		decreaseIdent();
		appendString("}");
		decreaseIdent();
		appendString("}");
		appendStatement("throw new ConstantNotFoundException(\"Enum value not found by value \" + value)");
		decreaseIdent();
		appendString("}");
		emptyline();
		
		return clazz;
	}

	public static String getEnumClassName(EnumerationType type) {
		return type.getName() + "Enum";
	}

	public static String getEnumImport(EnumerationType type) {
		return getPackageName(GeneratorDataRegistry.getInstance().getContext()) + "." + getEnumClassName(type);
	}
}