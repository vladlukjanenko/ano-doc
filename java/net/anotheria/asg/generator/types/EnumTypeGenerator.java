package net.anotheria.asg.generator.types;

import net.anotheria.asg.generator.*;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.types.meta.EnumerationType;

import java.util.ArrayList;
import java.util.List;

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
		clazz.addImport("java.util.ArrayList");
		clazz.addImport("java.util.List");

		clazz.setType(TypeOfClass.ENUM);

		clazz.setName(getEnumClassName(type));

		startClassBody();

		final List<String> values = type.getValues();
		final StringBuilder stringBuilder = new StringBuilder("UNKNOWN(0)," + CRLF + "\t");
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

		appendStatement("private final static int[] " + type.getName().toUpperCase() + "_VALUES_LIST = new int[values().length]");
		emptyline();


		appendString("static {");
		increaseIdent();
		appendStatement("int valIndex = 0");
		appendString("for (" + type.getName() + "Enum e : values()) {");
		increaseIdent();
		appendStatement(type.getName().toUpperCase() + "_VALUES_LIST[valIndex++] = e.getValue()");
		decreaseIdent();
		appendString("}");
		decreaseIdent();
		appendString("}");
		emptyline();

		appendStatement("private final int value");
		emptyline();

		appendString("private " + type.getName() + "Enum(int value) {");
		increaseIdent();
		appendStatement("this.value = value");
		decreaseIdent();
		appendString("}");
		emptyline();

		appendCommentLine("");
		appendCommentLine("get enum value field");
		appendCommentLine("");
		appendCommentLine("@return int field");
		appendCommentLine("");
		appendString("public int getValue() {");
		increaseIdent();
		appendStatement("return value");
		decreaseIdent();
		appendString("}");
		emptyline();

		
		appendCommentLine("");
		appendCommentLine("get enum type values converted to int array");
		appendCommentLine("");
		appendCommentLine("@return enum names list");
		appendCommentLine("");
		appendString("public static int[] get" + type.getName() + "Values() {");
		increaseIdent();
		appendStatement("return " + type.getName().toUpperCase() + "_VALUES_LIST");
		decreaseIdent();
		appendString("}");
		emptyline();


		appendString("public static String get" + type.getName() + "Name(int value) {");
		increaseIdent();
		appendString("for (" + type.getName() + "Enum e : values()) {");
		increaseIdent();
		appendString("if (e.getValue() == value) {");
		increaseIdent();
		appendStatement("return e.name()");
		decreaseIdent();
		appendString("}");
		decreaseIdent();
		appendString("}");
		appendStatement("return UNKNOWN.name()");
		decreaseIdent();
		appendString("}");
		emptyline();

		appendString("public static int get" + type.getName() + "Value(String name) {");
		increaseIdent();
		appendString("if (name == null || name.isEmpty()) {");
		increaseIdent();
		appendStatement("throw new IllegalArgumentException(\"Name argument null or empty\")");
		decreaseIdent();
		appendString("}");

		appendString("try {");
		increaseIdent();
		appendStatement("final " + type.getName() + "Enum e = valueOf(name)");
		appendStatement("return e.getValue()");
		decreaseIdent();
		appendString("} catch (EnumConstantNotPresentException e) {");
		increaseIdent();
		appendStatement("return UNKNOWN.value");
		decreaseIdent();
		appendString("}");
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