package net.anotheria.asg.generator.apputil;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LanguageUtilsGenerator extends AbstractGenerator{
	
	public List<FileEntry> generate(List<MetaModule> modules, Context context){
		List<FileEntry> entries = new ArrayList<FileEntry>();
		
		if (context.areLanguagesSupported())
			entries.add(generateLanguageCopy(modules, context));
		
		return entries;
		
	}

	public static String getCopierClassName(Context context){
		return StringUtils.capitalize(context.getApplicationName())+"LanguageUtils";
	}
	
	private FileEntry generateLanguageCopy(List<MetaModule> modules, Context context){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(context.getServicePackageName(MetaModule.SHARED));
		
		Set<MetaModule> modulesSet = new HashSet<MetaModule>();
		for (MetaModule m : modules){
			for (MetaDocument doc : m.getDocuments()){
				if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
					modulesSet.add(m);
				}
			}
			
		}

		
		clazz.addImport("java.util.List");
		clazz.addImport("java.util.Arrays");
		emptyline();
		for (MetaModule m : modulesSet){
			clazz.addImport(ServiceGenerator.getFactoryImport(m));
			//appendImport(ServiceGenerator.getInterfaceImport(context, m));
		}
		
		clazz.setName(getCopierClassName(context));
		
		startClassBody();
		//create supported language list;
		appendString("private static final List<String> supportedLanguages = Arrays.asList(new String[]{");
		increaseIdent();
		List<String> languages = context.getLanguages();
		for (String l : languages)
			appendString(quote(l)+","); 
		decreaseIdent();
		appendString("});");
		emptyline();
		
		appendString("public static List<String> getSupportedLanguages(){");
		increaseIdent();
		appendStatement("return supportedLanguages");
		append(closeBlock());
		emptyline();
		
		appendString("public static void copyAttributesFromLanguage2Language(String sourceLang, String targetLanguage){");
		increaseIdent();
		append(closeBlock());
		emptyline();
		
		appendString("public static void main(String[] a) throws Exception{");
		increaseIdent();
		appendStatement("String sourceLang, targetLang");
		appendString("if (a.length!=2)");
		appendIncreasedStatement("throw new RuntimeException("+quote("Wrong number of arguments, please use "+getCopierClassName(context)+" sourcelanguage targetlanguage")+")");
		appendStatement("sourceLang = a[0].toUpperCase()");
		appendStatement("checkParameter(sourceLang)");
		appendStatement("targetLang = a[1].toUpperCase()");
		appendStatement("checkParameter(targetLang)");
		emptyline();
		appendStatement("copy(sourceLang, targetLang)");
		
		
		append(closeBlock());
		emptyline();
		

		
		appendString("private static void checkParameter(String lang){");
		increaseIdent();
		appendString("if (supportedLanguages.indexOf(lang)==-1)");
		appendIncreasedStatement("throw new RuntimeException("+quote("Language ")+"+lang+"+quote("not supported")+")");
		append(closeBlock());
		emptyline();
		
		//TODO replace with Ano-Doc exception
		appendString("private static void copy(String sourceLanguage, String targetLanguage) throws Exception {");
		increaseIdent();
		appendString("//initialize and copy");
		for (MetaModule m : modulesSet){
			appendStatement("System.out.println(\"Working on "+ServiceGenerator.getServiceName(m)+"\")");
			appendStatement(ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"().copyMultilingualAttributesInAllObjects(sourceLanguage, targetLanguage)");
		}
		emptyline();
		append(closeBlock());

		return new FileEntry(clazz);
		
	}

}
