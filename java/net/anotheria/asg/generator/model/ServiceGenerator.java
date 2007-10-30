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

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
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
		
		ret.add(new FileEntry(FileEntry.package2path(packageName), getInterfaceName(mod), generateInterface(mod)));
		
		if (mod.getStorageType()==StorageType.CMS){
			CMSBasedServiceGenerator cmsGen = new CMSBasedServiceGenerator();
			ret.addAll(cmsGen.generate(gmodule, context));
		}
		
		if (mod.getStorageType()==StorageType.DB){
			JDBCPersistenceServiceGenerator jdbcGen = new JDBCPersistenceServiceGenerator();
			ret.addAll(jdbcGen.generate(gmodule, context));
			PersistenceServiceDAOGenerator daoGen = new PersistenceServiceDAOGenerator();
			ret.addAll(daoGen.generate(gmodule, context));
			JDBCBasedServiceGenerator servGen = new JDBCBasedServiceGenerator();
			ret.addAll(servGen.generate(gmodule, context));
		}
		
		if (mod.getStorageType()==StorageType.FEDERATION){
			FederationServiceGenerator cmsGen = new FederationServiceGenerator();
			ret.addAll(cmsGen.generate(gmodule, context));
		}

		return ret;
	}
	
	private String getPackageName(MetaModule module){
		return context.getPackageName(module)+".service";
	}
	
	private String generateInterface(MetaModule module){
	    String ret = "";
	    
	    ret += CommentGenerator.generateJavaTypeComment(getInterfaceName(module));
 
	    ret += writeStatement("package "+getPackageName(module));
	    ret += emptyline();
	    ret += writeImport("java.util.List");
	    ret += writeImport("net.anotheria.util.sorter.SortType");
	    ret += emptyline();
	    
	    List docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    }
	    ret += emptyline();
	    
	    ret += writeImport("org.jdom.Element");
	    ret += emptyline();
	    ret += writeImport("net.anotheria.anodoc.query2.DocumentQuery");
	    ret += writeImport("net.anotheria.anodoc.query2.QueryResult");
	    ret += writeImport("net.anotheria.anodoc.query2.QueryProperty");
	    ret += emptyline();

	    ret += writeString("public interface "+getInterfaceName(module)+"{");
	    increaseIdent();

	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        ret += writeComment("Returns all "+doc.getMultiple()+" objects stored.");
	        ret += writeStatement("public "+listDecl+" get"+doc.getMultiple()+"()");
	        ret += emptyline();
			ret += writeComment("Returns all "+doc.getMultiple()+" objects sorted by given sortType.");
			ret += writeStatement("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType)");
			ret += emptyline();
	        ret += writeComment("Deletes a "+doc.getName()+" object by id.");
	        ret += writeStatement("public void delete"+doc.getName()+"(String id)");
	        ret += emptyline();
	        ret += writeComment("Deletes a "+doc.getName()+" object.");
	        ret += writeStatement("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")");
	        ret += emptyline();
	        ret += writeComment("Returns the "+doc.getName()+" object with the specified id.");
	        ret += writeStatement("public "+doc.getName()+" get"+doc.getName()+"(String id)");
	        ret += emptyline();
	        ret += writeComment("Creates a new "+doc.getName()+" object.\nReturns the created version.");
	        ret += writeStatement("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")");
	        ret += emptyline();
	        ret += writeComment("Updates a "+doc.getName()+" object.\nReturns the updated version.");
	        ret += writeStatement("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")");
	        ret += emptyline();
	        //special functions
	        ret += writeComment("Returns all "+doc.getName()+" objects, where property with given name equals object.");
	        ret += writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)");
	        ret += emptyline();
			ret += writeComment("Returns all "+doc.getName()+" objects, where property with given name equals object, sorted");
			ret += writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)");
			ret += emptyline();
			ret += writeComment("Executes a query");
			ret += writeStatement("public QueryResult executeQueryOn"+doc.getMultiple()+"(DocumentQuery query)");
			ret += emptyline();
	        ret += writeComment("Returns all "+doc.getName()+" objects, where property matches.");
	        ret += writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(QueryProperty... property)");
	        ret += emptyline();
			ret += writeComment("Returns all "+doc.getName()+" objects, where property matches, sorted");
			ret += writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)");
			ret += emptyline();
	    }
	    
		ret += writeComment("creates an xml element with all contained data.");
		ret += writeStatement("public Element exportToXML()");
		ret += emptyline();
	    
	    ret += closeBlock();
	    return ret;
	}
	
	
	public static String getInterfaceName(MetaModule m){
	    return "I"+getServiceName(m);
	}
	
	public static String getInterfaceImport(Context ctx, MetaModule m){
		return getPackageName(ctx,m)+"."+getInterfaceName(m);
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