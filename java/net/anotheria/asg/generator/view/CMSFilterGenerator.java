package net.anotheria.asg.generator.view;


import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;

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
		
		clazz.addImport("java.util.Arrays");
		clazz.addImport("java.util.List");
		clazz.addImport("javax.servlet.FilterConfig");
		clazz.addImport("javax.servlet.ServletException");
		clazz.addImport("net.anotheria.maf.MAFFilter");
		clazz.addImport("net.anotheria.maf.action.ActionMappingsConfigurator");
		clazz.addImport("org.apache.log4j.Logger");

		clazz.setParent("MAFFilter");
		clazz.setName("CMSFilter");

		startClassBody();
		
		appendComment("Logger initialization.");
		appendStatement("private static final Logger log = Logger.getLogger(CMSFilter.class);");
		
		emptyline();
		
		appendString("@Override");
		openFun("public void init(FilterConfig config) throws ServletException");
		appendStatement("super.init(config)");
		appendStatement("log.info(\"----  Initing CMS...  ------\")");
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