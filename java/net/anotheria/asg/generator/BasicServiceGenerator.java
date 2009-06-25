/* ------------------------------------------------------------------------- *
$Source: /work/cvs/ano-doc/java/net/anotheria/asg/generator/BasicServiceGenerator.java,v $
$Author: lrosenberg $
$Date: 2007/06/07 23:40:19 $
$Revision: 1.5 $


Copyright 2004-2005 by FriendScout24 GmbH, Munich, Germany.
All rights reserved.

This software is the confidential and proprietary information
of FriendScout24 GmbH. ("Confidential Information").  You
shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement
you entered into with FriendScout24 GmbH.
See www.friendscout24.de for details.
** ------------------------------------------------------------------------- */
package net.anotheria.asg.generator;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.meta.MetaModule;


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
		
		clazz.addImport("org.apache.log4j.Logger");
		clazz.addImport("net.anotheria.asg.util.listener.IServiceListener");
		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");


		clazz.setAbstractClass(true);
		clazz.setName("BasicService");
		
		startClassBody();

		appendStatement("protected Logger log");
		emptyline();
		
		appendString("//Support for listeners.");
		appendStatement("private List<IServiceListener> listeners");

        //generate constructor
        appendString("protected BasicService(){");
        increaseIdent();
        appendStatement("log = Logger.getLogger(this.getClass())");
        appendStatement("listeners = new ArrayList<IServiceListener>()");
        append(closeBlock());
        emptyline();
        
        //support for listeners.
        appendString("public void addServiceListener(IServiceListener listener){");
        increaseIdent();
        appendStatement("listeners.add(listener)");
        append(closeBlock());
        emptyline();
        
        appendString("public void removeServiceListener(IServiceListener listener){");
        increaseIdent();
        appendStatement("listeners.remove(listener)");
        append(closeBlock());
        emptyline();
        
        appendString("public boolean hasServiceListeners(){");
        increaseIdent();
        appendStatement("return listeners.size() > 0");
        append(closeBlock());
        emptyline();
        
        appendString("protected List<IServiceListener> getServiceListeners(){");
        increaseIdent();
        appendStatement("return listeners");
        append(closeBlock());

		return clazz;
	}

	private GeneratedClass generateBasicCMSService(List<MetaModule> modules){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".service");

		clazz.addImport("net.anotheria.anodoc.data.Module");
		clazz.addImport("net.anotheria.anodoc.service.IModuleService");
		clazz.addImport("net.anotheria.anodoc.service.ModuleServiceFactory");

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
        append(closeBlock());
        emptyline();
        
        //generate constructor
        appendString("protected BasicCMSService(){");
        increaseIdent();
        appendStatement("service = ModuleServiceFactory.createModuleService()");
        append(closeBlock());
        emptyline();
        
        //generate update method.
        appendString("protected void updateModule(Module mod){");
        increaseIdent();
        appendString("try{");
        appendString("service.storeModule(mod);");
        appendString("}catch(Exception e){");
        increaseIdent();
        appendString("log.error(\"updateModule\", e);");
        append(closeBlock());
        append(closeBlock());
            
    

        appendString("protected Module getModule(String moduleId){");
        increaseIdent();
        appendString("try{");
        appendString("return service.getModule(MY_OWNER_ID, moduleId, true);");
        appendString("}catch(Exception e){");
        increaseIdent();
        appendString("log.error(\"getModule\", e);");
        append(closeBlock());
        appendStatement("return null");
        append(closeBlock());
        emptyline();
        
		return clazz;
	}
}
