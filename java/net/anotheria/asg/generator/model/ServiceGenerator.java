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

import quicktime.std.clocks.TimeRecord;

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
	    String ret = "";
	    
	    ret += CommentGenerator.generateJavaTypeComment(getExceptionName(module));
 
	    ret += writeStatement("package "+getPackageName(module));
	    ret += emptyline();
	    ret += writeImport(ASGRuntimeException.class.getName());
	    
	    ret += writeComment("Base class for all exceptions thrown by implementations of "+getInterfaceName(module));
	    ret += writeString("public class "+getExceptionName(module)+" extends ASGRuntimeException{");
	    increaseIdent();
	    
	    ret += writeString("public "+getExceptionName(module)+" (String message){" );
	    ret += writeIncreasedStatement("super(message)");
	    ret += writeString("}");
	    
	    ret += closeBlock();
	    return ret;
	}

	private String generateInterface(MetaModule module){
	    StringBuilder ret = new StringBuilder(5000);
	    
	    ret.append(CommentGenerator.generateJavaTypeComment(getInterfaceName(module)));
 
	    ret.append(writeStatement("package "+getPackageName(module)));
	    ret.append(emptyline());
	    ret.append(writeImport("java.util.List"));
	    ret.append(writeImport("net.anotheria.util.sorter.SortType"));
	    ret.append(emptyline());
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        ret.append(writeImport(DataFacadeGenerator.getDocumentImport(context, doc)));
	    }
	    ret.append(emptyline());
	    
	    ret.append(writeImport("net.anotheria.util.xml.XMLNode"));
	    ret.append(emptyline());
	    ret.append(writeImport("net.anotheria.anodoc.query2.DocumentQuery"));
	   
	    ret.append(writeImport("net.anotheria.anodoc.query2.QueryResult"));
	    ret.append(writeImport("net.anotheria.anodoc.query2.QueryProperty"));
	    ret.append(emptyline());
	    ret.append(writeImport("net.anotheria.asg.service.ASGService"));
	    ret.append(emptyline());

	    ret.append(writeString("public interface "+getInterfaceName(module)+" extends ASGService {"));
	    increaseIdent();
	    
	    boolean containsAnyMultilingualDocs = false;

	    String throwsClause = " throws "+getExceptionName(module);
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        ret.append(writeComment("Returns all "+doc.getMultiple()+" objects stored."));
	        ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause));
	        ret.append(emptyline());
			ret.append(writeComment("Returns all "+doc.getMultiple()+" objects sorted by given sortType."));
			ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType)"+throwsClause));
			ret.append(emptyline());
	        ret.append(writeComment("Deletes a "+doc.getName()+" object by id."));
	        ret.append(writeStatement("public void delete"+doc.getName()+"(String id)"+throwsClause));
	        ret.append(emptyline());
	        ret.append(writeComment("Deletes a "+doc.getName()+" object."));
	        ret.append(writeStatement("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause));
	        ret.append(emptyline());
	        ret.append(writeComment("Returns the "+doc.getName()+" object with the specified id."));
	        ret.append(writeStatement("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause));
	        ret.append(emptyline());
	        ret.append(writeComment("Creates a new "+doc.getName()+" object.\nReturns the created version."));
	        ret.append(writeStatement("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause));
	        ret.append(emptyline());
	        
	        ret.append(writeComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions."));
	        ret.append(writeStatement("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause));
	        ret.append(emptyline());

	        ret.append(writeComment("Updates a "+doc.getName()+" object.\nReturns the updated version."));
	        ret.append(writeStatement("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause));
	        ret.append(emptyline());

	        ret.append(writeComment("Updates mutiple "+doc.getName()+" objects.\nReturns the updated versions."));
	        ret.append(writeStatement("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause));
	        ret.append(emptyline());
	        
	        
	        //special functions
	        ret.append(writeComment("Returns all "+doc.getName()+" objects, where property with given name equals object."));
	        ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)"+throwsClause));
	        ret.append(emptyline());
			ret.append(writeComment("Returns all "+doc.getName()+" objects, where property with given name equals object, sorted"));
			ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)"+throwsClause));
			ret.append(emptyline());
			ret.append(writeComment("Executes a query"));
			ret.append(writeStatement("public QueryResult executeQueryOn"+doc.getMultiple()+"(DocumentQuery query)"+throwsClause));
			ret.append(emptyline());
	        ret.append(writeComment("Returns all "+doc.getName()+" objects, where property matches."));
	        ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(QueryProperty... property)"+throwsClause));
	        ret.append(emptyline());
			ret.append(writeComment("Returns all "+doc.getName()+" objects, where property matches, sorted"));
			ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)"+throwsClause));
			ret.append(emptyline());
			
			if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
				ret.append(writeComment("In all documents of type "+doc.getName()+" copies all multilingual fields from sourceLanguage to targetLanguage"));
				ret.append(writeStatement("public void copyMultilingualAttributesInAll"+doc.getMultiple()+"(String sourceLanguage, String targetLanguage)"+throwsClause));
				ret.append(emptyline());
				containsAnyMultilingualDocs = true;
			}
	    }
	    
	    if (containsAnyMultilingualDocs){
			ret.append(writeComment("Copies all multilingual fields from sourceLanguage to targetLanguage in all data objects (documents, vo) which are part of this module and managed by this service"));
			ret.append(writeStatement("public void copyMultilingualAttributesInAllObjects(String sourceLanguage, String targetLanguage)"+throwsClause));
			ret.append(emptyline());
	    }
	    
		ret.append(writeComment("creates an xml element with all contained data."));
		ret.append(writeStatement("public XMLNode exportToXML()"+throwsClause));
		ret.append(emptyline());
	    
	    ret.append(closeBlock());
	    return ret.toString();
	}
	
	
	public static String getExceptionName(MetaModule m){
	    return getServiceName(m)+"Exception";
	}

	
	public static String getInterfaceName(MetaModule m){
	    return "I"+getServiceName(m);
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