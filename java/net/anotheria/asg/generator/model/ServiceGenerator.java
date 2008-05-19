/* ------------------------------------------------------------------------- *
$Source: /work/cvs/ano-doc/java/net/anotheria/asg/generator/model/ServiceGenerator.java,v $
$Author: lrosenberg $
$Date: 2007/06/07 23:40:19 $
$Revision: 1.1 $


Copyright 2004-2005 by FriendScout24 GmbH, Munich, Germany.
All rights reserved.

This software is the confidential and proprietary information
of FriendScout24 GmbH. ("Confidential Information").  You
shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement
you entered into with FriendScout24 GmbH.
See www.friendscout24.de for details.
** ------------------------------------------------------------------------- */
package net.anotheria.asg.generator.model;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.exception.ASGRuntimeException;
import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.model.db.JDBCBasedServiceGenerator;
import net.anotheria.asg.generator.model.db.JDBCPersistenceServiceGenerator;
import net.anotheria.asg.generator.model.db.PersistenceServiceDAOGenerator;
import net.anotheria.asg.generator.model.docs.CMSBasedServiceGenerator;
import net.anotheria.asg.generator.model.federation.FederationServiceGenerator;
import net.anotheria.asg.generator.model.inmemory.InMemoryServiceGenerator;
import net.anotheria.asg.generator.model.rmi.RMIServiceGenerator;
import net.anotheria.util.ExecutionTimer;

/**
 * TODO Please remain lrosenberg to comment ServiceGenerator.java
 * @author lrosenberg
 * @created on Feb 24, 2005
 */
public class ServiceGenerator extends AbstractGenerator implements IGenerator{
	
	private Context context;
	
	public List<FileEntry> generate(IGenerateable gmodule, Context context){
		
		MetaModule mod = (MetaModule)gmodule;
		
		this.context = context;
		String packageName = context.getServicePackageName(mod);
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ExecutionTimer timer = new ExecutionTimer("ServiceGenerator");
		
		
		//timer.startExecution(mod.getName()+"-Interface");
		ret.add(new FileEntry(FileEntry.package2path(packageName), getInterfaceName(mod), generateInterface(mod)));
		//timer.stopExecution(mod.getName()+"-Interface");
		//timer.startExecution(mod.getName()+"-Exception");
		ret.add(new FileEntry(FileEntry.package2path(packageName), getExceptionName(mod), generateException(mod)));
		//timer.stopExecution(mod.getName()+"-Exception");
		
		//add in memory genererator
		timer.startExecution(mod.getName()+"-InMem");
		InMemoryServiceGenerator inMemGen = new InMemoryServiceGenerator();
		ret.addAll(inMemGen.generate(gmodule, context));
		timer.stopExecution(mod.getName()+"-InMem");
		// - end in memory

		//addrmi genererator
		timer.startExecution(mod.getName()+"-RMI");
		RMIServiceGenerator rmiGen = new RMIServiceGenerator();
		ret.addAll(rmiGen.generate(gmodule, context));
		timer.stopExecution(mod.getName()+"-RMI");
		// - end rmiy
		
		
		if (mod.getStorageType()==StorageType.CMS){
			timer.startExecution(mod.getName()+"-CMS");
			CMSBasedServiceGenerator cmsGen = new CMSBasedServiceGenerator();
			ret.addAll(cmsGen.generate(gmodule, context));
			timer.stopExecution(mod.getName()+"-CMS");
		}
		
		if (mod.getStorageType()==StorageType.DB){
			timer.startExecution(mod.getName()+"-DB");
			
			timer.startExecution(mod.getName()+"-JDBC");
			JDBCPersistenceServiceGenerator jdbcGen = new JDBCPersistenceServiceGenerator();
			ret.addAll(jdbcGen.generate(gmodule, context));
			timer.stopExecution(mod.getName()+"-JDBC");
			
			timer.startExecution(mod.getName()+"-DAO");
			PersistenceServiceDAOGenerator daoGen = new PersistenceServiceDAOGenerator();
			ret.addAll(daoGen.generate(gmodule, context));
			timer.stopExecution(mod.getName()+"-DAO");

			timer.startExecution(mod.getName()+"-JDBC-Serv");
			JDBCBasedServiceGenerator servGen = new JDBCBasedServiceGenerator();
			ret.addAll(servGen.generate(gmodule, context));
			timer.stopExecution(mod.getName()+"-JDBC-Serv");

			//SQLGenerator sqlGen = new SQLGenerator();
			//ret.addAll(sqlGen.generate(gmodule, context));
			timer.stopExecution(mod.getName()+"-DB");
		}
		
		if (mod.getStorageType()==StorageType.FEDERATION){
			timer.startExecution(mod.getName()+"-Fed");
			FederationServiceGenerator cmsGen = new FederationServiceGenerator();
			ret.addAll(cmsGen.generate(gmodule, context));
			timer.stopExecution(mod.getName()+"-Fed");
		}
		
		//timer.printExecutionTimesOrderedByCreation();

		return ret;
	}
	
	private String getPackageName(MetaModule module){
		return context.getPackageName(module)+".service";
	}
	
	private String generateException(MetaModule module){

		startNewJob();
		append(CommentGenerator.generateJavaTypeComment(getExceptionName(module)));
 
	    appendStatement("package "+getPackageName(module));
	    append(emptyline());
	    appendImport(ASGRuntimeException.class.getName());
	    
	    appendComment("Base class for all exceptions thrown by implementations of "+getInterfaceName(module));
	    appendString("@SuppressWarnings(" + quote("serial") + ")");
	    appendString("public class "+getExceptionName(module)+" extends ASGRuntimeException{");
	    increaseIdent();
	    
	    appendString("public "+getExceptionName(module)+" (String message){" );
	    appendIncreasedStatement("super(message)");
	    appendString("}");
	    append(emptyline());
	    
	    appendString("public "+getExceptionName(module)+" (Throwable cause){" );
	    appendIncreasedStatement("super(cause)");
	    appendString("}");
	    append(emptyline());
	    
	    appendString("public "+getExceptionName(module)+" (String message, Throwable cause){" );
	    appendIncreasedStatement("super(message, cause)");
	    appendString("}");

	    append(closeBlock());
	    return getCurrentJobContent().toString();
	}

	private String generateInterface(MetaModule module){
	    
		startNewJob();
		append(CommentGenerator.generateJavaTypeComment(getInterfaceName(module)));
 
	    appendStatement("package "+getPackageName(module));
	    appendEmptyline();
	    appendImport("java.util.List");
	    appendImport("net.anotheria.util.sorter.SortType");
	    appendEmptyline();
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        appendImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    }
	    appendEmptyline();
	    
	    appendImport("net.anotheria.util.xml.XMLNode");
	    appendEmptyline();
	    appendImport("net.anotheria.anodoc.query2.DocumentQuery");
	   
	    appendImport("net.anotheria.anodoc.query2.QueryResult");
	    appendImport("net.anotheria.anodoc.query2.QueryProperty");
	    appendEmptyline();
	    appendImport("net.anotheria.asg.service.ASGService");
	    appendEmptyline();

	    appendString("public interface "+getInterfaceName(module)+" extends ASGService {");
	    increaseIdent();
	    
	    boolean containsAnyMultilingualDocs = false;

	    String throwsClause = " throws "+getExceptionName(module);
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        appendComment("Returns all "+doc.getMultiple()+" objects stored.");
	        appendStatement("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause);
	        appendEmptyline();
			appendComment("Returns all "+doc.getMultiple()+" objects sorted by given sortType.");
			appendStatement("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType)"+throwsClause);
			appendEmptyline();
	        appendComment("Deletes a "+doc.getName()+" object by id.");
	        appendStatement("public void delete"+doc.getName()+"(String id)"+throwsClause);
	        appendEmptyline();
	        appendComment("Deletes a "+doc.getName()+" object.");
	        appendStatement("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        appendEmptyline();
	        appendComment("Deletes multiple "+doc.getName()+" object.");
	        appendStatement("public void delete"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        appendEmptyline();
	        appendComment("Returns the "+doc.getName()+" object with the specified id.");
	        appendStatement("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause);
	        appendEmptyline();

	        appendComment("Imports a new "+doc.getName()+" object.\nReturns the created version.");
	        appendStatement("public "+doc.getName()+" import"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        appendEmptyline();

	        appendComment("Creates a new "+doc.getName()+" object.\nReturns the created version.");
	        appendStatement("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        appendEmptyline();
	        
	        appendComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.");
	        appendStatement("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        appendEmptyline();

	        appendComment("Updates a "+doc.getName()+" object.\nReturns the updated version.");
	        appendStatement("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        appendEmptyline();

	        appendComment("Updates mutiple "+doc.getName()+" objects.\nReturns the updated versions.");
	        appendStatement("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        appendEmptyline();
	        
	        
	        //special functions
	        appendComment("Returns all "+doc.getName()+" objects, where property with given name equals object.");
	        appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)"+throwsClause);
	        appendEmptyline();
			appendComment("Returns all "+doc.getName()+" objects, where property with given name equals object, sorted");
			appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)"+throwsClause);
			appendEmptyline();
			appendComment("Executes a query");
			appendStatement("public QueryResult executeQueryOn"+doc.getMultiple()+"(DocumentQuery query)"+throwsClause);
			appendEmptyline();
	        appendComment("Returns all "+doc.getName()+" objects, where property matches.");
	        appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(QueryProperty... property)"+throwsClause);
	        appendEmptyline();
			appendComment("Returns all "+doc.getName()+" objects, where property matches, sorted");
			appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)"+throwsClause);
			appendEmptyline();
			
			if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
				appendComment("In all documents of type "+doc.getName()+" copies all multilingual fields from sourceLanguage to targetLanguage");
				appendStatement("public void copyMultilingualAttributesInAll"+doc.getMultiple()+"(String sourceLanguage, String targetLanguage)"+throwsClause);
				appendEmptyline();
				containsAnyMultilingualDocs = true;
			}
	    }
	    
	    if (containsAnyMultilingualDocs){
			appendComment("Copies all multilingual fields from sourceLanguage to targetLanguage in all data objects (documents, vo) which are part of this module and managed by this service");
			appendStatement("public void copyMultilingualAttributesInAllObjects(String sourceLanguage, String targetLanguage)"+throwsClause);
			appendEmptyline();
	    }
	    
		appendComment("creates an xml element with all contained data.");
		appendStatement("public XMLNode exportToXML()"+throwsClause);
		appendEmptyline();
	    
	    append(closeBlock());
	    return getCurrentJobContent().toString();
	}
	
	
	public static String getExceptionName(MetaModule m){
	    return getServiceName(m)+"Exception";
	}

	
	public static String getInterfaceName(MetaModule module){
	    return "I"+getServiceName(module);
	}
	
	public static String getInterfaceImport(Context ctx, MetaModule m){
		return getPackageName(ctx,m)+"."+getInterfaceName(m);
	}
	
	public static String getExceptionImport(Context ctx, MetaModule m){
		return getPackageName(ctx,m)+"."+getExceptionName(m);
	}

	public static String getServiceName(MetaModule m){
	    return m.getName()+"Service";
	}
	
	

	public static String getFactoryName(MetaModule m){
	    return getServiceName(m)+"Factory";
	}
	
	public static String getFactoryImport(Context ctx, MetaModule m){
	    return getPackageName(ctx, m)+"."+getFactoryName(m);
	}

	public static String getImplementationName(MetaModule m){
	    return getServiceName(m)+"Impl";
	}
	
	
	
	/**
	 * @deprecated
	 * @param context
	 * @return
	 */
	public static String getPackageName(Context context){
	    return context.getPackageName()+".service";
	}
	public static String getPackageName(Context context, MetaModule module){
	    return context.getServicePackageName(module);
	}
}