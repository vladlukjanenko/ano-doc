package net.anotheria.asg.generator;

import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.service.AbstractASGService;

import java.util.ArrayList;
import java.util.List;


/**
 * TODO Please remain lrosenberg to comment AbstractServiceGenerator.java
 * @author lrosenberg
 * @created on Feb 24, 2005
 */
public class BasicServiceGenerator extends AbstractGenerator{
	
	
	public List<FileEntry> generate(List<MetaModule>  modules){
		List<FileEntry> ret = new ArrayList<FileEntry>(); 
		
		ret.add(new FileEntry(generateBasicService(modules)));
		ret.add(new FileEntry(generateBasicCMSService(modules)));
		
		return ret;
	}
	
	private GeneratedClass generateBasicService(List<MetaModule> modules){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".service");
		
		clazz.addImport("org.slf4j.Logger");
		clazz.addImport("org.slf4j.LoggerFactory");

		clazz.addImport(AbstractASGService.class);

		clazz.setAbstractClass(true);
		clazz.setName("BasicService");
		clazz.setParent(AbstractASGService.class);
		
		startClassBody();

		appendStatement("protected Logger log");
		emptyline();
		

        //generate constructor
        appendString("protected BasicService(){");
        increaseIdent();
        appendStatement("log = LoggerFactory.getLogger(this.getClass())");
        closeBlockNEW();
        emptyline();
		return clazz;
	}

	private GeneratedClass generateBasicCMSService(List<MetaModule> modules){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".service");

		clazz.addImport("net.anotheria.anodoc.data.Module");
		clazz.addImport("net.anotheria.anodoc.service.IModuleService");
		clazz.addImport("net.anotheria.anodoc.service.ModuleServiceFactory");
		clazz.addImport("net.anotheria.asg.util.listener.IModuleListener");

		clazz.setName("BasicCMSService");
		clazz.setParent("BasicService");
		clazz.setAbstractClass(true);

		startClassBody();
		
		appendStatement("public static final String MY_OWNER_ID = "+quote(GeneratorDataRegistry.getInstance().getContext().getOwner()));
		appendStatement("protected IModuleService service");
		emptyline();

		appendString("static{");
		increaseIdent();
		appendString("AnoDocConfigurator.configure();");
        closeBlockNEW();
        emptyline();
        
        //generate constructor
        appendString("protected BasicCMSService(){");
        increaseIdent();
        appendStatement("service = ModuleServiceFactory.createModuleService()");
        closeBlockNEW();
        emptyline();
        
        //generate update method.
        appendString("protected void updateModule(Module mod){");
        increaseIdent();
        appendString("try{");
        appendString("service.storeModule(mod);");
        appendString("}catch(Exception e){");
        increaseIdent();
        appendString("log.error(\"updateModule\", e);");
        closeBlockNEW();
        closeBlockNEW();
		emptyline();

        //generate method for adding module listener.
        appendString("protected void addModuleListener(String moduleId, IModuleListener moduleListener){");
        increaseIdent();
        appendString("service.addModuleListener(moduleId, MY_OWNER_ID, moduleListener);");
        closeBlockNEW();
    	emptyline();

        appendString("protected Module getModule(String moduleId){");
        increaseIdent();
        appendString("try{");
        appendString("return service.getModule(MY_OWNER_ID, moduleId, true);");
        appendString("}catch(Exception e){");
        increaseIdent();
        appendString("log.error(\"getModule\", e);");
        closeBlockNEW();
        appendStatement("return null");
        closeBlockNEW();
        emptyline();
        
		return clazz;
	}
}
