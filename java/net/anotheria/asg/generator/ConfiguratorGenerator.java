package net.anotheria.asg.generator;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.apputil.CallContextGenerator;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.StorageType;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class ConfiguratorGenerator extends AbstractGenerator{
	
	public static String getConfiguratorClassName(){
		return "AnoDocConfigurator";
	}

	public List<FileEntry> generate(List<MetaModule> modules, Context context){
		List<FileEntry> entries = new ArrayList<FileEntry>();
		
		entries.add(generateConfigurator(modules, context));
		return entries;
		
	}
	
	private FileEntry generateConfigurator(List<MetaModule> modules, Context context){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(context.getServicePackageName(MetaModule.SHARED));


		clazz.addImport("net.anotheria.anodoc.service.IModuleFactory");
		clazz.addImport("net.anotheria.anodoc.service.IModuleService");
		clazz.addImport("net.anotheria.anodoc.service.ModuleServiceFactory");
		clazz.addImport("net.anotheria.anodoc.util.CommonHashtableModuleStorage");
		
		clazz.setName(getConfiguratorClassName());
		
		startClassBody();

		appendString("private static void addCommonStorage(String moduleId, IModuleService service, IModuleFactory factory, String storageDirConfigKey){");
		increaseIdent();
		appendString("service.attachModuleFactory(moduleId, factory );");
		appendString("if (storageDirConfigKey==null)");
		appendIncreasedString("service.attachModuleStorage(moduleId, new CommonHashtableModuleStorage(moduleId+\".dat\", factory));");
		appendString("else");
		appendIncreasedString("service.attachModuleStorage(moduleId, new CommonHashtableModuleStorage(moduleId+\".dat\", factory, storageDirConfigKey));");
		append(closeBlock());
		appendEmptyline();
		
		
		appendStatement("private static boolean configured");
		appendEmptyline();
		appendString("public static void configure(){");
		increaseIdent();
		appendString("if (configured)");
		increaseIdent();
		appendString("return;");
		decreaseIdent();
		appendString("configured = true;");
		appendString("net.anotheria.anodoc.util.context.ContextManager.setFactory(new "+CallContextGenerator.getFullFactoryName(context)+"());");
		
		appendStatement("IModuleService service = ModuleServiceFactory.createModuleService()");
		
		for (int i=0; i<modules.size(); i++){
			MetaModule m = modules.get(i);
			clazz.addImport(context.getPackageName(m)+".data.*");
			if (m.getStorageType()==StorageType.CMS){
				String call = "addCommonStorage(";
				call += m.getModuleClassName()+".MODULE_ID";
				call += ", ";
				call += "service";
				call += ", ";
				call += "new "+m.getFactoryClassName()+"()";
				if (m.getStorageKey()!=null)
					call += ", "+quote(m.getStorageKey());
				else
					call += ", null";
				call +=")";
				appendStatement(call);
			}
			  
		}
		append(closeBlock());
		return new FileEntry(clazz);
		
	}
	

}
