package net.anotheria.asg.generator.apputil;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.util.StringUtils;

public class CallContextGenerator extends AbstractGenerator implements IGenerator {

	public List<FileEntry> generate(IGenerateable g) {
		List<FileEntry> ret = new ArrayList<FileEntry>();
		ret.add(generateCallContextFactory());
		ret.add(generateCallContext());
		return ret;
	}

	private FileEntry generateCallContext(){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackageName());
		
		clazz.addImport("net.anotheria.anodoc.util.context.CallContext");
		clazz.addImport("java.io.Serializable");
		
		clazz.setName(getCallContextName(GeneratorDataRegistry.getInstance().getContext()));
		clazz.setParent("CallContext");
		clazz.addInterface("Serializable");

		startClassBody();
		appendString("public String getDefaultLanguage(){");
		increaseIdent();
		appendStatement("return "+quote(GeneratorDataRegistry.getInstance().getContext().getDefaultLanguage()));
		append(closeBlock());
		
		return new FileEntry(clazz);
		
		
	}
	
	private FileEntry generateCallContextFactory(){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackageName());
		
		clazz.addImport("net.anotheria.anodoc.util.context.CallContextFactory");
		clazz.addImport("net.anotheria.anodoc.util.context.CallContext");

		clazz.setName(getFactoryName(GeneratorDataRegistry.getInstance().getContext()));
		clazz.addInterface("CallContextFactory");

		startClassBody();
		appendString("public CallContext createContext(){");
		increaseIdent();
		appendStatement("return new "+getPreName()+"CallContext()");
		append(closeBlock());
		return new FileEntry(clazz);
	}
	
	private static String getPreName(Context context){
		return StringUtils.capitalize(context.getApplicationName());
	}

	private String getPreName(){
		return getPreName(GeneratorDataRegistry.getInstance().getContext());
	}

	private String getPackageName(){
		return getPackageName(GeneratorDataRegistry.getInstance().getContext());
	}
	
	private static String getPackageName(Context context){
		return context.getTopPackageName()+".util";
	}

	public static final String getFullFactoryName(Context c){
		return getPackageName(c)+"."+getFactoryName(c);
	}
	
	public static String getFactoryName(Context c){
		return getPreName(c)+"CallContextFactory";
	}
	public static String getCallContextName(Context c){
		return getPreName(c)+"CallContext";
	}

}
