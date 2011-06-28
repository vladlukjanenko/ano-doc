package net.anotheria.asg.generator.model.db;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.AbstractServiceGenerator;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.asg.generator.model.ServiceGenerator;

/**
 * Generates a DB-Backed implementation of a module interface and the according factory.
 * @author another
 *
 */
public class JDBCBasedServiceGenerator extends AbstractServiceGenerator implements IGenerator{
	
	public List<FileEntry> generate(IGenerateable gmodule){
		
		MetaModule mod = (MetaModule)gmodule;
		
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
		appendGenerationPoint("generateImplementation");
		Context context = GeneratorDataRegistry.getInstance().getContext();
		
	    clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The implementation of the "+getInterfaceName(module)+"."));
	    clazz.setPackageName(getPackageName(module));
	    	
	    clazz.addImport("java.util.List");
	    clazz.addImport("net.anotheria.util.sorter.SortType");
	    clazz.addImport("net.anotheria.util.sorter.StaticQuickSorter");
	    clazz.addImport(context.getServicePackageName(MetaModule.SHARED)+".BasicService");
	    //ret.emptyline();
	    clazz.addImport(JDBCPersistenceServiceGenerator.getInterfaceImport(context, module));
	    clazz.addImport(JDBCPersistenceServiceGenerator.getFactoryImport(context, module));
	    clazz.addImport(JDBCPersistenceServiceGenerator.getExceptionImport(context, module));

	    clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResultEntry");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
	    
	    clazz.addImport("net.anotheria.util.xml.XMLNode");
	    clazz.addImport("net.anotheria.util.xml.XMLAttribute");
	    clazz.addImport("net.anotheria.util.slicer.Segment");

	    clazz.setName(getImplementationName(module));
	    clazz.setParent("BasicService");
	    clazz.addInterface(getInterfaceName(module));
	    
	    startClassBody();
	    appendStatement("private static "+getImplementationName(module)+" instance");
	    emptyline();
	    
	    appendStatement("private "+JDBCPersistenceServiceGenerator.getInterfaceName(module)+" pService");
	    
	    appendString("private "+getImplementationName(module)+"(){");
	    increaseIdent();
	    appendStatement("pService = "+JDBCPersistenceServiceGenerator.getFactoryName(module)+".create"+JDBCPersistenceServiceGenerator.getServiceName(module)+"()");
	    if (module.getListeners().size()>0){
	    	for (int i=0; i<module.getListeners().size(); i++){
	    		String listClassName = module.getListeners().get(i);
	    		appendStatement("addServiceListener(new "+listClassName+"())");
	    	}
	    }
	    closeBlockNEW();
	    emptyline();
	    
	    appendString("static final "+getImplementationName(module)+" getInstance(){");
	    increaseIdent();
	    appendString("if (instance==null){");
	    increaseIdent();
	    appendStatement("instance = new "+getImplementationName(module)+"()");
	    closeBlockNEW();
	    appendStatement("return instance");
	    closeBlockNEW();
	    emptyline();
	    

	    String throwsClause = " throws "+getExceptionName(module)+" ";
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";

	        clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
	        clazz.addImport(DataFacadeGenerator.getXMLHelperImport(context, doc));

	        appendString("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause+"{");
	        increaseIdent();
	        openTry();
	        appendStatement("return pService.get"+doc.getMultiple()+"()");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        closeBlockNEW();
	        emptyline();
	        
			appendString("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType)"+throwsClause+"{");
			increaseIdent();
			appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"(), sortType)");
			closeBlockNEW();
			emptyline();

			//appendString("public "+listDecl+" get"+doc.getMultiple()+"(List<String> ids)"+throwsClause+"{");
			//increaseIdent();
			//appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"(), sortType)");
			//closeBlockNEW();
			//emptyline();

			//appendString("public "+listDecl+" get"+doc.getMultiple()+"(List<String> ids, SortType sortType)"+throwsClause+"{");
			//increaseIdent();
			//appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"(ids), sortType)");
			//closeBlockNEW();
			//emptyline();

			appendString("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("delete"+doc.getName()+"("+doc.getVariableName()+".getId())");
            appendString("if(hasServiceListeners()){");
            increaseIdent();
            appendStatement("fireObjectDeletedEvent("+doc.getVariableName()+")");
	        closeBlockNEW();
	        closeBlockNEW();
	        emptyline();


	        appendString("public void delete"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        openTry();
            appendStatement(doc.getName()+" varValue = hasServiceListeners()?pService.get"+doc.getName()+"(id):null");
	        appendStatement("pService.delete"+doc.getName()+"(id)");
            appendString("if(varValue!=null){");
            increaseIdent();
            appendStatement("fireObjectDeletedEvent(varValue)");
	        closeBlockNEW();
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        closeBlockNEW();
	        emptyline();

	        appendComment("Deletes multiple "+doc.getName()+" objects.");
	        appendString("public void delete"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        openTry();
	        appendStatement("pService.delete"+doc.getMultiple()+"(list)");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendString("for (int t = 0; t<list.size(); t++)");
	        appendIncreasedStatement("fireObjectDeletedEvent(list.get(t))");
	        closeBlockNEW();	
	        closeBlockNEW();	
	        emptyline();
	        
	        appendString("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        openTry();
	        appendStatement("return pService.get"+doc.getName()+"(id)");
	        decreaseIdent();
	        clazz.addImport(JDBCPersistenceServiceGenerator.getItemNotFoundExceptionImport(context, doc, module));
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getItemNotFoundExceptionName(doc, module)+" e){");
	        appendIncreasedStatement("throw new "+ServiceGenerator.getItemNotFoundExceptionName(doc, module)+"(id)");
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        closeBlockNEW();
	        emptyline();
	        
	        
	        appendString("public "+doc.getName()+" import"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        openTry();
	        appendStatement(doc.getVariableName() + " = pService.import"+doc.getName()+"("+doc.getVariableName()+")");
            appendString("if(hasServiceListeners()){");
            increaseIdent();
            appendStatement("fireObjectImportedEvent("+doc.getVariableName()+")");
            closeBlockNEW();
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        appendStatement("return "+doc.getVariableName());
	        closeBlockNEW();
	        emptyline();

            appendString("public "+listDecl+" import"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = null");
	        openTry();
	        appendStatement("ret = pService.import"+doc.getMultiple()+"(list)");
            appendString("if(hasServiceListeners()){");
            increaseIdent();
            appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : ret)");
            increaseIdent();
            appendStatement("fireObjectImportedEvent("+doc.getVariableName()+")");
            decreaseIdent();
            closeBlockNEW();
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        appendStatement("return ret");
	        closeBlockNEW();
	        emptyline();

	        
	        appendString("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        openTry();
	        appendStatement(doc.getVariableName() + " = pService.create"+doc.getName()+"("+doc.getVariableName()+")");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        appendStatement("fireObjectCreatedEvent("+doc.getVariableName()+")");
	        appendStatement("return "+doc.getVariableName());
	        closeBlockNEW();
	        emptyline();
	        
	        //
	        appendComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.");
	        appendString("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = null");
	        openTry();
	        appendStatement("ret = pService.create"+doc.getMultiple()+"(list)");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : ret)");
	        appendIncreasedStatement("fireObjectCreatedEvent("+doc.getVariableName()+")");
	        closeBlockNEW();	
	        appendStatement("return ret");
	        closeBlockNEW();
	        emptyline();

	        appendComment("Updates multiple "+doc.getName()+" objects.\nReturns the updated versions.");
	        appendString("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = null");
	        openTry();
	        appendStatement("ret = pService.update"+doc.getMultiple()+"(list)");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendString("for (int t = 0; t<ret.size(); t++)");
	        appendIncreasedStatement("fireObjectUpdatedEvent(list.get(t), ret.get(t))");
	        closeBlockNEW();	
	        appendStatement("return ret");
	        closeBlockNEW();
	        emptyline();

	        
	        appendString("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(doc.getName()+" oldVersion = null");
	        openTry();
	        appendString("if (hasServiceListeners())");
	        appendIncreasedStatement("oldVersion = pService.get"+doc.getName()+"("+doc.getVariableName()+".getId())");
	        
	        appendStatement(doc.getVariableName()+" = pService.update"+doc.getName()+"("+doc.getVariableName()+")");
	        decreaseIdent();
	        appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
	        appendString("}");
	        
	        
	        appendString("if (oldVersion!=null)");
	        appendIncreasedStatement("fireObjectUpdatedEvent(oldVersion, "+doc.getVariableName()+")");
	        
	        appendStatement("return "+doc.getVariableName());
	        closeBlockNEW();
	        emptyline();
	        
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
	        closeBlockNEW();
	        decreaseIdent();
			appendString("}catch(NoSuchPropertyException nspe){"));
			increaseIdent();
			appendString("if (value==null)"));
			appendIncreasedStatement("add("+doc.getVariableName()+")"));
			decreaseIdent();
	        appendString("}catch(Exception ignored){}"));
	        
	        closeBlockNEW();
	        appendString("return ret;"));
	*/      closeBlockNEW();
	        emptyline();
	        
			appendString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)"+throwsClause+"{");
			increaseIdent();
			appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(propertyName, value), sortType)");
			closeBlockNEW();
			
			appendComment("Executes a query on "+doc.getMultiple());
			appendString("public QueryResult executeQueryOn"+doc.getMultiple()+"(DocumentQuery query)"+throwsClause+"{");
			increaseIdent();
			appendStatement(listDecl+" all"+doc.getMultiple()+" = get"+doc.getMultiple()+"()");
			appendStatement("QueryResult result = new QueryResult()");
			appendString("for (int i=0; i<all"+doc.getMultiple()+".size(); i++){");
			increaseIdent();
			appendStatement("List<QueryResultEntry> partialResult = query.match(all"+doc.getMultiple()+".get(i))");
			appendStatement("result.add(partialResult)");
			closeBlockNEW();
			
			appendStatement("return result");
			closeBlockNEW();
			emptyline();

			appendComment("Returns all "+doc.getName()+" objects, where property matches.");
	        appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(QueryProperty... property)"+throwsClause+"{");
	        increaseIdent();
			appendString("try{");
			appendIncreasedStatement("return pService.get"+doc.getMultiple()+"ByProperty(property)");
			appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
			appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())");
			appendString("}");
	        closeBlockNEW();
	        emptyline();
	        
			appendComment("Returns all "+doc.getName()+" objects, where property matches, sorted");
			appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(property), sortType)");
	        closeBlockNEW();
			emptyline();
			
			// get elements COUNT
			appendComment("Returns " + doc.getName() + " objects count.");
			appendString("public int get" + doc.getMultiple() + "Count()" + throwsClause + "{");
			increaseIdent();
			openTry();
			appendStatement("return pService.get" + doc.getMultiple() + "Count()");
			decreaseIdent();
			appendString("} catch (" + JDBCPersistenceServiceGenerator.getExceptionName(module) + " e) {");
			appendIncreasedStatement("throw new " + getExceptionName(module) + "(\"Persistence failed: \"+e.getMessage())");
			appendString("}");
			closeBlockNEW();
			emptyline();
			// end get elements COUNT

			// get elements Segment
			appendComment("Returns " + doc.getName() + " objects segment.");
			appendString("public " + listDecl + " get" + doc.getMultiple() + "(Segment aSegment)" + throwsClause + "{");
			increaseIdent();
			openTry();
			appendStatement("return pService.get" + doc.getMultiple() + "(aSegment)");
			decreaseIdent();
			appendString("} catch (" + JDBCPersistenceServiceGenerator.getExceptionName(module) + " e) {");
			appendIncreasedStatement("throw new " + getExceptionName(module) + "(\"Persistence failed: \"+e.getMessage())");
			appendString("}");
			closeBlockNEW();
			emptyline();
			// end get elements Segment

			// get elements Segment with FILTER
			appendComment("Returns " + doc.getName() + " objects segment, where property matches.");
			appendStatement("public " + listDecl + " get" + doc.getMultiple() + "ByProperty(Segment aSegment, QueryProperty... aProperty)"
					+ throwsClause + "{");
			increaseIdent();
			appendString("try {");
			appendIncreasedStatement("return pService.get" + doc.getMultiple() + "ByProperty(aSegment, aProperty)");
			appendString("} catch (" + JDBCPersistenceServiceGenerator.getExceptionName(module) + " e) {");
			appendIncreasedStatement("throw new " + getExceptionName(module) + "(\"Persistence failed: \"+e.getMessage())");
			appendString("}");
			closeBlockNEW();
			emptyline();
			// end get elements Segment with FILTER

			// get elements Segment with SORTING, FILTER
			appendComment("Returns " + doc.getName() + " objects segment, where property matches, sorted");
			appendStatement("public " + listDecl + " get" + doc.getMultiple()
					+ "ByProperty(Segment aSegment, SortType aSortType, QueryProperty... aProperty)" + throwsClause + "{");
			increaseIdent();
			appendStatement("return StaticQuickSorter.sort(get" + doc.getMultiple() + "ByProperty(aSegment, aProperty), aSortType)");
			closeBlockNEW();
			emptyline();
			// end get elements Segment with SORTING, FILTER
	    }
	    
	    //generate export function
	    emptyline();
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
	    	closeBlockNEW();
	    	appendStatement("catch("+getExceptionName(module)+" e){");
	    	increaseIdent();
	    	appendStatement("throw new RuntimeException("+quote("export"+d.getMultiple()+"ToXML() failure: ")+" + e.getStackTrace())");
	    	closeBlockNEW();
	    	closeBlockNEW();
	    	emptyline();

			appendString("public XMLNode export"+d.getMultiple()+"ToXML(List<"+d.getName()+"> list){");
	    	increaseIdent();
	    	appendStatement("XMLNode ret = new XMLNode("+quote(d.getMultiple())+")");
	    	appendStatement("ret.addAttribute(new XMLAttribute("+quote("count")+", list.size()))");
	    	appendString("for ("+d.getName()+" object : list)");
	    	appendIncreasedStatement("ret.addChildNode("+DataFacadeGenerator.getXMLHelperName(d)+".toXML(object))");
	    	appendStatement("return ret");
	    	closeBlockNEW();
	    	emptyline();
	    	
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
	    	closeBlockNEW();
	    	appendStatement("catch("+getExceptionName(module)+" e){");
	    	increaseIdent();
	    	appendStatement("throw new RuntimeException("+quote("export"+d.getMultiple()+"ToXML() failure: ")+" + e.getStackTrace())");
	    	closeBlockNEW();
	    	closeBlockNEW();
	    	emptyline();

			appendString("public XMLNode export"+d.getMultiple()+"ToXML(String languages[], List<"+d.getName()+"> list){");
	    	increaseIdent();
	    	appendStatement("XMLNode ret = new XMLNode("+quote(d.getMultiple())+")");
	    	appendStatement("ret.addAttribute(new XMLAttribute("+quote("count")+", list.size()))");
	    	appendString("for ("+d.getName()+" object : list)");
	    	appendIncreasedStatement("ret.addChildNode("+DataFacadeGenerator.getXMLHelperName(d)+".toXML(object, languages))");
	    	appendStatement("return ret");
	    	closeBlockNEW();
	    	emptyline();
	    }
	    
	    appendComment("Executes a query on all data objects (documents, vo) which are part of this module and managed by this service");
		appendString("public QueryResult executeQueryOnAllObjects(DocumentQuery query)" + throwsClause + "{");
		increaseIdent();
		appendStatement("QueryResult ret = new QueryResult()");
		for (MetaDocument doc : docs){
			appendStatement("ret.add(executeQueryOn"+doc.getMultiple()+"(query).getEntries())");
		}
		appendStatement("return ret");
		closeBlock("executeQueryOnAllObjects");
		emptyline();

	    appendString("public XMLNode exportToXML(){");
	    increaseIdent();
	    appendStatement("XMLNode ret = new XMLNode("+quote(module.getName())+")");
	    emptyline();
	    for (MetaDocument d : docs){
	    	appendStatement("ret.addChildNode(export"+d.getMultiple()+"ToXML())");
	    }
	    emptyline();
	    appendStatement("return ret");
	    closeBlockNEW();
	    emptyline();
	    
	    appendString("public XMLNode exportToXML(String[] languages){");
	    increaseIdent();
	    appendStatement("XMLNode ret = new XMLNode("+quote(module.getName())+")");
	    emptyline();
	    for (MetaDocument d : docs){
	    	appendStatement("ret.addChildNode(export"+d.getMultiple()+"ToXML(languages))");
	    }
	    emptyline();
	    appendStatement("return ret");
	    closeBlockNEW();

	    
	    return clazz;
	}
	
	public List<String> getAll() {
		return getAllByLimit(Integer.MIN_VALUE);
	}
	
	public List<String> getAllByLimit(int aLimit) {
		return getAllByLimitAndOffset(aLimit, Integer.MIN_VALUE);
	}
	
	public List<String> getAllByLimitAndOffset(int aLimit, long aOffcet) {
		List<String> result = new ArrayList<String>();
		String sqlQuery = "SELECT * FROM table";
		String sqlQueryLimit = "";
		String sqlQueryOffcet = "";
		
		if (aLimit != Integer.MIN_VALUE) {
			sqlQueryLimit = " LIMIT " + aLimit;
		}
		
		if (aOffcet == Integer.MIN_VALUE) {
			sqlQueryOffcet = " OFFSET " + aLimit;
		}
		
		sqlQuery = sqlQuery + sqlQueryLimit + sqlQueryOffcet;

		// some db logic 
		
		return result;
	}
	
	@Override protected String getMoskitoSubsystem(){
		return super.getMoskitoSubsystem()+"-jdbc";
	}
		
}
