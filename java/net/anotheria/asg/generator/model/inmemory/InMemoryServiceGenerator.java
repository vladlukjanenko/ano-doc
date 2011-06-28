package net.anotheria.asg.generator.model.inmemory;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GenerationOptions;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.AbstractServiceGenerator;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.asg.service.InMemoryService;
import net.anotheria.util.ExecutionTimer;

/**
 * Generates an inmemory implementation of a module interface and the according factory.
 * @author another
 *
 */
public class InMemoryServiceGenerator extends AbstractServiceGenerator implements IGenerator{
	
	@Override public List<FileEntry> generate(IGenerateable gmodule){
		
		MetaModule mod = (MetaModule)gmodule;
		if (!mod.isEnabledByOptions(GenerationOptions.INMEMORY))
			return new ArrayList<FileEntry>();

		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ExecutionTimer timer = new ExecutionTimer("InMemory Generator");
		timer.startExecution(mod.getName()+"Factory");
		ret.add(new FileEntry(generateFactory(mod)));
		timer.stopExecution(mod.getName()+"Factory");
		
		timer.startExecution(mod.getName()+"Impl");
		ret.add(new FileEntry(generateImplementation(mod)));
		timer.stopExecution(mod.getName()+"Impl");
		
		//timer.printExecutionTimesOrderedByCreation();
		
		return ret;
	}

	@Override public String getImplementationName(MetaModule m){
	    return "InMemory"+getServiceName(m)+"Impl";
	}
	
	
	public static String getInMemoryFactoryName(MetaModule m){
		return "InMemory"+getServiceName(m)+"Factory";
	}
	
	public String getFactoryName(MetaModule m){
	    return getInMemoryFactoryName(m);
	}
	
	public static String getInMemoryFactoryImport(MetaModule m){
	    return getPackageName(GeneratorDataRegistry.getInstance().getContext(), m)+"."+getInMemoryFactoryName(m);
	}
	
	@Override protected String getPackageName(MetaModule module){
		return getPackageName(GeneratorDataRegistry.getInstance().getContext(), module);
	}
	
	@Override protected void addAdditionalFactoryImports(GeneratedClass clazz, MetaModule module){
		clazz.addImport(GeneratorDataRegistry.getInstance().getContext().getServicePackageName(module)+"."+getInterfaceName(module));
		clazz.addImport(InMemoryService.class);
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
	private GeneratedClass generateImplementation(MetaModule module){

		Context context = GeneratorDataRegistry.getInstance().getContext();
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
	    ExecutionTimer timer = new ExecutionTimer("InMemory-generateImplementation");
	    timer.startExecution("pre");
	    
	    clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The in memory implementation of the "+getInterfaceName(module)+"."));
	    clazz.setPackageName(getPackageName(module));

	    clazz.addImport("java.util.List");
	    clazz.addImport("java.util.Map");
	    clazz.addImport("java.util.ArrayList");
	    clazz.addImport("java.util.HashMap");
	    clazz.addImport("java.util.concurrent.atomic.AtomicLong");


	    clazz.addImport("net.anotheria.util.sorter.SortType");
	    clazz.addImport("net.anotheria.util.sorter.StaticQuickSorter");
	    clazz.addImport(context.getServicePackageName(MetaModule.SHARED)+".BasicService");

	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
//	        clazz.addImport(DataFacadeGenerator.getDocumentFactoryImport(context, doc));
	        clazz.addImport(DataFacadeGenerator.getXMLHelperImport(context, doc));
	    }
	    clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResultEntry");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
	    
	    clazz.addImport("net.anotheria.util.xml.XMLNode");
	    clazz.addImport("net.anotheria.util.xml.XMLAttribute");
	    clazz.addImport("net.anotheria.util.slicer.Segment");
	    clazz.addImport("net.anotheria.util.slicer.Slicer");
	    clazz.addImport("net.anotheria.asg.exception.ASGRuntimeException");
	    clazz.addImport("net.anotheria.asg.service.InMemoryService");
	    clazz.addImport("net.anotheria.asg.service.InMemoryObjectWrapper");
	    clazz.addImport(ServiceGenerator.getInterfaceImport(module));
	    clazz.addImport(ServiceGenerator.getExceptionImport(module));

	    clazz.setName(getImplementationName(module));
	    clazz.setParent("BasicService");
	    clazz.addInterface(getInterfaceName(module));
	    clazz.addInterface("InMemoryService<"+getInterfaceName(module)+">");
	    
	    startClassBody();
	    appendStatement("private static "+getImplementationName(module)+" instance");
	    emptyline();
	    
	    appendStatement("private boolean paired");
	    appendStatement(getInterfaceName(module)," pairedInstance = null");
	    emptyline();

	    //generate storage
	    for (MetaDocument doc : docs){
	        appendStatement("private Map<String, InMemoryObjectWrapper<"+doc.getName()+">> "+getCacheName(doc));
		    appendStatement("private AtomicLong "+getLastIdName(doc)+" = new AtomicLong()");
	        appendStatement("private List<"+doc.getName()+"> "+getCachedListName(doc));
	        appendStatement("private Object "+doc.getName()+"Lock = new Object()");

	    }
	    emptyline();
	    
	    
	    appendString("private "+getImplementationName(module)+"(){");
	    increaseIdent();
	    appendStatement("reset()");
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
	        closeBlockNEW();
	        appendStatement( getCachedListName(doc)+" = tmp");
	        appendStatement("return "+getCachedListName(doc));
	        closeBlockNEW();
	        closeBlockNEW();
	        emptyline();
	        
	        
	        appendString("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("return _getCached"+doc.getMultiple()+"()");
	        closeBlockNEW();
	        emptyline();

	        appendString("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType)"+throwsClause+"{");
			increaseIdent();
			appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"(), sortType)");
			closeBlockNEW();
			emptyline();
	        
	        appendString("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("delete"+doc.getName()+"("+doc.getVariableName()+".getId())");
            appendString("if (hasServiceListeners())");
	        appendIncreasedStatement("fireObjectDeletedEvent("+doc.getVariableName()+")");
	        closeBlockNEW();
	        emptyline();
	        
	        appendString("public void delete"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
            appendStatement(doc.getName()+ " doc = null");
	        appendString("if (paired){");
	        increaseIdent();
	        appendStatement(getWrapperDecl(doc)+" w = "+getCacheName(doc)+".get(id)");
            appendStatement("doc = hasServiceListeners() && w!=null?w.get():null");
	        appendString("if (w!=null)");
	        appendIncreasedStatement("w.delete()");
	        decreaseIdent();
	        appendString("}else{");
	        increaseIdent();
            appendStatement("doc=hasServiceListeners() && "+getCacheName(doc)+".get(id)!=null?"+getCacheName(doc)+".get(id).get():null");
	        appendStatement(getCacheName(doc)+".remove(id)");
	        closeBlockNEW();
            appendString("if (doc!=null)");
            appendIncreasedStatement("fireObjectDeletedEvent(doc)");
	        closeBlockNEW();
	        emptyline();

	        
	        appendComment("Deletes multiple "+doc.getName()+" objects.");
	        appendString("public void delete"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : list){");
	        increaseIdent();
	        appendStatement(wrapperDecl+" wrapper = "+getCacheName(doc)+".get("+doc.getVariableName()+".getId())");
	        appendString("if (wrapper==null || wrapper.get()==null)");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"No such "+doc.getName()+" with id: \"+"+doc.getVariableName()+".getId())");
	        appendStatement("wrapper.delete()");
	        closeBlockNEW();
	        
	        appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendString("for (int t = 0; t<list.size(); t++)");
	        appendIncreasedStatement("fireObjectDeletedEvent(list.get(t))");
	        closeBlockNEW();

	        closeBlockNEW();
	        emptyline();
	        
//*/	        

	        appendString("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(wrapperDecl+" w = "+getCacheName(doc)+".get(id)");
	        appendString("if (w==null || w.get()==null)");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"No such "+doc.getName()+" with id: \"+id)");
	        appendStatement("return w.get()");
	        closeBlockNEW();
	        emptyline();
	        
	        appendString("private "+doc.getName()+" createNewObject(String anId, "+doc.getName()+" template){");
	        increaseIdent();
	        appendString("if (template instanceof net.anotheria.asg.data.AbstractVO){");
	        increaseIdent();
	        appendStatement("throw new RuntimeException(",quote("Not yet implemented"), ")");
	        //appendStatement(ret, doc.getName(), " ret = ",DataFacadeGenerator.getDocumentFactoryName(doc), ".create", doc.getName(), "(anId)");
	    	//appendStatement(ret, "((net.anotheria.asg.data.AbstractVO)ret).copyAttributesFrom(template)" );
	    	//appendStatement(ret, "return ret");
	        
	        closeBlockNEW();
	        emptyline();

	        appendString("if (template instanceof net.anotheria.anodoc.data.Document){");
	        increaseIdent();
	        appendStatement("throw new RuntimeException(\"Document creation not yet supported\")");
	        closeBlockNEW();
	        
	        appendStatement("throw new RuntimeException(\"Unknown document type: \"+template.getClass())");

	        closeBlockNEW();
            emptyline();


	        appendString("public "+doc.getName()+" import"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("InMemoryObjectWrapper<", doc.getName(),"> wrapper = new InMemoryObjectWrapper<",doc.getName(),">(",doc.getVariableName(),")");
	        appendStatement(getCacheName(doc), ".put(wrapper.getId(), wrapper)");
	        appendStatement(getCachedListName(doc), " = null");
            appendString("if (hasServiceListeners())");
	        appendIncreasedStatement("fireObjectImportedEvent("+doc.getVariableName()+")");
	        appendStatement("return "+doc.getVariableName());
	        closeBlockNEW();
	        emptyline();

            appendString("public "+listDecl+" import"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = new ArrayList<"+doc.getName()+">(list.size())");
	        appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : list){");
	        increaseIdent();
	        appendStatement("ret.add(import"+doc.getName()+"("+doc.getVariableName()+"))");
            closeBlockNEW();
            appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : ret)");
	        appendIncreasedStatement("fireObjectImportedEvent("+doc.getVariableName()+")");
	        closeBlockNEW();
            appendStatement("return ret");
	        closeBlockNEW();
	        emptyline();
	        
///*	        
	        appendString("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("long nextId = "+getLastIdName(doc)+".incrementAndGet()");
	        appendStatement(doc.getVariableName()+" = createNewObject(\"\"+nextId, "+doc.getVariableName()+")");
	        appendStatement(wrapperDecl+" wrapper = new "+wrapperDecl+"("+doc.getVariableName()+", paired)");
	        appendStatement("// should check whether an object with this id already exists... which however can only happen in case of an error ");
	        appendStatement(getCacheName(doc)+".put(wrapper.getId(), wrapper)");
	        appendStatement(getCachedListName(doc), " = null");
	        appendIncreasedStatement("fireObjectCreatedEvent("+doc.getVariableName()+")");
	        appendStatement("return "+doc.getVariableName());
	        closeBlockNEW();
	        emptyline();
	        
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
	        closeBlockNEW();
	        
	        appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : ret)");
	        appendIncreasedStatement("fireObjectCreatedEvent("+doc.getVariableName()+")");
	        closeBlockNEW();
	        appendStatement("return ret");
	        closeBlockNEW();
	        emptyline();

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
	        closeBlockNEW();
	        
	        appendString("if (hasServiceListeners()){");
	        increaseIdent();
	        appendString("for (int t = 0; t<list.size(); t++)");
	        appendIncreasedStatement("fireObjectUpdatedEvent(old.get(t), list.get(t))");
	        closeBlockNEW();
	        appendStatement("return list");
	        closeBlockNEW();
	        emptyline();
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
	        appendString("if (oldVersion!=null)");
	        appendIncreasedStatement("fireObjectUpdatedEvent(oldVersion, "+doc.getVariableName()+")");
	        
	        appendStatement("return "+doc.getVariableName());
	        closeBlockNEW();
	        emptyline();
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
      		closeBlockNEW();
	        emptyline();
//*/
	        
			appendString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)"+throwsClause+"{");
			increaseIdent();
			appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(propertyName, value), sortType)");
			closeBlockNEW();
	        emptyline();

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
			closeBlockNEW();
			
			appendStatement("return result");
			closeBlockNEW();
			emptyline();

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
			closeBlockNEW();
			
			appendString("if (mayPass)");
			appendIncreasedStatement("ret.add("+doc.getVariableName()+")");
			closeBlockNEW();
			
			appendStatement("return ret");
			closeBlockNEW();
	        emptyline();
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
			appendComment("Returns all "+doc.getName()+" objects, where property matches, sorted.");
			appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(property), sortType)");
	        closeBlockNEW();
			emptyline();
		//	*/

			// get elements COUNT
			appendComment("Returns " + doc.getName() + " objects count.");
			appendString("public int get" + doc.getMultiple() + "Count()" + throwsClause + "{");
			increaseIdent();
			appendStatement("return _getCached" + doc.getMultiple() + "().size()");
			closeBlockNEW();
			emptyline();
			// end get elements COUNT

			// get elements Segment
			appendComment("Returns " + doc.getName() + " objects segment.");
			appendString("public " + listDecl + " get" + doc.getMultiple() + "(Segment aSegment)" + throwsClause + "{");
			increaseIdent();
			appendStatement("return Slicer.slice(aSegment, get" + doc.getMultiple() + "()).getSliceData()");
			closeBlockNEW();
			emptyline();
			// end get elements Segment

			// get elements Segment with FILTER
			appendComment("Returns " + doc.getName() + " objects segment, where property matched.");
			appendString("public " + listDecl + " get" + doc.getMultiple() + "ByProperty(Segment aSegment, QueryProperty... property)"
					+ throwsClause + "{");
			increaseIdent();
			appendStatement("int pLimit = aSegment.getElementsPerSlice()");
			appendStatement("int pOffset = aSegment.getSliceNumber() * aSegment.getElementsPerSlice() - aSegment.getElementsPerSlice()");
			appendStatement(listDecl + " ret = new ArrayList<" + doc.getName() + ">()");
			appendStatement(listDecl + " src = _getCached" + doc.getMultiple() + "()");
			appendStatement("for (" + doc.getName() + " " + doc.getVariableName() + " : src) {");
			increaseIdent();
			appendStatement("boolean mayPass = true");
			appendStatement("for (QueryProperty qp : property) {");
			increaseIdent();
			appendStatement("mayPass = mayPass && qp.doesMatch(" + doc.getVariableName() + ".getPropertyValue(qp.getName()))");
			closeBlockNEW();
			appendString("if (mayPass)");
			appendIncreasedStatement("ret.add(" + doc.getVariableName() + ")");
			appendString("if (ret.size() > pOffset + pLimit)");			
			appendIncreasedStatement("break");
			closeBlockNEW();
			appendStatement("return Slicer.slice(aSegment, ret).getSliceData()");
			closeBlockNEW();
			emptyline();
			// end get elements Segment with FILTER

			// get elements Segment with SORTING, FILTER
			appendComment("Returns " + doc.getName() + " objects segment, where property matched, sorted.");
			appendString("public " + listDecl + " get" + doc.getMultiple()
					+ "ByProperty(Segment aSegment, SortType aSortType, QueryProperty... aProperty)" + throwsClause + "{");
			increaseIdent();
			appendStatement("return StaticQuickSorter.sort(get" + doc.getMultiple() + "ByProperty(aSegment, aProperty), aSortType)");
			closeBlockNEW();
			emptyline();
			// end get elements Segment with SORTING, FILTER
			
			if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
				appendComment("In all documents of type "+doc.getName()+" copies all multilingual fields from sourceLanguage to targetLanguage");
				appendStatement("public void copyMultilingualAttributesInAll"+doc.getMultiple()+"(String sourceLanguage, String targetLanguage)"+throwsClause+"{");
				increaseIdent();
		        appendStatement("throw new RuntimeException(\"Not yet implemented\")");
				closeBlockNEW();
				emptyline();
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
			closeBlockNEW();
			emptyline();
			
	    }

	  //generate export function
	    emptyline();
	    for (MetaDocument d : docs){
	    	appendString("public XMLNode export"+d.getMultiple()+"ToXML(){");
	    	increaseIdent();
	    	appendStatement("XMLNode ret = new XMLNode("+quote(d.getMultiple())+")");
	    	
	    	appendIncreasedString("try{");
	    	increaseIdent();
	    	appendStatement("List<"+d.getName()+"> list = get"+d.getMultiple()+"()");
	    	appendStatement("ret.addAttribute(new XMLAttribute("+quote("count")+", list.size()))");
	    	appendString("for ("+d.getName()+" object : list)");
	    	appendIncreasedStatement("ret.addChildNode("+DataFacadeGenerator.getXMLHelperName(d)+".toXML(object))");
	    	appendStatement("return ret");
	    	closeBlockNEW();
	    	appendString("catch("+getExceptionName(module)+" e){");
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

	    
	    	appendString("public XMLNode export"+d.getMultiple()+"ToXML(String[] languages){");
	    	increaseIdent();
	    	appendStatement("XMLNode ret = new XMLNode("+quote(d.getMultiple())+")");
	    	
	    	appendIncreasedString("try{");
	    	increaseIdent();
	    	appendStatement("List<"+d.getName()+"> list = get"+d.getMultiple()+"()");
	    	appendStatement("ret.addAttribute(new XMLAttribute("+quote("count")+", list.size()))");
	    	appendString("for ("+d.getName()+" object : list)");
	    	appendIncreasedStatement("ret.addChildNode("+DataFacadeGenerator.getXMLHelperName(d)+".toXML(object, languages))");
	    	appendStatement("return ret");
	    	closeBlockNEW();
	    	appendString("catch("+getExceptionName(module)+" e){");
	    	increaseIdent();
	    	appendStatement("throw new RuntimeException("+quote("export"+d.getMultiple()+"ToXML() failure: ")+" + e.getStackTrace())");
	    	closeBlockNEW();
	    	closeBlockNEW();
	    	emptyline();

			appendString("public XMLNode export"+d.getMultiple()+"ToXML(String[] languages, List<"+d.getName()+"> list){");
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
	    
	    
	    //*/

	    appendString("public synchronized void pairTo("+getInterfaceName(module)+" instance) throws ASGRuntimeException{");
	    increaseIdent();
	    
	    appendString("if (paired)");
	    appendIncreasedStatement("throw new ASGRuntimeException(", quote("Already paired"), ")");
	    appendStatement( "paired = true");
	    appendStatement( "pairedInstance = instance");
	    appendStatement( "reset()");
	    
		closeBlockNEW();
	    emptyline();
	    
	    appendString("public synchronized void unpair("+getInterfaceName(module)+" instance){");
	    increaseIdent();
	    appendStatement("throw new RuntimeException(\"Not yet implemented\")");
	    closeBlockNEW();
	    emptyline();

	    appendString("public synchronized void synchBack()throws ASGRuntimeException{");
	    increaseIdent();
	    appendString("if (!paired)");
	    appendIncreasedStatement( "throw new ASGRuntimeException(", quote("Not paired"), ")");
	    
	    appendStatement( "throw new RuntimeException(\"Not yet implemented\")");
		closeBlockNEW();
	    emptyline();

	    
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
			closeBlockNEW();
		    appendStatement(getLastIdName(doc)+" = new AtomicLong(maxId)");
		    appendStatement(getCachedListName(doc)+" = null");
			closeBlockNEW();
		    emptyline();
	    }

	    appendString("public void readFrom("+getInterfaceName(module)+" instance) throws ASGRuntimeException {");
	    increaseIdent();
	    for (MetaDocument doc : docs){
	    	appendStatement("read"+doc.getName()+"From(instance)");
	    }
	    closeBlockNEW();
	    emptyline();
		
	    appendString("public void synchTo("+getInterfaceName(module)+" instance)throws ASGRuntimeException{");
	    increaseIdent();
	    appendStatement("throw new RuntimeException(\"Not yet implemented\")");
	    closeBlockNEW();
	    emptyline();
	    
	    appendString("public void clear(){");
	    increaseIdent();
	    appendString("if (paired) ");
	    appendIncreasedStatement("throw new RuntimeException(\"Cant reset a paired copy, unpair it first\")");
	    appendStatement("reset()");
	    closeBlockNEW();
	    
	    appendString("private void reset(){");
	    increaseIdent();
	    for (MetaDocument doc : docs){
	        appendStatement(getCacheName(doc)+" = new HashMap<String, InMemoryObjectWrapper<"+doc.getName()+">>()");
		    appendStatement(getLastIdName(doc)+" = new AtomicLong(0)");
		    appendStatement(getCachedListName(doc)+" = null");
		    emptyline();
	    }
	    closeBlockNEW();
    

	    timer.stopExecution("foot");
	    return clazz;
	}
	
	
	public static String getPackageName(Context context, MetaModule module){
		return context.getPackageName(module)+".service.inmemory";
	}
	
	protected String getSupportedInterfacesList(MetaModule module){
		return super.getSupportedInterfacesList(module)+", InMemoryService.class";
	}

	@Override protected String getMoskitoSubsystem(){
		return super.getMoskitoSubsystem()+"-inmem";
	}

}
