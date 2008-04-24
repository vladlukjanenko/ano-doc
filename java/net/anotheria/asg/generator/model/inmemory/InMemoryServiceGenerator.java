package net.anotheria.asg.generator.model.inmemory;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.AbstractServiceGenerator;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.util.ExecutionTimer;

/**
 * Generates a DB-Backed implementation of a module interface and the according factory.
 * @author another
 *
 */
public class InMemoryServiceGenerator extends AbstractServiceGenerator implements IGenerator{
	
	private Context context;
	
	public List<FileEntry> generate(IGenerateable gmodule, Context context){
		
		MetaModule mod = (MetaModule)gmodule;
		
		this.context = context;
		String packageName = getPackageName(context, mod);
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ExecutionTimer timer = new ExecutionTimer("InMemory Generator");
		timer.startExecution(mod.getName()+"Factory");
		ret.add(new FileEntry(FileEntry.package2path(packageName), getFactoryName(mod), generateFactory(mod)));
		timer.stopExecution(mod.getName()+"Factory");
		
		timer.startExecution(mod.getName()+"Impl");
		ret.add(new FileEntry(FileEntry.package2path(packageName), getImplementationName(mod), generateImplementation(mod)));
		timer.stopExecution(mod.getName()+"Impl");
		
		//timer.printExecutionTimesOrderedByCreation();
		
		return ret;
	}

	public String getImplementationName(MetaModule m){
	    return "InMemory"+getServiceName(m)+"Impl";
	}
	
	public String getFactoryName(MetaModule m){
	    return "InMemory"+getServiceName(m)+"Factory";
	}

	
	protected String getPackageName(MetaModule module){
		return getPackageName(context, module);
	}
	
	protected String writeAdditionalFactoryImports(MetaModule module){
		String ret = writeImport(context.getServicePackageName(module)+"."+getInterfaceName(module));
		ret += writeImport("net.anotheria.asg.service.InMemoryService");
		return ret;
	}
	
	private String getCacheName(MetaDocument doc){
		return "cache"+doc.getName();
	}

	private String getCachedListName(MetaDocument doc){
		return "_cached"+doc.getName()+"List";
	}

	private String getWrapperDecl(MetaDocument doc){
		return "InMemoryObjectWrapper<"+doc.getName()+">";
	}

	private String getLastIdName(MetaDocument doc){
		return "last"+doc.getName()+"Id";
	}

	/**
	 * Generates the implementation
	 * @param module the metamodule to generate
	 * @return
	 */
	private String generateImplementation(MetaModule module){

		startNewJob();
	    ExecutionTimer timer = new ExecutionTimer("InMemory-generateImplementation");
	    timer.startExecution("pre");
		append(CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The in memory implementation of the "+getInterfaceName(module)+"."));

	    appendStatement("package "+getPackageName(module));
	    appendEmptyline();
	    appendImport("java.util.List");
	    appendImport("java.util.Map");
	    appendImport("java.util.ArrayList");
	    appendImport("java.util.HashMap");
	    appendImport("java.util.concurrent.atomic.AtomicLong");


	    appendImport("net.anotheria.util.sorter.SortType");
	    appendImport("net.anotheria.util.sorter.StaticQuickSorter");
	    appendImport(context.getServicePackageName(MetaModule.SHARED)+".BasicService");
	    appendEmptyline();

	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        appendImport( DataFacadeGenerator.getDocumentImport(context, doc));
	        appendImport( DataFacadeGenerator.getDocumentFactoryImport(context, doc));
	        appendImport(DataFacadeGenerator.getXMLHelperImport(context, doc));
	    }
	    appendEmptyline();
	    appendImport( "net.anotheria.asg.util.listener.IServiceListener");
	    appendImport( "net.anotheria.anodoc.query2.DocumentQuery");
	    appendImport( "net.anotheria.anodoc.query2.QueryResult");
	    appendImport( "net.anotheria.anodoc.query2.QueryResultEntry");
	    appendImport( "net.anotheria.anodoc.query2.QueryProperty");
	    
	    appendEmptyline();
	    appendImport( "net.anotheria.util.xml.XMLNode");
	    appendImport("net.anotheria.util.xml.XMLAttribute");
	    appendEmptyline();
	    appendImport( "net.anotheria.asg.exception.ASGRuntimeException");
	    appendImport( "net.anotheria.asg.service.InMemoryService");
	    appendImport( "net.anotheria.asg.service.InMemoryObjectWrapper");
	    appendImport( ServiceGenerator.getInterfaceImport(context, module));
	    appendImport( ServiceGenerator.getExceptionImport(context, module));
	    appendEmptyline();
	    
	    
	    appendString("public class "+getImplementationName(module)+" extends BasicService implements "+getInterfaceName(module)+", InMemoryService<"+getInterfaceName(module)+"> {");
	    increaseIdent();
	    appendStatement("private static "+getImplementationName(module)+" instance");
	    append(emptyline());
	    
	    appendStatement("private boolean paired");
	    appendStatement(getInterfaceName(module)," pairedInstance = null");
	    appendEmptyline();

	    //generate storage
	    for (MetaDocument doc : docs){
	        appendStatement("private Map<String, InMemoryObjectWrapper<"+doc.getName()+">> "+getCacheName(doc));
		    appendStatement("private AtomicLong "+getLastIdName(doc)+" = new AtomicLong()");
	        appendStatement("private List<"+doc.getName()+"> "+getCachedListName(doc));
	        appendStatement("private Object "+doc.getName()+"Lock = new Object()");

	    }
	    appendEmptyline();
	    
	    
	    appendString("private "+getImplementationName(module)+"(){");
	    increaseIdent();
	    appendStatement("reset()");
	    append(closeBlock());
	    appendEmptyline();
	    
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
	    boolean containsAnyMultilingualDocs = false;
	    
	    timer.stopExecution("pre");
	    timer.startExecution("main");
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        timer.startExecution(doc.getName()+"Document");
	        String listDecl = "List<"+doc.getName()+">";
	        String wrapperDecl = getWrapperDecl(doc);
	        
	        appendString("private "+listDecl+" _getCached"+doc.getMultiple()+"()"+throwsClause+"{");
	        increaseIdent();
	        appendString("if ("+getCachedListName(doc)+" != null )");
	        appendIncreasedStatement("return "+getCachedListName(doc));
	        appendString("synchronized("+doc.getName()+"Lock){");
	        increaseIdent();
	        appendString("if ("+getCachedListName(doc)+" != null )");
	        appendIncreasedStatement("return "+getCachedListName(doc));

	        appendStatement(listDecl+"tmp = new ArrayList<"+doc.getName()+">("+getCacheName(doc)+".size())");
	        appendString("for ("+wrapperDecl+" wrapper  : "+getCacheName(doc)+".values()){");
	        increaseIdent();
	        appendString("if (wrapper.get()!=null)");
	        appendIncreasedStatement("tmp.add(wrapper.get())");
	        append(closeBlock());
	        appendStatement( getCachedListName(doc)+" = tmp");
	        appendStatement("return "+getCachedListName(doc));
	        append(closeBlock());
	        append(closeBlock());
	        appendEmptyline();
	        
	        
	        appendString("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("return _getCached"+doc.getMultiple()+"()");
	        append(closeBlock());
	        append(emptyline());

	        appendString("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType)"+throwsClause+"{");
			increaseIdent();
			appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"(), sortType)");
			append(closeBlock());
			appendEmptyline();
	        
	        appendString("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("delete"+doc.getName()+"("+doc.getVariableName()+".getId())");
	        append(closeBlock());
	        append(emptyline());
	        
	        appendString("public void delete"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        appendString("if (paired){");
	        increaseIdent();
	        appendStatement(getWrapperDecl(doc)+" w = "+getCacheName(doc)+".get(id)");
	        appendString("if (w!=null)");
	        appendIncreasedStatement("w.delete()");
	        decreaseIdent();
	        appendString("}else{");
	        increaseIdent();
	        appendStatement(getCacheName(doc)+".remove(id)");
	        append(closeBlock());
	        append(closeBlock());
	        append(emptyline());
	        
//*/	        

	        appendString("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(wrapperDecl+" w = "+getCacheName(doc)+".get(id)");
	        appendString("if (w==null || w.get()==null)");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"No such "+doc.getName()+" with id: \"+id)");
	        appendStatement("return w.get()");
	        append(closeBlock());
	        append(emptyline());
	        
	        appendString("private "+doc.getName()+" createNewObject(String anId, "+doc.getName()+" template){");
	        increaseIdent();
	        appendString("if (template instanceof net.anotheria.asg.data.AbstractVO){");
	        increaseIdent();
	        appendStatement("throw new RuntimeException(",quote("Not yet implemented"), ")");
	        //appendStatement(ret, doc.getName(), " ret = ",DataFacadeGenerator.getDocumentFactoryName(doc), ".create", doc.getName(), "(anId)");
	    	//appendStatement(ret, "((net.anotheria.asg.data.AbstractVO)ret).copyAttributesFrom(template)" );
	    	//appendStatement(ret, "return ret");
	        
	        append(closeBlock());
	        append(emptyline());

	        appendString("if (template instanceof net.anotheria.anodoc.data.Document){");
	        increaseIdent();
	        appendStatement("throw new RuntimeException(\"Document creation not yet supported\")");
	        append(closeBlock());
	        
	        appendStatement("throw new RuntimeException(\"Unknown document type: \"+template.getClass())");

	        append(closeBlock());
///*	        
	        appendString("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("long nextId = "+getLastIdName(doc)+".incrementAndGet()");
	        appendStatement(doc.getVariableName()+" = createNewObject(\"\"+nextId, "+doc.getVariableName()+")");
	        appendStatement(wrapperDecl+" wrapper = new "+wrapperDecl+"("+doc.getVariableName()+", paired)");
	        appendStatement("// should check whether an object with this id already exists... which however can only happen in case of an error ");
	        appendStatement(getCacheName(doc)+".put(wrapper.getId(), wrapper)");
	        appendStatement(getCachedListName(doc), " = null");
	        appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendStatement("List<IServiceListener> myListeners = getServiceListeners()");
	        appendString("for (int i=0; i<myListeners.size(); i++)");
	        appendIncreasedStatement("myListeners.get(i).documentCreated("+doc.getVariableName()+")");
	        append(closeBlock());
	        appendStatement("return "+doc.getVariableName());
	        append(closeBlock());
	        append(emptyline());
	        
///*	        
	        
	        appendComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.");
	        appendString("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = new ArrayList<"+doc.getName()+">(list.size())");
	        
	        appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : list){");
	        increaseIdent();
	        appendStatement("long nextId = "+getLastIdName(doc)+".incrementAndGet()");
	        appendStatement(doc.getVariableName()+" = createNewObject(\"\"+nextId, "+doc.getVariableName()+")");
	        appendStatement(wrapperDecl+" wrapper = new "+wrapperDecl+"("+doc.getVariableName()+", paired)");
	        appendStatement("// should check whether an object with this id already exists... which however can only happen in case of an error ");
	        appendStatement(getCacheName(doc)+".put(wrapper.getId(), wrapper)");
	        appendStatement("ret.add("+doc.getVariableName()+")");
	        append(closeBlock());
	        
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

	        //*/
	        
	        appendComment("Updates multiple new "+doc.getName()+" objects.\nReturns the updated versions.");
	        appendString("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" old = new ArrayList<"+doc.getName()+">(list.size())");
	        appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : list){");
	        increaseIdent();
	        appendStatement(wrapperDecl+" wrapper = "+getCacheName(doc)+".get("+doc.getVariableName()+".getId())");
	        appendString("if (wrapper==null || wrapper.get()==null)");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"No such "+doc.getName()+" with id: \"+"+doc.getVariableName()+".getId())");
	        appendStatement("old.add(wrapper.get())");
	        appendStatement("wrapper.update("+doc.getVariableName()+")");
	        append(closeBlock());
	        
	        appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendStatement("List<IServiceListener> myListeners = getServiceListeners()");
	        appendString("for (int i=0; i<myListeners.size(); i++)");
	        increaseIdent();
	        appendString("for (int t = 0; t<list.size(); t++){");
	        appendIncreasedStatement("myListeners.get(i).documentUpdated(old.get(i), list.get(i))");
	        appendString("}");
	        decreaseIdent();
	        append(closeBlock());
	        appendStatement("return list");
	        append(closeBlock());
	        appendEmptyline();
//*/
	        
	        appendString("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("String id = "+doc.getVariableName()+".getId()");
	        appendStatement(doc.getName()+" oldVersion = null");
	        appendStatement(wrapperDecl+" w = "+getCacheName(doc)+".get(id)");
	        appendString("if (w==null || w.get()==null)");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"No such "+doc.getName()+" with id: \"+id)");
	        appendString("if (hasServiceListeners())");
	        appendIncreasedStatement("oldVersion = w.get()");
	        appendStatement("w.update("+doc.getVariableName()+")");
	        
	        
	        appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendStatement("List<IServiceListener> myListeners = getServiceListeners()");
	        appendString("for (int i=0; i<myListeners.size(); i++)");
	        appendIncreasedStatement("myListeners.get(i).documentUpdated(oldVersion, "+doc.getVariableName()+")");
	        append(closeBlock());
	        
	        appendStatement("return "+doc.getVariableName());
	        append(closeBlock());
	        append(emptyline());
///*	        
	        appendString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("throw new RuntimeException(\"Not yet implemented\")");
/*			appendStatement("QueryProperty p = new QueryProperty(propertyName, value)");
			appendString("try{");
			appendIncreasedStatement("return pService.get"+doc.getMultiple()+"ByProperty(p)");
			appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
			appendIncreasedStatement("throw new RuntimeException(\"Persistence failed: \"+e.getMessage())");
			appendString("}");
*/			
      		append(closeBlock());
	        append(emptyline());
//*/
	        
			appendString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)"+throwsClause+"{");
			increaseIdent();
			appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(propertyName, value), sortType)");
			append(closeBlock());
	        append(emptyline());

	        // /*			
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
			appendString("//first the slow version, the fast version is a todo.");
			appendStatement(listDecl+" ret = new ArrayList<"+doc.getName()+">()");
			appendStatement(listDecl+" src = get"+doc.getMultiple()+"()");
			appendStatement("for ( "+doc.getName()+" "+doc.getVariableName() +" : src){");
			increaseIdent();
			appendStatement("boolean mayPass = true");
			appendStatement("for (QueryProperty qp : property){");
			increaseIdent();
			appendStatement("mayPass = mayPass && qp.doesMatch("+doc.getVariableName()+".getPropertyValue(qp.getName()))");
			append(closeBlock());
			
			appendString("if (mayPass)");
			appendIncreasedStatement("ret.add("+doc.getVariableName()+")");
			append(closeBlock());
			
			appendStatement("return ret");
			append(closeBlock());
	        append(emptyline());
/*
	        increaseIdent();
			appendString("try{");
			appendIncreasedStatement("return pService.get"+doc.getMultiple()+"ByProperty(property)");
			appendString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
			appendIncreasedStatement("throw new RuntimeException(\"Persistence failed: \"+e.getMessage())");
			appendString("}");
	        ret.append(closeBlock();
	        ret.append(emptyline();
	*/        
			appendComment("Returns all "+doc.getName()+" objects, where property matches, sorted");
			appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(property), sortType)");
	        append(closeBlock());
			append(emptyline());
		//	*/
			
			if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
				appendComment("In all documents of type "+doc.getName()+" copies all multilingual fields from sourceLanguage to targetLanguage");
				appendStatement("public void copyMultilingualAttributesInAll"+doc.getMultiple()+"(String sourceLanguage, String targetLanguage)"+throwsClause+"{");
				increaseIdent();
		        appendStatement("throw new RuntimeException(\"Not yet implemented\")");
				append(closeBlock());
				append(emptyline());
				containsAnyMultilingualDocs = true;
			}
	    
	        timer.stopExecution(doc.getName()+"Document");
	    }

	    timer.stopExecution("main");
	    timer.startExecution("foot");

	    
	    if (containsAnyMultilingualDocs){
			appendComment("Copies all multilingual fields from sourceLanguage to targetLanguage in all data objects (documents, vo) which are part of this module and managed by this service");
			appendString("public void copyMultilingualAttributesInAllObjects(String sourceLanguage, String targetLanguage)"+throwsClause+"{");
			increaseIdent();
	        appendStatement("throw new RuntimeException(\"Not yet implemented\")");
			append(closeBlock());
			append(emptyline());
			
	    }

	  //generate export function
	    append(emptyline());
	    for (MetaDocument d : docs){
	    	appendString("public XMLNode export"+d.getMultiple()+"ToXML(){");
	    	increaseIdent();
	    	appendStatement("XMLNode ret = new XMLNode("+quote(d.getMultiple())+")");
	    	
	    	appendStatement("try{");
	    	increaseIdent();
	    	appendStatement("List<"+d.getName()+"> list = get"+d.getMultiple()+"()");
	    	appendStatement("ret.addAttribute(new XMLAttribute("+quote("count")+", list.size()))");
	    	appendString("for ("+d.getName()+" object : list)");
	    	appendIncreasedStatement("ret.addChildNode("+DataFacadeGenerator.getXMLHelperName(d)+".toXML(object))");
	    	appendStatement("return ret");
	    	append(closeBlock());
	    	appendString("catch("+getExceptionName(module)+" e){");
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
	    
	    
	    
	    //*/

	    appendString("public synchronized void pairTo("+getInterfaceName(module)+" instance) throws ASGRuntimeException{");
	    increaseIdent();
	    
	    appendString("if (paired)");
	    appendIncreasedStatement("throw new ASGRuntimeException(", quote("Already paired"), ")");
	    appendStatement( "paired = true");
	    appendStatement( "pairedInstance = instance");
	    appendStatement( "reset()");
	    
		append(closeBlock());
	    appendEmptyline();
	    
	    appendString("public synchronized void unpair("+getInterfaceName(module)+" instance){");
	    increaseIdent();
	    appendStatement("throw new RuntimeException(\"Not yet implemented\")");
	    append(closeBlock());
	    append(emptyline());

	    appendString("public synchronized void synchBack()throws ASGRuntimeException{");
	    increaseIdent();
	    appendString("if (!paired)");
	    appendIncreasedStatement( "throw new ASGRuntimeException(", quote("Not paired"), ")");
	    
	    appendStatement( "throw new RuntimeException(\"Not yet implemented\")");
		append(closeBlock());
	    appendEmptyline();

	    
	    for (MetaDocument doc : docs){
		    appendString("public void read"+doc.getName()+"From("+getInterfaceName(module)+" instance) throws ASGRuntimeException{");
		    increaseIdent();
		    appendStatement("List<"+doc.getName()+"> list = instance.get"+doc.getMultiple()+"()");
		    appendStatement("long maxId = 0");
		    appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : list){ ");
		    increaseIdent();
		    appendStatement("long id = Long.parseLong("+doc.getVariableName()+".getId())");
		    appendString("if (id>maxId)");
		    appendIncreasedStatement("maxId = id");
		    appendStatement(getCacheName(doc)+".put("+doc.getVariableName()+".getId(), new InMemoryObjectWrapper<"+doc.getName()+">("+doc.getVariableName()+"))");
			append(closeBlock());
		    appendStatement(getLastIdName(doc)+" = new AtomicLong(maxId)");
		    appendStatement(getCachedListName(doc)+" = null");
			append(closeBlock());
		    append(emptyline());
	    }

	    appendString("public void readFrom("+getInterfaceName(module)+" instance) throws ASGRuntimeException {");
	    increaseIdent();
	    for (MetaDocument doc : docs){
	    	appendStatement("read"+doc.getName()+"From(instance)");
	    }
	    append(closeBlock());
	    append(emptyline());
		
	    appendString("public void synchTo("+getInterfaceName(module)+" instance)throws ASGRuntimeException{");
	    increaseIdent();
	    appendStatement("throw new RuntimeException(\"Not yet implemented\")");
	    append(closeBlock());
	    append(emptyline());
	    
	    appendString("public void clear(){");
	    increaseIdent();
	    appendString("if (paired) ");
	    appendIncreasedStatement("throw new RuntimeException(\"Cant reset a paired copy, unpair it first\")");
	    appendStatement("reset()");
	    append(closeBlock());
	    
	    appendString("private void reset(){");
	    increaseIdent();
	    for (MetaDocument doc : docs){
	        appendStatement(getCacheName(doc)+" = new HashMap<String, InMemoryObjectWrapper<"+doc.getName()+">>()");
		    appendStatement(getLastIdName(doc)+" = new AtomicLong(0)");
		    appendStatement(getCachedListName(doc)+" = null");
		    appendEmptyline();
	    }
	    append(closeBlock());
    

		append(closeBlock());
	    timer.stopExecution("foot");
	    //timer.printExecutionTimesOrderedByCreation();
	    
	    return getCurrentJobContent().toString();
	}
	
	
	public static String getPackageName(Context context, MetaModule module){
		return context.getPackageName(module)+".service.inmemory";
	}
	
	protected String getSupportedInterfacesList(MetaModule module){
		return super.getSupportedInterfacesList(module)+", InMemoryService.class";
	}


}
