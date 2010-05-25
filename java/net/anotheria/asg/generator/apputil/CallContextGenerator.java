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
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.util.StringUtils;

/**
 * Generater for the call context.
 * @author lrosenberg
 *
 */
public class CallContextGenerator extends AbstractGenerator implements IGenerator {

	public List<FileEntry> generate(IGenerateable g) {
		List<FileEntry> ret = new ArrayList<FileEntry>();
		ret.add(generateCallContextFactory());
		ret.add(generateCallContext());
		return ret;
	}

	/**
	 * Generated the special call context class.
	 * @return
	 */
	private FileEntry generateCallContext(){
		
		Context context = GeneratorDataRegistry.getInstance().getContext();
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackageName());
		
		clazz.addImport("net.anotheria.anodoc.util.context.CallContext");
		clazz.addImport("java.io.Serializable");
		clazz.addImport("java.util.List");		
		
		clazz.setName(getCallContextName(context));
		clazz.setParent("CallContext");
		clazz.addInterface("Serializable");

		startClassBody();
		
		appendString("public String getDefaultLanguage() {");
		increaseIdent();
		appendStatement("return "+quote(context.getDefaultLanguage()));
		append(closeBlock());
				
		appendString("public List<String> getSupportedLanguages() {");
		increaseIdent();
		// Process multilanguage support
		if (context.areLanguagesSupported()) {
			clazz.addImport(context.getServicePackageName(MetaModule.SHARED) + "." + LanguageUtilsGenerator.getCopierClassName(context));
			appendStatement("return " + LanguageUtilsGenerator.getCopierClassName(context) + ".getSupportedLanguages()");
		} else {
			clazz.addImport("java.util.ArrayList");
			appendStatement("return new ArrayList<String>()");
		}
		append(closeBlock());
		
		
		return new FileEntry(clazz);
		
		
	}
	
	/**
	 * Generates a factory for the call context.
	 * @return
	 */
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
