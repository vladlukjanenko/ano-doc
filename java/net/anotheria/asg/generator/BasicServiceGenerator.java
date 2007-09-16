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
	
	
	public List<FileEntry> generate(List modules, Context context){
		List<FileEntry> ret = new ArrayList<FileEntry>(); 
		
		ret.add(new FileEntry(FileEntry.package2path(context.getPackageName(MetaModule.SHARED)+".service"), "BasicService", generateBasicService(modules, context)));
		ret.add(new FileEntry(FileEntry.package2path(context.getPackageName(MetaModule.SHARED)+".service"), "BasicCMSService", generateBasicCMSService(modules, context)));
		
		return ret;
	}
	
	private String generateBasicService(List modules, Context context){
		
		
		String ret = "";
		
		ret += writeStatement("package "+context.getPackageName(MetaModule.SHARED)+".service");
		ret += emptyline();


		ret += writeImport("net.anotheria.util.sorter.Sorter");
		ret += writeImport("net.anotheria.util.sorter.QuickSorter");
		ret += writeImport("org.apache.log4j.Logger");
		ret += emptyline();
		ret += writeImport("net.anotheria.asg.util.listener.IServiceListener");
		ret += writeImport("java.util.List");
		ret += writeImport("java.util.ArrayList");


		//ret += emptyline();
		//ret += writeImport(context.getPackageName()+".data.*");
		
		ret += emptyline();
		ret += writeString("public abstract class BasicService{");
		increaseIdent();
		ret += emptyline();

		ret += writeStatement("protected Sorter sorter");

		ret += writeStatement("protected Logger log");
		ret += emptyline();
		
		ret += writeString("//Support for listeners.");
		ret += writeStatement("private List<IServiceListener> listeners");

        //generate constructor
        ret += writeString("protected BasicService(){");
        increaseIdent();
        ret += writeStatement("log = Logger.getLogger(this.getClass())");
        ret += writeStatement("sorter = new QuickSorter()");
        ret += writeStatement("listeners = new ArrayList<IServiceListener>()");
        ret += closeBlock();
        ret += emptyline();
        
        //support for listeners.
        ret += writeString("public void addServiceListener(IServiceListener listener){");
        increaseIdent();
        ret += writeStatement("listeners.add(listener)");
        ret += closeBlock();
        ret += emptyline();
        
        ret += writeString("public void removeServiceListener(IServiceListener listener){");
        increaseIdent();
        ret += writeStatement("listeners.remove(listener)");
        ret += closeBlock();
        ret += emptyline();
        
        ret += writeString("public boolean hasServiceListeners(){");
        increaseIdent();
        ret += writeStatement("return listeners.size() > 0");
        ret += closeBlock();
        ret += emptyline();
        
        ret += writeString("protected List getServiceListeners(){");
        increaseIdent();
        ret += writeStatement("return listeners");
        ret += closeBlock();

		ret += closeBlock();

			
		return ret;

		
	}

	private String generateBasicCMSService(List modules, Context context){
		
		
		String ret = "";
		
		ret += writeStatement("package "+context.getPackageName(MetaModule.SHARED)+".service");
		ret += emptyline();

		ret += writeImport("net.anotheria.anodoc.data.Module");
		ret += writeImport("net.anotheria.anodoc.service.IModuleService");
		ret += writeImport("net.anotheria.anodoc.service.ModuleServiceFactory");
		ret += emptyline();


		//ret += emptyline();
		//ret += writeImport(context.getPackageName()+".data.*");
		
		ret += emptyline();
		ret += writeString("public abstract class BasicCMSService extends BasicService{");
		increaseIdent();
		ret += emptyline();
		
		ret += writeStatement("public static final String MY_OWNER_ID = "+quote(context.getOwner()));
		ret += writeStatement("protected IModuleService service");
		ret += emptyline();

		ret += writeString("static{");
		increaseIdent();
		ret += writeString("AnoDocConfigurator.configure();");
        ret += closeBlock();
        ret += emptyline();
        
        //generate constructor
        ret += writeString("protected BasicCMSService(){");
        increaseIdent();
        ret += writeStatement("service = ModuleServiceFactory.createModuleService()");
        ret += closeBlock();
        ret += emptyline();
        
        //generate update method.
        ret += writeString("protected void updateModule(Module mod){");
        increaseIdent();
        ret += writeString("try{");
        ret += writeString("service.storeModule(mod);");
        ret += writeString("}catch(Exception e){");
        increaseIdent();
        ret += writeString("log.error(\"updateModule\", e);");
        ret += closeBlock();
        ret += closeBlock();
            
    

        ret += writeString("protected Module getModule(String moduleId){");
        increaseIdent();
        ret += writeString("try{");
        ret += writeString("return service.getModule(MY_OWNER_ID, moduleId, true);");
        ret += writeString("}catch(Exception e){");
        increaseIdent();
        ret += writeString("log.error(\"getModule\", e);");
        ret += closeBlock();
        ret += writeStatement("return null");
        ret += closeBlock();
        ret += emptyline();
        
		ret += closeBlock();
		return ret;

		
	}
}


/* ------------------------------------------------------------------------- *
 * $Log: BasicServiceGenerator.java,v $
 * Revision 1.5  2007/06/07 23:40:19  lrosenberg
 * added db functionality
 *
 * Revision 1.4  2006/12/28 14:26:57  lrosenberg
 * *** empty log message ***
 *
 * Revision 1.3  2006/12/27 23:47:59  lrosenberg
 * *** empty log message ***
 *
 * Revision 1.2  2006/03/07 16:04:44  lrosenberg
 * *** empty log message ***
 *
 * Revision 1.1  2005/10/20 21:20:12  lro
 * *** empty log message ***
 *
 * Revision 1.1  2005/02/24 19:48:40  lro
 * *** empty log message ***
 *
 */