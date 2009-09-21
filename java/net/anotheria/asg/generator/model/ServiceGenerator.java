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
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.TypeOfClass;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.model.db.JDBCBasedServiceGenerator;
import net.anotheria.asg.generator.model.db.JDBCPersistenceServiceGenerator;
import net.anotheria.asg.generator.model.db.PersistenceServiceDAOGenerator;
import net.anotheria.asg.generator.model.docs.CMSBasedServiceGenerator;
import net.anotheria.asg.generator.model.federation.FederationServiceGenerator;
import net.anotheria.asg.generator.model.fixture.FixtureServiceGenerator;
import net.anotheria.asg.generator.model.inmemory.InMemoryServiceGenerator;
import net.anotheria.asg.generator.model.rmi.RMIServiceGenerator;
import net.anotheria.util.ExecutionTimer;
import net.anotheria.util.sorter.SortType;

/**
 * Controls different sub generators for generation of the service layer. Generates factories and interfaces.
 * @author lrosenberg
 * @created on Feb 24, 2005
 */
public class ServiceGenerator extends AbstractGenerator implements IGenerator{
	
	/**
	 * Generates the service layer for a MetaModule.
	 */
	public List<FileEntry> generate(IGenerateable gmodule){
		
		MetaModule mod = (MetaModule)gmodule;
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ExecutionTimer timer = new ExecutionTimer("ServiceGenerator");
		
		
		//timer.startExecution(mod.getName()+"-Interface");
		ret.add(new FileEntry(generateInterface(mod)));
		//timer.stopExecution(mod.getName()+"-Interface");
		//timer.startExecution(mod.getName()+"-Exception");
		ret.add(new FileEntry(generateException(mod)));
		//timer.stopExecution(mod.getName()+"-Exception");
		
		//add in memory genererator
		timer.startExecution(mod.getName()+"-InMem");
		InMemoryServiceGenerator inMemGen = new InMemoryServiceGenerator();
		ret.addAll(inMemGen.generate(gmodule));
		timer.stopExecution(mod.getName()+"-InMem");
		// - end in memory

		//add fixture generator
		timer.startExecution(mod.getName()+"-Fixture");
		FixtureServiceGenerator fixtureGen = new FixtureServiceGenerator();
		ret.addAll(fixtureGen.generate(gmodule));
		timer.stopExecution(mod.getName()+"-Fixture");
		// - end fixture

		
		
		//addrmi genererator
		timer.startExecution(mod.getName()+"-RMI");
		RMIServiceGenerator rmiGen = new RMIServiceGenerator();
		ret.addAll(rmiGen.generate(gmodule));
		timer.stopExecution(mod.getName()+"-RMI");
		// - end rmiy
		
		
		if (mod.getStorageType()==StorageType.CMS){
			timer.startExecution(mod.getName()+"-CMS");
			CMSBasedServiceGenerator cmsGen = new CMSBasedServiceGenerator();
			ret.addAll(cmsGen.generate(gmodule));
			timer.stopExecution(mod.getName()+"-CMS");
		}
		
		if (mod.getStorageType()==StorageType.DB){
			timer.startExecution(mod.getName()+"-DB");
			
			timer.startExecution(mod.getName()+"-JDBC");
			JDBCPersistenceServiceGenerator jdbcGen = new JDBCPersistenceServiceGenerator();
			ret.addAll(jdbcGen.generate(gmodule));
			timer.stopExecution(mod.getName()+"-JDBC");
			
			timer.startExecution(mod.getName()+"-DAO");
			PersistenceServiceDAOGenerator daoGen = new PersistenceServiceDAOGenerator();
			ret.addAll(daoGen.generate(gmodule));
			timer.stopExecution(mod.getName()+"-DAO");

			timer.startExecution(mod.getName()+"-JDBC-Serv");
			JDBCBasedServiceGenerator servGen = new JDBCBasedServiceGenerator();
			ret.addAll(servGen.generate(gmodule));
			timer.stopExecution(mod.getName()+"-JDBC-Serv");

			//SQLGenerator sqlGen = new SQLGenerator();
			//ret.addAll(sqlGen.generate(gmodule, context));
			timer.stopExecution(mod.getName()+"-DB");
		}
		
		if (mod.getStorageType()==StorageType.FEDERATION){
			timer.startExecution(mod.getName()+"-Fed");
			FederationServiceGenerator cmsGen = new FederationServiceGenerator();
			ret.addAll(cmsGen.generate(gmodule));
			timer.stopExecution(mod.getName()+"-Fed");
		}
		
		//timer.printExecutionTimesOrderedByCreation();

		return ret;
	}
	
	/**
	 * Returns the package name for the service generation.
	 * @param module
	 * @return
	 */
	private String getPackageName(MetaModule module){
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(module)+".service";
	}
	
	/**
	 * Generates the base exception class for a module.
	 * @param module
	 * @return
	 */
	private GeneratedClass generateException(MetaModule module){

		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getExceptionName(module), this));
 
	    clazz.setPackageName(getPackageName(module));
	    clazz.addImport(ASGRuntimeException.class);
	    
	    clazz.setClazzComment("Base class for all exceptions thrown by implementations of "+getInterfaceName(module));
	    //TODO FIXME
	    //appendString("@SuppressWarnings(" + quote("serial") + ")");
	    clazz.setName(getExceptionName(module));
	    clazz.setParent(ASGRuntimeException.class);
	    
	    startClassBody();
	    appendString("public "+getExceptionName(module)+" (String message){" );
	    appendIncreasedStatement("super(message)");
	    appendString("}");
	    emptyline();
	    
	    appendString("public "+getExceptionName(module)+" (Throwable cause){" );
	    appendIncreasedStatement("super(cause)");
	    appendString("}");
	    emptyline();
	    
	    appendString("public "+getExceptionName(module)+" (String message, Throwable cause){" );
	    appendIncreasedStatement("super(message, cause)");
	    appendString("}");

	    return clazz;
	}
	
	/**
	 * Generates the service interface for the module.
	 * @param module
	 * @return
	 */
	private GeneratedClass generateInterface(MetaModule module){
	    
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getInterfaceName(module), this));
 
	    clazz.setPackageName(getPackageName(module));
	    clazz.addImport(List.class);
	    clazz.addImport(SortType.class);
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
	    }
	    
	    clazz.addImport("net.anotheria.util.xml.XMLNode");
	    clazz.addImport("net.anotheria.util.slicer.Segment");
	    clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
	   
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
	    clazz.addImport("net.anotheria.asg.service.ASGService");

	    clazz.setType(TypeOfClass.INTERFACE);
	    clazz.setName(getInterfaceName(module));
	    clazz.setParent("ASGService");
	    
	    boolean containsAnyMultilingualDocs = false;

	    String throwsClause = " throws "+getExceptionName(module);
	    startClassBody();
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        appendComment("Returns all "+doc.getMultiple()+" objects stored.");
	        appendStatement("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause);
	        emptyline();
			appendComment("Returns all "+doc.getMultiple()+" objects sorted by given sortType.");
			appendStatement("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType)"+throwsClause);
			emptyline();

			//get multiple by id
//			appendComment("Returns the "+doc.getName()+" objects with the specified ids.");
//	        appendStatement("public "+listDecl+" get"+doc.getMultiple()+"(List<String> ids)"+throwsClause);
//	        emptyline();

//	        appendComment("Returns the "+doc.getName()+" objects with the specified ids, sorted by given sorttype.");
//	        appendStatement("public "+listDecl+" get"+doc.getMultiple()+"(List<String> ids, SortType sortType)"+throwsClause);
//	        emptyline();

	        appendComment("Deletes a "+doc.getName()+" object by id.");
	        appendStatement("public void delete"+doc.getName()+"(String id)"+throwsClause);
	        emptyline();
	        appendComment("Deletes a "+doc.getName()+" object.");
	        appendStatement("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        emptyline();
	        appendComment("Deletes multiple "+doc.getName()+" object.");
	        appendStatement("public void delete"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        emptyline();
	        appendComment("Returns the "+doc.getName()+" object with the specified id.");
	        appendStatement("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause);
	        emptyline();


	        appendComment("Imports a new "+doc.getName()+" object.\nReturns the created version.");
	        appendStatement("public "+doc.getName()+" import"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        emptyline();

            appendComment("Imports multiple new  "+doc.getName()+" object.\nReturns the created versions.");
            appendStatement("public "+listDecl+" import"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        emptyline();
            

	        appendComment("Creates a new "+doc.getName()+" object.\nReturns the created version.");
	        appendStatement("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        emptyline();
	        
	        appendComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.");
	        appendStatement("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        emptyline();

	        appendComment("Updates a "+doc.getName()+" object.\nReturns the updated version.");
	        appendStatement("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        emptyline();

	        appendComment("Updates mutiple "+doc.getName()+" objects.\nReturns the updated versions.");
	        appendStatement("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        emptyline();
	        
	        
	        //special functions
	        appendComment("Returns all "+doc.getName()+" objects, where property with given name equals object.");
	        appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)"+throwsClause);
	        emptyline();
			appendComment("Returns all "+doc.getName()+" objects, where property with given name equals object, sorted");
			appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)"+throwsClause);
			emptyline();
			appendComment("Executes a query");
			appendStatement("public QueryResult executeQueryOn"+doc.getMultiple()+"(DocumentQuery query)"+throwsClause);
			emptyline();
	        appendComment("Returns all "+doc.getName()+" objects, where property matches.");
	        appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(QueryProperty... property)"+throwsClause);
	        emptyline();
			appendComment("Returns all "+doc.getName()+" objects, where property matches, sorted");
			appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)"+throwsClause);
			emptyline();
			
			// get elements COUNT
			appendComment("Returns all " + doc.getMultiple() + " count.");
			appendStatement("public int get" + doc.getMultiple() + "Count()" + throwsClause);
			emptyline();
			// end get elements COUNT

			// get elements Segment
			appendComment("Returns " + doc.getMultiple() + " objects segment.");
			appendStatement("public " + listDecl + " get" + doc.getMultiple() + "(Segment aSegment)" + throwsClause);
			emptyline();
			// end get elements Segment

			// get elements Segment with FILTER
			appendComment("Returns " + doc.getName() + " objects segment, where property matches.");
			appendStatement("public " + listDecl + " get" + doc.getMultiple() + "ByProperty(Segment aSegment, QueryProperty... aProperty)"
					+ throwsClause);
			emptyline();
			// end get elements Segment with FILTER

			// get elements Segment with SORTING, FILTER
			appendComment("Returns " + doc.getName() + " objects segment, where property matches, sorted.");
			appendStatement("public " + listDecl + " get" + doc.getMultiple()
					+ "ByProperty(Segment aSegment, SortType aSortType, QueryProperty... aProperty)" + throwsClause);
			emptyline();
			// end get elements Segment with SORTING, FILTER

			if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
				appendComment("In all documents of type "+doc.getName()+" copies all multilingual fields from sourceLanguage to targetLanguage");
				appendStatement("public void copyMultilingualAttributesInAll"+doc.getMultiple()+"(String sourceLanguage, String targetLanguage)"+throwsClause);
				emptyline();
				containsAnyMultilingualDocs = true;
			}
			
			//this method checks whether a document with the given document set exists or no.
	    }
	    
	    if (containsAnyMultilingualDocs){
			appendComment("Copies all multilingual fields from sourceLanguage to targetLanguage in all data objects (documents, vo) which are part of this module and managed by this service");
			appendStatement("public void copyMultilingualAttributesInAllObjects(String sourceLanguage, String targetLanguage)"+throwsClause);
			emptyline();
	    }
	    
		appendComment("creates an xml element with all contained data.");
		appendStatement("public XMLNode exportToXML()"+throwsClause);
		emptyline();
	    
		appendComment("creates an xml element with all contained data but only selected languages in multilingual attributes");
		appendStatement("public XMLNode exportToXML(String[] languages)"+throwsClause);

	    return clazz;
	}
	
	/**
	 * Returns the name of the base exception class for the service for the given module.
	 * @param m
	 * @return
	 */
	public static String getExceptionName(MetaModule m){
	    return getServiceName(m)+"Exception";
	}

	/**
	 * Returns the interface name for the service for the module. 
	 * @param module
	 * @return
	 */
	public static String getInterfaceName(MetaModule module){
	    return "I"+getServiceName(module);
	}
	
	/**
	 * Returns the import (package and class name) for the service interface for the given module. 
	 * @param m
	 * @return
	 */
	public static String getInterfaceImport(MetaModule m){
		return getPackageName(GeneratorDataRegistry.getInstance().getContext(),m)+"."+getInterfaceName(m);
	}
	
	/**
	 * Returns the base exception name for the service for this module
	 * @param m the module.
	 * @return
	 */
	public static String getExceptionImport(MetaModule m){
		return getPackageName(GeneratorDataRegistry.getInstance().getContext(),m)+"."+getExceptionName(m);
	}
	
	/**
	 * Returns the service name for this module.
	 * @param m
	 * @return
	 */
	public static String getServiceName(MetaModule m){
	    return m.getName()+"Service";
	}

	/**
	 * Returns the factory name for the service for a metamodule.
	 * @param m
	 * @return
	 */
	public static String getFactoryName(MetaModule m){
	    return getServiceName(m)+"Factory";
	}
	/**
	 * Returns the import for the factory for the service for a metamodule.
	 * @param m
	 * @return
	 */
	public static String getFactoryImport(MetaModule m){
	    return getPackageName(GeneratorDataRegistry.getInstance().getContext(), m)+"."+getFactoryName(m);
	}

	public static String getImplementationName(MetaModule m){
	    return getServiceName(m)+"Impl";
	}
	
	public static String getPackageName(Context context, MetaModule module){
	    return context.getServicePackageName(module);
	}
}