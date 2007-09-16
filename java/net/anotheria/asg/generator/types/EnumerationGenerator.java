package net.anotheria.asg.generator.types;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.types.meta.EnumerationType;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class EnumerationGenerator extends AbstractGenerator implements IGenerator{

	private Context context;
	
	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
	 */
	public List<FileEntry> generate(IGenerateable g, Context context) {
		EnumerationType type = (EnumerationType)g;
		List<FileEntry> ret = new ArrayList<FileEntry>();
		this.context = context;
		
		ret.add(new FileEntry(FileEntry.package2path(getPackageName()), getDefinitionClassName(type), generateDefinition(type)));		
		ret.add(new FileEntry(FileEntry.package2path(getPackageName()), getUtilsClassName(type), generateUtils(type)));		

		return ret;
	}
	
	public static final String getDefinitionClassName(EnumerationType type){
		return "I"+type.getName()+"Definition";
	}
	
	public static final String getUtilsClassName(EnumerationType type){
		return type.getName()+"Utils";
	}

	public static final String getUtilsImport(EnumerationType type){
		return getPackageName(GeneratorDataRegistry.getInstance().getContext())+"."+getUtilsClassName(type);
	}

	public final String getPackageName(){
		return getPackageName(context);
	}
	
	public static String getPackageName(Context context){
		return context.getPackageName(MetaModule.SHARED)+".data";
	}
	
	private String generateDefinition(EnumerationType type){
		String ret = "";
		
		String packageName = getPackageName();
	
		ret += writeStatement("package "+packageName);
		ret += emptyline();
		
		ret += writeString("public interface "+getDefinitionClassName(type)+"{");
		increaseIdent();
		List<String> values = type.getValues();
		for (int i=0; i<values.size(); i++){
			String v = values.get(i);
			ret += writeStatement("public static final int "+v+" = "+(i+1));
		}
		ret += emptyline();
		
		for (String v : values){
			ret += writeStatement("public static final String "+v+"_NAME = "+quote(v));
		}

		ret += emptyline();
		
		ret += writeString("public static final int "+type.getName().toUpperCase()+"_VALUES[] = {");
		for (int i=0; i<values.size(); i++){
			String v = values.get(i);
			ret += writeIncreasedString(v+",");
		}
		
		ret += writeString("};");
		ret += emptyline();
		
		
		ret += writeString("public static final String "+type.getName().toUpperCase()+"_NAMES[] = {");
		for (int i=0; i<values.size(); i++){
			String v = values.get(i);
			ret += writeIncreasedString(v+"_NAME,");
		}
		
		ret += writeString("};");

		ret += closeBlock();

		
		return ret;
	}

	private String generateUtils(EnumerationType type){
		String ret = "";
		
		String packageName = getPackageName();
	
		ret += writeStatement("package "+packageName);
		ret += emptyline();
		ret += writeImport("java.util.Arrays");
		ret += writeImport("java.util.List");
		ret += emptyline();
		
		ret += writeString("public class "+getUtilsClassName(type)+" implements "+getDefinitionClassName(type)+" {");
		increaseIdent();

		ret += writeString("public static List get"+type.getName()+"List(){");
		increaseIdent();
		ret += writeStatement("return Arrays.asList("+type.getName().toUpperCase()+"_NAMES)"); 
		ret += closeBlock();
		
		ret += writeString("public static String getName(int value){");
		increaseIdent();
		ret += writeString("switch(value){");
		increaseIdent();
		List<String> values = type.getValues();
		for (int i=0; i<values.size(); i++){
			String v = values.get(i);
			ret += writeString("case "+v+":");
			ret += writeIncreasedStatement("return "+v+"_NAME");
		}
		ret += writeString("default:");
		ret += writeIncreasedStatement("return \"Unknown: \"+value");
			
		ret += closeBlock();//...switch
		ret += closeBlock();

		ret += closeBlock();

		
		return ret;
	}

}
