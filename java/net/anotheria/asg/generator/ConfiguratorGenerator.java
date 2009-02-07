package net.anotheria.asg.generator;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.apputil.CallContextGenerator;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.asg.generator.model.docs.CMSBasedServiceGenerator;
import net.anotheria.asg.metafactory.Extension;
import net.anotheria.asg.metafactory.MetaFactory;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class ConfiguratorGenerator extends AbstractGenerator implements IGenerator{
	
	@Override
	public List<FileEntry> generate(IGenerateable g, Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	public static String getConfiguratorClassName(){
		return "AnoDocConfigurator";
	}

	public static String getMetaFactoryConfiguratorClassName(){
		return "MetaFactoryConfigurator";
	}

	public List<FileEntry> generate(List<MetaModule> modules, Context context){
		List<FileEntry> entries = new ArrayList<FileEntry>();
		
		entries.add(generateConfigurator(modules, context));
		entries.add(generateMetaFactoryConfigurator(modules, context));
		return entries;
		
	}
	
	private FileEntry generateMetaFactoryConfigurator(List<MetaModule> modules, Context context){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setGenerator(this);
		clazz.setPackageName(context.getServicePackageName(MetaModule.SHARED));
		
		clazz.setName(getMetaFactoryConfiguratorClassName());
		clazz.addImport(Extension.class);
		clazz.addImport(MetaFactory.class);
		
		startClassBody();

		appendStatement("private static volatile boolean configured");
		appendEmptyline();
		appendString("public static void configure(){");
		increaseIdent();
		appendString("if (configured)");
		increaseIdent();
		appendString("return;");
		decreaseIdent();
		appendString("configured = true;");
		for (int i=0; i<modules.size(); i++){
			MetaModule m = modules.get(i);
			
			clazz.addImport(ServiceGenerator.getInterfaceImport(context, m));
			clazz.addImport(ServiceGenerator.getFactoryImport(context, m));
			appendCommentLine("//aliases for "+ServiceGenerator.getInterfaceName(m));
			appendStatement("MetaFactory.addAlias("+ServiceGenerator.getInterfaceName(m)+".class, Extension.LOCAL)");
			appendStatement("MetaFactory.addAlias("+ServiceGenerator.getInterfaceName(m)+".class, Extension.DOMAIN, Extension.LOCAL)");
			appendStatement("MetaFactory.addAlias("+ServiceGenerator.getInterfaceName(m)+".class, Extension.DOMAIN, Extension.EDITORINTERFACE)");
			
			if (m.getStorageType()==StorageType.CMS){
				appendStatement("MetaFactory.addAlias("+ServiceGenerator.getInterfaceName(m)+".class, Extension.CMS, Extension.DOMAIN)");
				appendStatement("MetaFactory.addFactoryClass("+ServiceGenerator.getInterfaceName(m)+".class, Extension.CMS, "+ServiceGenerator.getFactoryName(m)+".class)");
			}
			
			if (m.getStorageType()==StorageType.DB){
				appendStatement("MetaFactory.addAlias("+ServiceGenerator.getInterfaceName(m)+".class, Extension.DB, Extension.DOMAIN)");
				appendStatement("MetaFactory.addFactoryClass("+ServiceGenerator.getInterfaceName(m)+".class, Extension.DB, "+ServiceGenerator.getFactoryName(m)+".class)");
			}

			if (m.getStorageType()==StorageType.FEDERATION){
				appendStatement("MetaFactory.addAlias("+ServiceGenerator.getInterfaceName(m)+".class, Extension.FEDERATION, Extension.DOMAIN)");
				appendStatement("MetaFactory.addFactoryClass("+ServiceGenerator.getInterfaceName(m)+".class, Extension.FEDERATION, "+ServiceGenerator.getFactoryName(m)+".class)");
			}
			
		}
		append(closeBlock());
		return new FileEntry(clazz);
			
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
		
		
		appendStatement("private static volatile boolean configured");
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
		appendStatement(getMetaFactoryConfiguratorClassName()+".configure()");
		append(closeBlock());
		return new FileEntry(clazz);
		
	}
	

}
