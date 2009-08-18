package net.anotheria.asg.generator.model.fixture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
import net.anotheria.asg.generator.model.db.VOGenerator;
import net.anotheria.util.ExecutionTimer;
import net.anotheria.util.StringUtils;

public class FixtureServiceGenerator  extends AbstractServiceGenerator implements IGenerator{
	
	@Override public List<FileEntry> generate(IGenerateable gmodule){
		
		MetaModule mod = (MetaModule)gmodule;
		if (!mod.isEnabledByOptions(GenerationOptions.FIXTURE))
			return new ArrayList<FileEntry>();
	
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ExecutionTimer timer = new ExecutionTimer("Fixture Generator");
		timer.startExecution(mod.getName()+"Factory");
		ret.add(new FileEntry(generateFactory(mod)));
		timer.stopExecution(mod.getName()+"Factory");
		
		timer.startExecution(mod.getName()+"Impl");
		ret.add(new FileEntry(generateImplementation(mod)));
		timer.stopExecution(mod.getName()+"Impl");
		
		//timer.printExecutionTimesOrderedByCreation();
		
		return ret;
	}
	
	public static String getFixtureFactoryName(MetaModule m){
		return getServiceName(m)+"FixtureFactory";
	}
	
	public String getFactoryName(MetaModule m){
	    return getFixtureFactoryName(m);
	}

	
	/**
	 * Generates the implementation
	 * @param module the metamodule to generate
	 * @return
	 */
	private GeneratedClass generateImplementation(MetaModule module){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		Context context = GeneratorDataRegistry.getInstance().getContext();
		
	    clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The implementation of the "+getInterfaceName(module)+" for unit testing."));
	    clazz.setPackageName(getPackageName(module));
	    	
	    clazz.addImport("java.util.List");
	    clazz.addImport("net.anotheria.util.sorter.SortType");
	    clazz.addImport("net.anotheria.util.sorter.StaticQuickSorter");
	    clazz.addImport(context.getServicePackageName(MetaModule.SHARED)+".BasicService");
	    //ret.emptyline();

	    clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResultEntry");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
	    
	    clazz.addImport("net.anotheria.util.xml.XMLNode");
	    clazz.addImport("net.anotheria.util.xml.XMLAttribute");
	    
	    clazz.addImport(ServiceGenerator.getInterfaceImport(module));
	    clazz.addImport(ServiceGenerator.getExceptionImport(module));

	    clazz.setName(getImplementationName(module));
	    clazz.setParent("BasicService");
	    clazz.addInterface(getInterfaceName(module));
	    
	    startClassBody();
	    appendStatement("private static "+getImplementationName(module)+" instance");
	    emptyline();
	    
	    List<MetaDocument> docs = module.getDocuments();
	    clazz.addImport(ConcurrentHashMap.class);
	    clazz.addImport(Map.class);
	    clazz.addImport(AtomicInteger.class);
	    clazz.addImport(ArrayList.class);
	    
	    for (MetaDocument doc : docs){
	    	appendStatement("private Map<String, "+doc.getName()+"> "+getMapName(doc)+" = new ConcurrentHashMap<String, "+doc.getName()+">()" );
	    	appendStatement("private AtomicInteger "+getIdHolderName(doc) +" = new AtomicInteger(0)");
	    	emptyline();
	    }
	    
	    appendString("private "+getImplementationName(module)+"(){");
	    increaseIdent();
	    //appendStatement("pService = "+JDBCPersistenceServiceGenerator.getFactoryName(module)+".create"+JDBCPersistenceServiceGenerator.getServiceName(module)+"()");
	    if (module.getListeners().size()>0){
	    	for (int i=0; i<module.getListeners().size(); i++){
	    		String listClassName = (String)module.getListeners().get(i);
	    		appendStatement("addServiceListener(new "+listClassName+"())");
	    	}
	    }
	    append(closeBlock());
	    emptyline();
	    
	    appendString("static final "+getImplementationName(module)+" getInstance(){");
	    increaseIdent();
	    appendString("if (instance==null){");
	    increaseIdent();
	    appendStatement("instance = new "+getImplementationName(module)+"()");
	    append(closeBlock());
	    appendStatement("return instance");
	    append(closeBlock());
	    emptyline();
	    

	    String throwsClause = " throws "+getExceptionName(module)+" ";
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";

	        clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
	        clazz.addImport(DataFacadeGenerator.getXMLHelperImport(context, doc));

	        appendString("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = new Array"+listDecl+"()");
	        appendStatement("ret.addAll("+getMapName(doc)+".values())");
	        appendStatement("return ret");
	        append(closeBlock());
	        emptyline();
	        
			appendString("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType)"+throwsClause+"{");
			increaseIdent();
			appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"(), sortType)");
			append(closeBlock());
			emptyline();

			appendString("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("delete"+doc.getName()+"("+doc.getVariableName()+".getId())");
	        append(closeBlock());
	        emptyline();
	        
	        appendString("public void delete"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(doc.getName()+" old = "+getMapName(doc)+".remove(id)");
	        appendString("if (old!=null){");
	        increaseIdent();
	        appendStatement("fireObjectDeletedEvent(old)");
	        append(closeBlock());
	        append(closeBlock());
	        emptyline();

	        appendComment("Deletes multiple "+doc.getName()+" objects.");
	        appendString("public void delete"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : list){");
	        increaseIdent();
	        appendStatement("delete"+doc.getName()+"("+doc.getVariableName()+")");
	        append(closeBlock());
	        append(closeBlock());
	        emptyline();
	        
	        appendString("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(doc.getName()+" "+doc.getVariableName()+" = "+getMapName(doc)+".get(id)");
	        appendString("if ("+doc.getVariableName()+"==null)");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"No "+doc.getName()+" with id \"+id+\" found\")");
	        appendStatement("return "+doc.getVariableName());
	        append(closeBlock());
	        emptyline();
	        
	        
	        appendString("public "+doc.getName()+" import"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(getMapName(doc)+".put("+doc.getVariableName()+".getId(), "+doc.getVariableName()+")");
	        appendStatement("return "+doc.getVariableName());
	        append(closeBlock());
	        emptyline();

            appendString("public "+listDecl+" import"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = new Array"+listDecl+"()");
	        appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : list)");
	        appendIncreasedStatement("ret.add(import"+doc.getName()+"("+doc.getVariableName()+"))");
	        appendStatement("return ret");
	        append(closeBlock());
	        emptyline();

	        
	        appendString("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("String nextId = \"\"+"+getIdHolderName(doc)+".incrementAndGet();");
	        appendCommentLine("//Warning, following will work only with jdbc based classes for now...");
	        clazz.addImport(VOGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), doc));
	        appendStatement(VOGenerator.getDocumentImplName(doc)+" new"+StringUtils.capitalize(doc.getVariableName())+" = new "+VOGenerator.getDocumentImplName(doc)+"(nextId)");
	        appendStatement("new"+StringUtils.capitalize(doc.getVariableName())+".copyAttributesFrom("+doc.getVariableName()+")");
	        appendStatement(getMapName(doc)+".put("+"new"+StringUtils.capitalize(doc.getVariableName())+".getId(), "+"new"+StringUtils.capitalize(doc.getVariableName())+")");
	        appendStatement("fireObjectCreatedEvent("+"new"+StringUtils.capitalize(doc.getVariableName())+")");
	        appendStatement("return "+"new"+StringUtils.capitalize(doc.getVariableName()));
	        append(closeBlock());
	        emptyline();
	        
	        //
	        appendComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.");
	        appendString("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = new Array"+listDecl+"()");
	        appendString(getIterator(doc));
	        appendIncreasedStatement("ret.add(create"+doc.getName()+"("+doc.getVariableName()+"))");
	        
	        appendStatement("return ret");
	        append(closeBlock());
	        emptyline();

	        appendComment("Updates multiple new "+doc.getName()+" objects.\nReturns the updated versions.");
	        appendString("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = new Array"+listDecl+"()");
	        appendString(getIterator(doc));
	        appendIncreasedStatement("ret.add(update"+doc.getName()+"("+doc.getVariableName()+"))");
	        appendStatement("return ret");
	        append(closeBlock());
	        emptyline();

	        
	        appendString("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(doc.getName()+" oldVersion = "+getMapName(doc)+".put("+doc.getVariableName()+".getId(), "+doc.getVariableName()+")");
	        appendString("if (oldVersion!=null){");
	        appendIncreasedStatement("fireObjectUpdatedEvent(oldVersion, "+doc.getVariableName()+")");
	        append(closeBlock());
	        
	        appendStatement("return "+doc.getVariableName());
	        append(closeBlock());
	        emptyline();
	        
	        appendString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("throw new RuntimeException(\"Not yet implemented\")");
	        append(closeBlock());
	        emptyline();
	        
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
			emptyline();

			appendComment("Returns all "+doc.getName()+" objects, where property matches.");
	        appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(QueryProperty... property)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("throw new RuntimeException(\"Not yet implemented\")");
	        append(closeBlock());
	        emptyline();
	        
			appendComment("Returns all "+doc.getName()+" objects, where property matches, sorted");
			appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(property), sortType)");
	        append(closeBlock());
			emptyline();
			
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
	    	append(closeBlock());
	    	appendStatement("catch("+getExceptionName(module)+" e){");
	    	increaseIdent();
	    	appendStatement("throw new RuntimeException("+quote("export"+d.getMultiple()+"ToXML() failure: ")+" + e.getStackTrace())");
	    	append(closeBlock());
	    	append(closeBlock());
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
	    	append(closeBlock());
	    	appendStatement("catch("+getExceptionName(module)+" e){");
	    	increaseIdent();
	    	appendStatement("throw new RuntimeException("+quote("export"+d.getMultiple()+"ToXML() failure: ")+" + e.getStackTrace())");
	    	append(closeBlock());
	    	append(closeBlock());
	    	emptyline();
	    }
	    

	    appendString("public XMLNode exportToXML(){");
	    increaseIdent();
	    appendStatement("XMLNode ret = new XMLNode("+quote(module.getName())+")");
	    emptyline();
	    for (MetaDocument d : docs){
	    	appendStatement("ret.addChildNode(export"+d.getMultiple()+"ToXML())");
	    }
	    emptyline();
	    appendStatement("return ret");
	    append(closeBlock());
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
	    append(closeBlock());

	    
	    return clazz;
	}
	
	private String getMapName(MetaDocument doc){
		return doc.getName().toLowerCase()+"Map";
	}
	
	private String getIdHolderName(MetaDocument doc){
		return doc.getName().toLowerCase()+"IdHolder";
	}

	@Override public String getImplementationName(MetaModule m){
	    return getServiceName(m)+"FixtureImpl";
	}

	private String getIterator(MetaDocument doc){
		return "for ("+doc.getName()+" "+doc.getVariableName()+" : list)";
	}
	
	
	@Override protected String getPackageName(MetaModule module){
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(module)+".service.fixture";	
	}

	@Override protected void addAdditionalFactoryImports(GeneratedClass clazz, MetaModule module){
		clazz.addImport(GeneratorDataRegistry.getInstance().getContext().getServicePackageName(module)+"."+getInterfaceName(module));
	}

}
