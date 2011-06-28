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
 * TODO please remind another to comment this class.
 * @author another
 */
public class EnumerationGenerator extends AbstractGenerator implements IGenerator{
	/**
	 * Instance of EnumTypeGenerator.
	 */
	private static final EnumTypeGenerator enumTypeGenerator = new EnumTypeGenerator();

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
	 */
	public List<FileEntry> generate(IGenerateable g) {
		EnumerationType type = (EnumerationType)g;
		List<FileEntry> ret = new ArrayList<FileEntry>();
		ret.add(new FileEntry(generateDefinition(type)));		
		ret.add(new FileEntry(generateUtils(type)));		
		ret.addAll(enumTypeGenerator.generate(type));
		return ret;
	}
	
	public static String getDefinitionClassName(EnumerationType type){
		return "I"+type.getName()+"Definition";
	}
	
	public static String getUtilsClassName(EnumerationType type){
		return type.getName()+"Utils";
	}

	public static String getUtilsImport(EnumerationType type){
		return getPackageName(GeneratorDataRegistry.getInstance().getContext())+"."+getUtilsClassName(type);
	}

	public final String getPackageName(){
		return getPackageName(GeneratorDataRegistry.getInstance().getContext());
	}
	
	public static String getPackageName(Context context){
		return context.getPackageName(MetaModule.SHARED)+".data";
	}
	
	private GeneratedClass generateDefinition(EnumerationType type){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		clazz.setPackageName(getPackageName());

		clazz.setType(TypeOfClass.INTERFACE);
		
		clazz.setName(getDefinitionClassName(type));
		
		startClassBody();
		
		List<String> values = type.getValues();
		for (int i=0; i<values.size(); i++){
			String v = values.get(i);
			appendStatement("public static final int "+v+" = "+(i+1));
		}
		emptyline();
		
		for (String v : values){
			appendStatement("public static final String "+v+"_NAME = "+quote(v));
		}

		emptyline();
		
		appendString("public static final int "+type.getName().toUpperCase()+"_VALUES[] = {");
        for (String v : values) {
            appendIncreasedString(v + ",");
        }
		
		appendString("};");
		emptyline();
		
		
		appendString("public static final String "+type.getName().toUpperCase()+"_NAMES[] = {");
        for (String v : values) {
            appendIncreasedString(v + "_NAME,");
        }
		
		appendString("};");
		return clazz;
	}

	private GeneratedClass generateUtils(EnumerationType type){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackageName());
		clazz.addImport("java.util.Arrays");
		clazz.addImport("java.util.List");

		clazz.setName(getUtilsClassName(type));
		clazz.addInterface(getDefinitionClassName(type));

		startClassBody();
		appendString("public static List<String> get"+type.getName()+"List(){");
		increaseIdent();
		appendStatement("return Arrays.asList("+type.getName().toUpperCase()+"_NAMES)"); 
		append(closeBlock());
		
		appendString("public static String getName(int value){");
		increaseIdent();
		appendString("switch(value){");
		increaseIdent();
		List<String> values = type.getValues();
        for (String v : values) {
            appendString("case " + v + ":");
            appendIncreasedStatement("return " + v + "_NAME");
        }
		appendString("default:");
		appendIncreasedStatement("return \"Unknown: \"+value");
			
		append(closeBlock());//...switch
		append(closeBlock());//getName(...)

		appendString("public static int getValue(String name){");
		increaseIdent();
        for (String v : values) {
            appendString("if( " + v + "_NAME.equals(name))");
            appendIncreasedStatement("return " + v);
        }
		appendIncreasedStatement("return 0");
			
		append(closeBlock());//getValue(...)
		
		return clazz;		
	}

}
