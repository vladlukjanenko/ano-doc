package net.anotheria.asg.generator;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.apputil.CallContextGenerator;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.util.StringUtils;

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
		String ret = "";
		
		ret += writeStatement("package "+context.getServicePackageName(MetaModule.SHARED));


		ret += writeImport("net.anotheria.anodoc.service.IModuleFactory");
		ret += writeImport("net.anotheria.anodoc.service.IModuleService");
		ret += writeImport("net.anotheria.anodoc.service.ModuleServiceFactory");
		ret += writeImport("net.anotheria.anodoc.util.CommonHashtableModuleStorage");
		ret += emptyline();
		for (MetaModule m : modules){
			ret += writeImport(context.getPackageName(m)+".data.*");
		}
		
		ret += emptyline();
		ret += writeString("public class "+getConfiguratorClassName()+"{");
		increaseIdent();
		ret += emptyline();

		ret += writeString("private static void addCommonStorage(String moduleId, IModuleService service, IModuleFactory factory, String storageDirConfigKey){");
		increaseIdent();
		ret += writeString("service.attachModuleFactory(moduleId, factory );");
		ret += writeString("if (storageDirConfigKey==null)");
		ret += writeIncreasedString("service.attachModuleStorage(moduleId, new CommonHashtableModuleStorage(moduleId+\".dat\", factory));");
		ret += writeString("else");
		ret += writeIncreasedString("service.attachModuleStorage(moduleId, new CommonHashtableModuleStorage(moduleId+\".dat\", factory, storageDirConfigKey));");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeStatement("private static boolean configured");
		ret += emptyline();
		ret += writeString("public static void configure(){");
		increaseIdent();
		ret += writeString("if (configured)");
		increaseIdent();
		ret += writeString("return;");
		decreaseIdent();
		ret += writeString("configured = true;");
		ret += writeString("net.anotheria.anodoc.util.context.ContextManager.setFactory(new "+CallContextGenerator.getFullFactoryName(context)+"());");
		
		ret += writeStatement("IModuleService service = ModuleServiceFactory.createModuleService()");
		
		for (int i=0; i<modules.size(); i++){
			MetaModule m = modules.get(i);
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
				ret += writeStatement(call);
			}
			  
		}
		ret += closeBlock();
		ret += closeBlock();
			
		return new FileEntry(FileEntry.package2path(context.getServicePackageName(MetaModule.SHARED)), getConfiguratorClassName(),ret);
		
	}
	

}
