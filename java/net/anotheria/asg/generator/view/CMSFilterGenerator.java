package net.anotheria.asg.generator.view;


import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generator class for the CMSFilter.
 * @author dmetelin
 */
public class CMSFilterGenerator extends AbstractGenerator{
	
	public List<FileEntry> generate() {
		List<FileEntry> ret = new ArrayList<FileEntry>(); 
		try{
			ret.add(new FileEntry(generateCMSFilter()));
		}catch(Exception e){
			System.out.println("CMSFilterGenerator error: " + e.getMessage());
			e.printStackTrace();
		}
		return ret;
	}
	
	private GeneratedClass generateCMSFilter(){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackageName());
		
		clazz.addImport(Arrays.class);
		clazz.addImport(List.class);
		clazz.addImport(Logger.class);
		clazz.addImport(LoggerFactory.class);
		clazz.addImport(File.class);
		clazz.addImport("javax.servlet.FilterConfig");
		clazz.addImport("javax.servlet.ServletException");
		clazz.addImport("net.anotheria.maf.MAFFilter");
		clazz.addImport("net.anotheria.maf.action.ActionMappingsConfigurator");
        clazz.addImport("net.anotheria.anosite.cms.user.CMSUserManager");

		clazz.setParent("MAFFilter");
		clazz.setName("CMSFilter");
		appendGenerationPoint("generateCMSFilter()");

		startClassBody();
		
		appendComment("Logger initialization.");
		appendStatement("private static final Logger log = LoggerFactory.getLogger(CMSFilter.class);");
		
		emptyline();
		
		appendString("@Override");
		openFun("public void init(FilterConfig config) throws ServletException");
		appendStatement("log.info(\"----  Initing CMS...  ------\")");
		appendStatement("super.init(config)");

        emptyline();

        appendStatement("log.info(\"----  Initing CMS user manager...  ------\")");
        appendStatement("CMSUserManager.init()");
		
		closeBlock("init");
		
		emptyline();
		
		appendString("@Override");
		openFun("protected List<ActionMappingsConfigurator> getConfigurators()");
		appendStatement("return Arrays.asList(new ActionMappingsConfigurator[] { new CMSMappingsConfigurator()})");
		closeBlock("getConfigurators");
		
		return clazz;
	}
	
	private String getPackageName(){
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED) + ".filter";
	}
}
