package net.anotheria.asg.generator.apputil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.util.StringUtils;

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
		String ret = "";
		
		ret += writeStatement("package "+context.getServicePackageName(MetaModule.SHARED));
		
		/*ret += writeImport("net.anotheria.anodoc.service.IModuleFactory");
		ret += writeImport("net.anotheria.anodoc.service.IModuleService");
		ret += writeImport("net.anotheria.anodoc.service.ModuleServiceFactory");
		ret += writeImport("net.anotheria.anodoc.util.CommonHashtableModuleStorage");
		ret += emptyline();
		ret += writeImport(context.getPackageName()+".data.*");
		*/
		
		Set<MetaModule> modulesSet = new HashSet<MetaModule>();
		for (MetaModule m : modules){
			for (MetaDocument doc : m.getDocuments()){
				if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
					modulesSet.add(m);
				}
			}
			
		}

		
		ret += writeImport("java.util.List");
		ret += writeImport("java.util.Arrays");
		ret += emptyline();
		ret += writeImport("org.apache.log4j.BasicConfigurator");
		ret += emptyline();
		for (MetaModule m : modulesSet){
			ret += writeImport(ServiceGenerator.getFactoryImport(context, m));
			//ret += writeImport(ServiceGenerator.getInterfaceImport(context, m));
		}
		
		ret += emptyline();
		ret += writeString("public class "+getCopierClassName(context)+"{");
		increaseIdent();
		ret += emptyline();
		
		//create supported language list;
		ret += writeString("private static List<String> supportedLanguages = Arrays.asList(new String[]{");
		increaseIdent();
		List<String> languages = context.getLanguages();
		for (String l : languages)
			ret += writeString(quote(l)+","); 
		decreaseIdent();
		ret += writeString("});");
		ret += emptyline();
		

		ret += writeString("public static void copyAttributesFromLanguage2Language(String sourceLang, String targetLanguage){");
		increaseIdent();
		ret += closeBlock();
		
		ret += writeString("public static void main(String[] a) throws Exception{");
		increaseIdent();
		ret += writeStatement("BasicConfigurator.configure()");
		ret += writeStatement("String sourceLang, targetLang");
		ret += writeString("if (a.length!=2)");
		ret += writeIncreasedStatement("throw new RuntimeException("+quote("Wrong number of arguments, please use "+getCopierClassName(context)+" sourcelanguage targetlanguage")+")");
		ret += writeStatement("sourceLang = a[0].toUpperCase()");
		ret += writeStatement("checkParameter(sourceLang)");
		ret += writeStatement("targetLang = a[1].toUpperCase()");
		ret += writeStatement("checkParameter(targetLang)");
		ret += emptyline();
		ret += writeStatement("copy(sourceLang, targetLang)");
		
		
		ret += closeBlock();//..main
		ret += emptyline();
		

		
		ret += writeString("private static void checkParameter(String lang){");
		increaseIdent();
		ret += writeString("if (supportedLanguages.indexOf(lang)==-1)");
		ret += writeIncreasedStatement("throw new RuntimeException("+quote("Language ")+"+lang+"+quote("not supported")+")");
		ret += closeBlock(); // ...checkParameter
		ret += emptyline();
		
		//TODO replace with Ano-Doc exception
		ret += writeString("private static void copy(String sourceLanguage, String targetLanguage) throws Exception {");
		increaseIdent();
		ret += writeString("//initialize and copy");
		for (MetaModule m : modulesSet){
			ret += writeStatement("System.out.println(\"Working on "+ServiceGenerator.getServiceName(m)+"\")");
			ret += writeStatement(ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"().copyMultilingualAttributesInAllObjects(sourceLanguage, targetLanguage)");
		}
		ret += emptyline();
		ret += closeBlock();//...copy

		
		
		
		ret += closeBlock(); // ..class

		return new FileEntry(FileEntry.package2path(context.getServicePackageName(MetaModule.SHARED)), getCopierClassName(context),ret);
		
	}

}
