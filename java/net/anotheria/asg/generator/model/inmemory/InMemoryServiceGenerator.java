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
import net.anotheria.asg.generator.model.db.VOGenerator;
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
	    StringBuilder ret = new StringBuilder(5000);

	    ExecutionTimer timer = new ExecutionTimer("InMemory-generateImplementation");
	    timer.startExecution("pre");
		ret.append(CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The in memory implementation of the "+getInterfaceName(module)+"."));

	    ret.append(writeStatement("package "+getPackageName(module)));
	    ret.append(emptyline());
	    appendImport(ret, "java.util.List");
	    ret.append(writeImport("java.util.Map"));
	    ret.append(writeImport("java.util.ArrayList"));
	    ret.append(writeImport("java.util.HashMap"));
	    ret.append(writeImport("java.util.concurrent.atomic.AtomicLong"));


	    ret.append(writeImport("net.anotheria.util.sorter.SortType"));
	    ret.append(writeImport("net.anotheria.util.sorter.StaticQuickSorter"));
		//ret.append(writeImport("net.anotheria.util.Date"));
	    ret.append(writeImport(context.getServicePackageName(MetaModule.SHARED)+".BasicService"));
	    ret.append(emptyline());

	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        appendImport(ret, DataFacadeGenerator.getDocumentImport(context, doc));
	        appendImport(ret, DataFacadeGenerator.getDocumentFactoryImport(context, doc));
	    }
	    emptyline(ret);
	    appendImport(ret, "net.anotheria.asg.util.listener.IServiceListener");
	    appendImport(ret, "net.anotheria.anodoc.query2.DocumentQuery");
	    appendImport(ret, "net.anotheria.anodoc.query2.QueryResult");
	    appendImport(ret, "net.anotheria.anodoc.query2.QueryResultEntry");
	    appendImport(ret, "net.anotheria.anodoc.query2.QueryProperty");
	    
	    emptyline(ret);
	    appendImport(ret, "net.anotheria.util.xml.XMLNode");
	    emptyline(ret);
	    appendImport(ret, "net.anotheria.asg.exception.ASGRuntimeException");
	    appendImport(ret, "net.anotheria.asg.service.InMemoryService");
	    appendImport(ret, "net.anotheria.asg.service.InMemoryObjectWrapper");
	    appendImport(ret, ServiceGenerator.getInterfaceImport(context, module));
	    appendImport(ret, ServiceGenerator.getExceptionImport(context, module));
	    emptyline(ret);
	    
	    
	    ret.append(writeString("public class "+getImplementationName(module)+" extends BasicService implements "+getInterfaceName(module)+", InMemoryService<"+getInterfaceName(module)+"> {"));
	    increaseIdent();
	    ret.append(writeStatement("private static "+getImplementationName(module)+" instance"));
	    ret.append(emptyline());
	    
	    ret.append(writeStatement("private boolean paired"));

	    //generate storage
	    for (MetaDocument doc : docs){
	        ret.append(writeStatement("private Map<String, InMemoryObjectWrapper<"+doc.getName()+">> "+getCacheName(doc)));
		    ret.append(writeStatement("private AtomicLong "+getLastIdName(doc)+" = new AtomicLong()"));
	        ret.append(writeStatement("private List<"+doc.getName()+"> "+getCachedListName(doc)));
	        ret.append(writeStatement("private Object "+doc.getName()+"Lock = new Object()"));

	    }
	    ret.append(emptyline());
	    
	    
	    ret.append(writeString("private "+getImplementationName(module)+"(){"));
	    increaseIdent();
	    ret.append(writeStatement("reset()"));
	    ret.append(closeBlock());
	    ret.append(emptyline());
	    
	    ret.append(writeString("static final "+getImplementationName(module)+" getInstance(){"));
	    increaseIdent();
	    ret.append(writeString("if (instance==null){"));
	    increaseIdent();
	    ret.append(writeStatement("instance = new "+getImplementationName(module)+"()"));
	    ret.append(closeBlock());
	    ret.append(writeStatement("return instance"));
	    ret.append(closeBlock());
	    ret.append(emptyline());
	    
	    String throwsClause = " throws "+getExceptionName(module)+" ";
	    boolean containsAnyMultilingualDocs = false;
	    
	    timer.stopExecution("pre");
	    timer.startExecution("main");
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        timer.startExecution(doc.getName()+"Document");
	        String listDecl = "List<"+doc.getName()+">";
	        String wrapperDecl = getWrapperDecl(doc);
	        
	        ret.append(writeString("private "+listDecl+" _getCached"+doc.getMultiple()+"()"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeString("if ("+getCachedListName(doc)+" != null )"));
	        ret.append(writeIncreasedStatement("return "+getCachedListName(doc)));
	        ret.append(writeString("synchronized("+doc.getName()+"Lock){"));
	        increaseIdent();
	        ret.append(writeString("if ("+getCachedListName(doc)+" != null )"));
	        ret.append(writeIncreasedStatement("return "+getCachedListName(doc)));

	        ret.append(writeStatement(listDecl+"tmp = new ArrayList<"+doc.getName()+">("+getCacheName(doc)+".size())"));
	        ret.append(writeString("for ("+wrapperDecl+" wrapper  : "+getCacheName(doc)+".values()){"));
	        increaseIdent();
	        ret.append(writeString("if (wrapper.get()!=null)"));
	        ret.append(writeIncreasedStatement("tmp.add(wrapper.get())"));
	        closeBlock(ret);
	        appendStatement(ret, getCachedListName(doc)+" = tmp");
	        ret.append(writeStatement("return "+getCachedListName(doc)));
	        closeBlock(ret);
	        closeBlock(ret);
	        emptyline(ret);
	        
	        
	        ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("return _getCached"+doc.getMultiple()+"()"));
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType)"+throwsClause+"{"));
			increaseIdent();
			ret.append(writeStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"(), sortType)"));
			closeBlock(ret);
			emptyline(ret);
	        
	        ret.append(writeString("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("delete"+doc.getName()+"("+doc.getVariableName()+".getId())"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        ret.append(writeString("public void delete"+doc.getName()+"(String id)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeString("if (paired){"));
	        increaseIdent();
	        ret.append(writeStatement(getWrapperDecl(doc)+" w = "+getCacheName(doc)+".get(id)"));
	        ret.append(writeString("if (w!=null)"));
	        ret.append(writeIncreasedStatement("w.delete()"));
	        decreaseIdent();
	        ret.append(writeString("}else{"));
	        increaseIdent();
	        ret.append(writeStatement(getCacheName(doc)+".remove(id)"));
	        ret.append(closeBlock());
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
//*/	        

	        ret.append(writeString("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement(wrapperDecl+" w = "+getCacheName(doc)+".get(id)"));
	        ret.append(writeString("if (w==null || w.get()==null)"));
	        ret.append(writeIncreasedStatement("throw new "+getExceptionName(module)+"(\"No such "+doc.getName()+" with id: \"+id)"));
	        ret.append(writeStatement("return w.get()"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        ret.append(writeString("private "+doc.getName()+" createNewObject(String anId, "+doc.getName()+" template){"));
	        increaseIdent();
	        ret.append(writeString("if (template instanceof net.anotheria.asg.data.AbstractVO){"));
	        increaseIdent();
	        appendStatement(ret, doc.getName(), " ret = ",DataFacadeGenerator.getDocumentFactoryName(doc), ".create", doc.getName(), "(template)");
	    	appendStatement(ret, "((", VOGenerator.getDocumentImport(context, doc), ")ret).copyAttributesFrom(template)" );
	    	appendStatement(ret, "return ret");
	        
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        ret.append(writeString("if (template instanceof net.anotheria.anodoc.data.Document){"));
	        increaseIdent();
	        ret.append(writeStatement("throw new RuntimeException(\"Document creation not yet supported\")"));
	        ret.append(closeBlock());
	        
	        ret.append(writeStatement("throw new RuntimeException(\"Unknown document type: \"+template.getClass())"));

	        ret.append(closeBlock());
///*	        
	        ret.append(writeString("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("long nextId = "+getLastIdName(doc)+".incrementAndGet()"));
	        ret.append(writeStatement(doc.getVariableName()+" = createNewObject(\"\"+nextId, "+doc.getVariableName()+")"));
	        ret.append(writeStatement(wrapperDecl+" wrapper = new "+wrapperDecl+"("+doc.getVariableName()+", paired)"));
	        ret.append(writeStatement("// should check whether an object with this id already exists... which however can only happen in case of an error "));
	        ret.append(writeStatement(getCacheName(doc)+".put(wrapper.getId(), wrapper)"));
	        ret.append(writeString("if (hasServiceListeners()){"));
	        increaseIdent();
	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
	        ret.append(writeIncreasedStatement("myListeners.get(i).documentCreated("+doc.getVariableName()+")"));
	        ret.append(closeBlock());
	        ret.append(writeStatement("return "+doc.getVariableName()));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
///*	        
	        
	        ret.append(writeComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions."));
	        ret.append(writeString("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement(listDecl+" ret = new ArrayList<"+doc.getName()+">(list.size())"));
	        
	        ret.append(writeString("for ("+doc.getName()+" "+doc.getVariableName()+" : list){"));
	        increaseIdent();
	        ret.append(writeStatement("long nextId = "+getLastIdName(doc)+".incrementAndGet()"));
	        ret.append(writeStatement(doc.getVariableName()+" = createNewObject(\"\"+nextId, "+doc.getVariableName()+")"));
	        ret.append(writeStatement(wrapperDecl+" wrapper = new "+wrapperDecl+"("+doc.getVariableName()+", paired)"));
	        ret.append(writeStatement("// should check whether an object with this id already exists... which however can only happen in case of an error "));
	        ret.append(writeStatement(getCacheName(doc)+".put(wrapper.getId(), wrapper)"));
	        ret.append(writeStatement("ret.add("+doc.getVariableName()+")"));
	        ret.append(closeBlock());
	        
	        ret.append(writeString("if (hasServiceListeners()){"));
	        increaseIdent();
	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
	        increaseIdent();
	        ret.append(writeString("for ("+doc.getName()+" "+doc.getVariableName()+" : ret)"));
	        ret.append(writeIncreasedStatement("myListeners.get(i).documentCreated("+doc.getVariableName()+")"));
	        decreaseIdent();
	        ret.append(closeBlock());
	        ret.append(writeStatement("return ret"));
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        //*/
	        
	        ret.append(writeComment("Updates multiple new "+doc.getName()+" objects.\nReturns the updated versions."));
	        ret.append(writeString("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement(listDecl+" old = new ArrayList<"+doc.getName()+">(list.size())"));
	        ret.append(writeString("for ("+doc.getName()+" "+doc.getVariableName()+" : list){"));
	        increaseIdent();
	        ret.append(writeStatement(wrapperDecl+" wrapper = "+getCacheName(doc)+".get("+doc.getVariableName()+".getId())"));
	        ret.append(writeString("if (wrapper==null || wrapper.get()==null)"));
	        ret.append(writeIncreasedStatement("throw new "+getExceptionName(module)+"(\"No such "+doc.getName()+" with id: \"+"+doc.getVariableName()+".getId())"));
	        ret.append(writeStatement("old.add(wrapper.get())"));
	        ret.append(writeStatement("wrapper.update("+doc.getVariableName()+")"));
	        ret.append(closeBlock());
	        
	        ret.append(writeString("if (hasServiceListeners()){"));
	        increaseIdent();
	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
	        increaseIdent();
	        ret.append(writeString("for (int t = 0; t<list.size(); t++){"));
	        ret.append(writeIncreasedStatement("myListeners.get(i).documentUpdated(old.get(i), list.get(i))"));
	        ret.append(writeString("}"));
	        decreaseIdent();
	        closeBlock(ret);
	        ret.append(writeStatement("return list"));
	        closeBlock(ret);
	        emptyline(ret);
//*/
	        
	        ret.append(writeString("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("String id = "+doc.getVariableName()+".getId()"));
	        ret.append(writeStatement(doc.getName()+" oldVersion = null"));
	        ret.append(writeStatement(wrapperDecl+" w = "+getCacheName(doc)+".get(id)"));
	        ret.append(writeString("if (w==null || w.get()==null)"));
	        ret.append(writeIncreasedStatement("throw new "+getExceptionName(module)+"(\"No such "+doc.getName()+" with id: \"+id)"));
	        ret.append(writeString("if (hasServiceListeners())"));
	        ret.append(writeIncreasedStatement("oldVersion = w.get()"));
	        ret.append(writeStatement("w.update("+doc.getVariableName()+")"));
	        
	        
	        ret.append(writeString("if (hasServiceListeners()){"));
	        increaseIdent();
	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
	        ret.append(writeIncreasedStatement("myListeners.get(i).documentUpdated(oldVersion, "+doc.getVariableName()+")"));
	        ret.append(closeBlock());
	        
	        ret.append(writeStatement("return "+doc.getVariableName()));
	        ret.append(closeBlock());
	        ret.append(emptyline());
///*	        
	        ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("throw new RuntimeException(\"Not yet implemented\")"));
/*			ret.append(writeStatement("QueryProperty p = new QueryProperty(propertyName, value)");
			ret.append(writeString("try{");
			ret.append(writeIncreasedStatement("return pService.get"+doc.getMultiple()+"ByProperty(p)");
			ret.append(writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
			ret.append(writeIncreasedStatement("throw new RuntimeException(\"Persistence failed: \"+e.getMessage())");
			ret.append(writeString("}");
*/			
      		ret.append(closeBlock());
	        ret.append(emptyline());
//*/
	        
			ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)"+throwsClause+"{"));
			increaseIdent();
			ret.append(writeStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(propertyName, value), sortType)"));
			ret.append(closeBlock());
	        ret.append(emptyline());

	        // /*			
			ret.append(writeComment("Executes a query on "+doc.getMultiple()));
			ret.append(writeString("public QueryResult executeQueryOn"+doc.getMultiple()+"(DocumentQuery query)"+throwsClause+"{"));
			increaseIdent();
			ret.append(writeStatement(listDecl+" all"+doc.getMultiple()+" = get"+doc.getMultiple()+"()"));
			ret.append(writeStatement("QueryResult result = new QueryResult()"));
			ret.append(writeString("for (int i=0; i<all"+doc.getMultiple()+".size(); i++){"));
			increaseIdent();
			ret.append(writeStatement("List<QueryResultEntry> partialResult = query.match(all"+doc.getMultiple()+".get(i))"));
			ret.append(writeStatement("result.add(partialResult)"));
			ret.append(closeBlock());
			
			ret.append(writeStatement("return result"));
			ret.append(closeBlock());
			ret.append(emptyline());

			ret.append(writeComment("Returns all "+doc.getName()+" objects, where property matches."));
	        ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(QueryProperty... property)"+throwsClause+"{"));
			increaseIdent();
			ret.append(writeString("//first the slow version, the fast version is a todo."));
			ret.append(writeStatement(listDecl+" ret = new ArrayList<"+doc.getName()+">()"));
			ret.append(writeStatement(listDecl+" src = get"+doc.getMultiple()+"()"));
			ret.append(writeStatement("for ( "+doc.getName()+" "+doc.getVariableName() +" : src){"));
			increaseIdent();
			ret.append(writeStatement("boolean mayPass = true"));
			ret.append(writeStatement("for (QueryProperty qp : property){"));
			increaseIdent();
			ret.append(writeStatement("mayPass = mayPass && qp.doesMatch("+doc.getVariableName()+".getPropertyValue(qp.getName()))"));
			closeBlock(ret);
			
			ret.append(writeString("if (mayPass)"));
			ret.append(writeIncreasedStatement("ret.add("+doc.getVariableName()+")"));
			closeBlock(ret);
			
			ret.append(writeStatement("return ret"));
			ret.append(closeBlock());
	        ret.append(emptyline());
/*
	        increaseIdent();
			ret.append(writeString("try{");
			ret.append(writeIncreasedStatement("return pService.get"+doc.getMultiple()+"ByProperty(property)");
			ret.append(writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
			ret.append(writeIncreasedStatement("throw new RuntimeException(\"Persistence failed: \"+e.getMessage())");
			ret.append(writeString("}");
	        ret.append(closeBlock();
	        ret.append(emptyline();
	*/        
			ret.append(writeComment("Returns all "+doc.getName()+" objects, where property matches, sorted"));
			ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(property), sortType)"));
	        ret.append(closeBlock());
			ret.append(emptyline());
		//	*/
			
			if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
				ret.append(writeComment("In all documents of type "+doc.getName()+" copies all multilingual fields from sourceLanguage to targetLanguage"));
				ret.append(writeStatement("public void copyMultilingualAttributesInAll"+doc.getMultiple()+"(String sourceLanguage, String targetLanguage)"+throwsClause+"{"));
				increaseIdent();
		        ret.append(writeStatement("throw new RuntimeException(\"Not yet implemented\")"));
				ret.append(closeBlock());
				ret.append(emptyline());
				containsAnyMultilingualDocs = true;
			}
	    
	        timer.stopExecution(doc.getName()+"Document");
	    }

	    timer.stopExecution("main");
	    timer.startExecution("foot");

	    
	    if (containsAnyMultilingualDocs){
			ret.append(writeComment("Copies all multilingual fields from sourceLanguage to targetLanguage in all data objects (documents, vo) which are part of this module and managed by this service"));
			ret.append(writeString("public void copyMultilingualAttributesInAllObjects(String sourceLanguage, String targetLanguage)"+throwsClause+"{"));
			increaseIdent();
	        ret.append(writeStatement("throw new RuntimeException(\"Not yet implemented\")"));
			ret.append(closeBlock());
			ret.append(emptyline());
			
	    }

	    //generate export function
	    ret.append(emptyline());
	    ret.append(writeString("public XMLNode exportToXML()"+throwsClause+"{"));
	    increaseIdent();
        ret.append(writeStatement("return new XMLNode("+quote("unimplemented_inmemory_export_"+module.getName())+")"));
	    ret.append(closeBlock());
	    
	    //*/

	    ret.append(writeString("public void pairTo("+getInterfaceName(module)+" instance){"));
	    increaseIdent();
	    ret.append(writeStatement("throw new RuntimeException(\"Not yet implemented\")"));
	    ret.append(closeBlock());
	    ret.append(emptyline());
	    
	    ret.append(writeString("public void unpair("+getInterfaceName(module)+" instance){"));
	    increaseIdent();
	    ret.append(writeStatement("throw new RuntimeException(\"Not yet implemented\")"));
	    ret.append(closeBlock());
	    ret.append(emptyline());

	    ret.append(writeString("public void synchBack(){"));
	    increaseIdent();
	    ret.append(writeStatement("throw new RuntimeException(\"Not yet implemented\")"));
	    ret.append(closeBlock());
	    ret.append(emptyline());

	    
	    for (MetaDocument doc : docs){
		    ret.append(writeString("public void read"+doc.getName()+"From("+getInterfaceName(module)+" instance) throws ASGRuntimeException{"));
		    increaseIdent();
		    ret.append(writeStatement("List<"+doc.getName()+"> list = instance.get"+doc.getMultiple()+"()"));
		    ret.append(writeStatement("long maxId = 0"));
		    ret.append(writeString("for ("+doc.getName()+" "+doc.getVariableName()+" : list){ "));
		    increaseIdent();
		    ret.append(writeStatement("long id = Long.parseLong("+doc.getVariableName()+".getId())"));
		    ret.append(writeString("if (id>maxId)"));
		    ret.append(writeIncreasedStatement("maxId = id"));
		    ret.append(writeStatement(getCacheName(doc)+".put("+doc.getVariableName()+".getId(), new InMemoryObjectWrapper<"+doc.getName()+">("+doc.getVariableName()+"))"));
		    closeBlock(ret);
		    ret.append(writeStatement(getLastIdName(doc)+" = new AtomicLong(maxId)"));
		    ret.append(writeStatement(getCachedListName(doc)+" = null"));
		    closeBlock(ret);
		    ret.append(emptyline());
	    }

	    ret.append(writeString("public void readFrom("+getInterfaceName(module)+" instance) throws ASGRuntimeException {"));
	    increaseIdent();
	    for (MetaDocument doc : docs){
	    	ret.append(writeStatement("read"+doc.getName()+"From(instance)"));
	    }
	    ret.append(closeBlock());
	    ret.append(emptyline());
		
	    ret.append(writeString("public void synchTo("+getInterfaceName(module)+" instance){"));
	    increaseIdent();
	    ret.append(writeStatement("throw new RuntimeException(\"Not yet implemented\")"));
	    ret.append(closeBlock());
	    ret.append(emptyline());
	    
	    ret.append(writeString("public void clear(){"));
	    increaseIdent();
	    ret.append(writeString("if (paired) "));
	    ret.append(writeIncreasedStatement("throw new RuntimeException(\"Cant reset a paired copy, unpair it first\")"));
	    ret.append(writeStatement("reset()"));
	    ret.append(closeBlock());
	    
	    ret.append(writeString("private void reset(){"));
	    increaseIdent();
	    for (MetaDocument doc : docs){
	        ret.append(writeStatement(getCacheName(doc)+" = new HashMap<String, InMemoryObjectWrapper<"+doc.getName()+">>()"));
		    ret.append(writeStatement(getLastIdName(doc)+" = new AtomicLong(0)"));
		    ret.append(writeStatement(getCachedListName(doc)+" = null"));
	    }
	    ret.append(closeBlock());
    

	    closeBlock(ret);
	    timer.stopExecution("foot");
	    //timer.printExecutionTimesOrderedByCreation();
	    
	    return ret.toString();
	}
	
	
	public static String getPackageName(Context context, MetaModule module){
		return context.getPackageName(module)+".service.inmemory";
	}
	
	protected String getSupportedInterfacesList(MetaModule module){
		return super.getSupportedInterfacesList(module)+", InMemoryService.class";
	}


}
