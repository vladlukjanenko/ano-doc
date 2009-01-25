package net.anotheria.asg.generator.model.db;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.AbstractServiceGenerator;
import net.anotheria.asg.generator.model.DataFacadeGenerator;

/**
 * Generates a DB-Backed implementation of a module interface and the according factory.
 * @author another
 *
 */
public class JDBCBasedServiceGenerator extends AbstractServiceGenerator implements IGenerator{
	
	private Context context;
	
	public List<FileEntry> generate(IGenerateable gmodule, Context context){
		
		MetaModule mod = (MetaModule)gmodule;
		
		this.context = context;
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ret.add(new FileEntry(generateFactory(mod)));
		ret.add(new FileEntry(generateImplementation(mod)));
		
		return ret;
	}
	
	/**
	 * Generates the implementation
	 * @param module the metamodule to generate
	 * @return
	 */
	private GeneratedClass generateImplementation(MetaModule module){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
	    clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The implementation of the "+getInterfaceName(module)+"."));
	    clazz.setPackageName(getPackageName(module));
	    	
	    clazz.addImport("java.util.List");
	    clazz.addImport("net.anotheria.util.sorter.SortType");
	    clazz.addImport("net.anotheria.util.sorter.StaticQuickSorter");
	    clazz.addImport(context.getServicePackageName(MetaModule.SHARED)+".BasicService");
	    //ret.append(emptyline());
	    clazz.addImport(JDBCPersistenceServiceGenerator.getInterfaceImport(context, module));
	    clazz.addImport(JDBCPersistenceServiceGenerator.getFactoryImport(context, module));
	    clazz.addImport(JDBCPersistenceServiceGenerator.getExceptionImport(context, module));

	    clazz.addImport("net.anotheria.asg.util.listener.IServiceListener");
	    clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResultEntry");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
	    
	    clazz.addImport("net.anotheria.util.xml.XMLNode");
	    clazz.addImport("net.anotheria.util.xml.XMLAttribute");

	    clazz.setName(getImplementationName(module));
	    clazz.setParent("BasicService");
	    clazz.addInterface(getInterfaceName(module));
	    
	    startClassBody();
	    appendStatement("private static "+getImplementationName(module)+" instance");
	    appendEmptyline();
	    
	    appendStatement("private "+JDBCPersistenceServiceGenerator.getInterfaceName(module)+" pService");
	    
	    appendString("private "+getImplementationName(module)+"(){");
	    increaseIdent();
	    appendStatement("pService = "+JDBCPersistenceServiceGenerator.getFactoryName(module)+".create"+JDBCPersistenceServiceGenerator.getServiceName(module)+"()");
	    if (module.getListeners().size()>0){
	    	for (int i=0; i<module.getListeners().size(); i++){
	    		String listClassName = (String)module.getListeners().get(i);
	    		appendStatement("addServiceListener(new "+listClassName+"())");
	    	}
	    }
	    append(closeBlock());
	    append(emptyline());
	    
	    appendString("static final "+getImplementationName(module)+" getInstance(){");
	    increaseIdent();
	    appendString("if (instance==null){");
	    increaseIdent();
	    appendStatement("instance = new "+getImplementationName(module)+"()");
	    append(closeBlock());
	    appendStatement("return instance");
	    append(closeBlock());
	    append(emptyline());
	    

	    String throwsClause = " throws "+getExceptionName(module)+" ";
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";

	        clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
	        clazz.addImport(DataFacadeGenerator.getXMLHelperImport(context, doc));

	        appendString("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause+"{");
	        increaseIdent();
	        append(openTry());
	        appendStatement("return pService.get"+doc.getMultiple()+"()");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        append(closeBlock());
	        append(emptyline());
	        
			appendString("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType)"+throwsClause+"{");
			increaseIdent();
			appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"(), sortType)");
			append(closeBlock());
			append(emptyline());

	        appendString("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("delete"+doc.getName()+"("+doc.getVariableName()+".getId())");
	        append(closeBlock());
	        append(emptyline());
	        
	        appendString("public void delete"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        append(openTry());
	        appendStatement("pService.delete"+doc.getName()+"(id)");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
//	        TODO: fix service delete listening
//	        appendString("if (hasServiceListeners()){"));
//	        increaseIdent();
//	        appendStatement("List<IServiceListener> myListeners = getServiceListeners()"));
//	        appendString("for (int i=0; i<myListeners.size(); i++)"));
//	        appendIncreasedStatement("myListeners.get(i).documentDeleted(oldVersion, "+doc.getVariableName()+")"));
//	        append(closeBlock());
	        append(closeBlock());
	        append(emptyline());

	        appendComment("Deletes multiple "+doc.getName()+" objects.");
	        appendString("public void delete"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        append(openTry());
	        appendStatement("pService.delete"+doc.getMultiple()+"(list)");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendStatement("List<IServiceListener> myListeners = getServiceListeners()");
	        appendString("for (int i=0; i<myListeners.size(); i++)");
	        increaseIdent();
	        appendString("for (int t = 0; t<list.size(); t++){");
	        appendIncreasedStatement("myListeners.get(i).documentDeleted(list.get(t))");
	        appendString("}");
	        decreaseIdent();
	        append(closeBlock());	
	        append(closeBlock());
	        append(emptyline());
	        
	        appendString("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        append(openTry());
	        appendStatement("return pService.get"+doc.getName()+"(id)");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        append(closeBlock());
	        append(emptyline());
	        
	        
	        appendString("public "+doc.getName()+" import"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        append(openTry());
	        appendStatement(doc.getVariableName() + " = pService.import"+doc.getName()+"("+doc.getVariableName()+")");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        appendStatement("return "+doc.getVariableName());
	        append(closeBlock());
	        append(emptyline());

	        
	        appendString("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        append(openTry());
	        appendStatement(doc.getVariableName() + " = pService.create"+doc.getName()+"("+doc.getVariableName()+")");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendStatement("List<IServiceListener> myListeners = getServiceListeners()");
	        appendString("for (int i=0; i<myListeners.size(); i++)");
	        appendIncreasedStatement("myListeners.get(i).documentCreated("+doc.getVariableName()+")");
	        append(closeBlock());	
	        appendStatement("return "+doc.getVariableName());
	        append(closeBlock());
	        append(emptyline());
	        
	        //
	        appendComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.");
	        appendString("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = null");
	        append(openTry());
	        appendStatement("ret = pService.create"+doc.getMultiple()+"(list)");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendStatement("List<IServiceListener> myListeners = getServiceListeners()");
	        appendString("for (int i=0; i<myListeners.size(); i++)");
	        increaseIdent();
	        appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : ret)");
	        appendIncreasedStatement("myListeners.get(i).documentCreated("+doc.getVariableName()+")");
	        decreaseIdent();
	        append(closeBlock());	
	        appendStatement("return ret");
	        append(closeBlock());
	        append(emptyline());

	        appendComment("Updates multiple new "+doc.getName()+" objects.\nReturns the updated versions.");
	        appendString("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = null");
	        append(openTry());
	        appendStatement("ret = pService.update"+doc.getMultiple()+"(list)");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendStatement("List<IServiceListener> myListeners = getServiceListeners()");
	        appendString("for (int i=0; i<myListeners.size(); i++)");
	        increaseIdent();
	        appendString("for (int t = 0; t<ret.size(); t++){");
	        appendIncreasedStatement("myListeners.get(i).documentUpdated(list.get(t), ret.get(t))");
	        appendString("}");
	        decreaseIdent();
	        append(closeBlock());	
	        appendStatement("return ret");
	        append(closeBlock());
	        append(emptyline());

	        
	        appendString("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(doc.getName()+" oldVersion = null");
	        append(openTry());
	        appendString("if (hasServiceListeners())");
	        appendIncreasedStatement("oldVersion = pService.get"+doc.getName()+"("+doc.getVariableName()+".getId())");
	        
	        appendStatement(doc.getVariableName()+" = pService.update"+doc.getName()+"("+doc.getVariableName()+")");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        
	        
	        appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendStatement("List<IServiceListener> myListeners = getServiceListeners()");
	        appendString("for (int i=0; i<myListeners.size(); i++)");
	        appendIncreasedStatement("myListeners.get(i).documentUpdated(oldVersion, "+doc.getVariableName()+")");
	        append(closeBlock());
	        
	        appendStatement("return "+doc.getVariableName());
	        append(closeBlock());
	        append(emptyline());
	        
	        appendString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)"+throwsClause+"{");
	        increaseIdent();
			appendStatement("QueryProperty p = new QueryProperty(propertyName, value)");
			appendString("try{");
			appendIncreasedStatement("return pService.get"+doc.getMultiple()+"ByProperty(p)");
			appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
			appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
			appendString("}");
			
/*
	        appendStatement(listDecl+" all"+doc.getMultiple()+" = get"+doc.getMultiple()+"()"));
	        appendStatement(listDecl+" ret = new ArrayList<"+doc.getName()+">()"));
	        appendString("for (int i=0; i<all"+doc.getMultiple()+".size(); i++){"));
	        increaseIdent();
	        appendStatement(doc.getName()+" "+doc.getVariableName()+" = all"+doc.getMultiple()+".get(i)"));
	        appendString("try{"));
	        increaseIdent();
	        appendStatement("Property property = (("+DocumentGenerator.getDocumentName(doc)+")"+doc.getVariableName()+").getProperty(propertyName)"));
	        appendStatement("if (property.getValue()==null && value==null){"));
	        appendIncreasedStatement("add("+doc.getVariableName()+")"));
	        appendString("}else{"));
	        increaseIdent();
	        appendString("if (value!=null && property.getValue().equals(value))"));
	        appendIncreasedStatement("add("+doc.getVariableName()+")"));
	        append(closeBlock());
	        decreaseIdent();
			appendString("}catch(NoSuchPropertyException nspe){"));
			increaseIdent();
			appendString("if (value==null)"));
			appendIncreasedStatement("add("+doc.getVariableName()+")"));
			decreaseIdent();
	        appendString("}catch(Exception ignored){}"));
	        
	        append(closeBlock());
	        appendString("return ret;"));
	*/      append(closeBlock());
	        append(emptyline());
	        
			appendString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)"+throwsClause+"{");
			increaseIdent();
			appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(propertyName, value), sortType)");
			append(closeBlock());
			
			appendComment("Executes a query on "+doc.getMultiple());
			appendString("public QueryResult executeQueryOn"+doc.getMultiple()+"(DocumentQuery query)"+throwsClause+"{");
			increaseIdent();
			appendStatement(listDecl+" all"+doc.getMultiple()+" = get"+doc.getMultiple()+"()");
			appendStatement("QueryResult result = new QueryResult()");
			appendString("for (int i=0; i<all"+doc.getMultiple()+".size(); i++){");
			increaseIdent();
			appendStatement("List<QueryResultEntry> partialResult = query.match(all"+doc.getMultiple()+".get(i))");
			appendStatement("result.add(partialResult)");
			append(closeBlock());
			
			appendStatement("return result");
			append(closeBlock());
			append(emptyline());

			appendComment("Returns all "+doc.getName()+" objects, where property matches.");
	        appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(QueryProperty... property)"+throwsClause+"{");
	        increaseIdent();
			appendString("try{");
			appendIncreasedStatement("return pService.get"+doc.getMultiple()+"ByProperty(property)");
			appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
			appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
			appendString("}");
	        append(closeBlock());
	        append(emptyline());
	        
			appendComment("Returns all "+doc.getName()+" objects, where property matches, sorted");
			appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(property), sortType)");
	        append(closeBlock());
			append(emptyline());
			
	    }
	    
	    //generate export function
	    append(emptyline());
	    for (MetaDocument d : docs){
	    	appendString("public XMLNode export"+d.getMultiple()+"ToXML(){");
	    	increaseIdent();
	    	appendStatement("XMLNode ret = new XMLNode("+quote(d.getMultiple())+")");
	    	
	    	appendString("try{");
	    	increaseIdent();
	    	appendStatement("List<"+d.getName()+"> list = get"+d.getMultiple()+"()");
	    	appendStatement("ret.addAttribute(new XMLAttribute("+quote("count")+", list.size()))");
	    	appendString("for ("+d.getName()+" object : list)");
	    	appendIncreasedStatement("ret.addChildNode("+DataFacadeGenerator.getXMLHelperName(d)+".toXML(object))");
	    	appendStatement("return ret");
	    	append(closeBlock());
	    	appendStatement("catch("+getExceptionName(module)+" e){");
	    	increaseIdent();
	    	appendStatement("throw new RuntimeException("+quote("export"+d.getMultiple()+"ToXML() failure: ")+" + e.getStackTrace())");
	    	append(closeBlock());
	    	append(closeBlock());
	    	append(emptyline());
	    	
	    	appendString("public XMLNode export"+d.getMultiple()+"ToXML(String languages[]){");
	    	increaseIdent();
	    	appendStatement("XMLNode ret = new XMLNode("+quote(d.getMultiple())+")");
	    	
	    	appendString("try{");
	    	increaseIdent();
	    	appendStatement("List<"+d.getName()+"> list = get"+d.getMultiple()+"()");
	    	appendStatement("ret.addAttribute(new XMLAttribute("+quote("count")+", list.size()))");
	    	appendString("for ("+d.getName()+" object : list)");
	    	appendIncreasedStatement("ret.addChildNode("+DataFacadeGenerator.getXMLHelperName(d)+".toXML(object, languages))");
	    	appendStatement("return ret");
	    	append(closeBlock());
	    	appendStatement("catch("+getExceptionName(module)+" e){");
	    	increaseIdent();
	    	appendStatement("throw new RuntimeException("+quote("export"+d.getMultiple()+"ToXML() failure: ")+" + e.getStackTrace())");
	    	append(closeBlock());
	    	append(closeBlock());
	    	append(emptyline());
	    }
	    

	    appendString("public XMLNode exportToXML(){");
	    increaseIdent();
	    appendStatement("XMLNode ret = new XMLNode("+quote(module.getName())+")");
	    append(emptyline());
	    for (MetaDocument d : docs){
	    	appendStatement("ret.addChildNode(export"+d.getMultiple()+"ToXML())");
	    }
	    append(emptyline());
	    appendStatement("return ret");
	    append(closeBlock());
	    append(emptyline());
	    
	    appendString("public XMLNode exportToXML(String[] languages){");
	    increaseIdent();
	    appendStatement("XMLNode ret = new XMLNode("+quote(module.getName())+")");
	    append(emptyline());
	    for (MetaDocument d : docs){
	    	appendStatement("ret.addChildNode(export"+d.getMultiple()+"ToXML(languages))");
	    }
	    append(emptyline());
	    appendStatement("return ret");
	    append(closeBlock());

	    
	    return clazz;
	}
		
}
