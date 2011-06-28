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
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.model.AbstractServiceGenerator;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.asg.generator.model.db.VOGenerator;
import net.anotheria.asg.generator.model.docs.DocumentGenerator;
import net.anotheria.asg.service.BaseFixtureService;
import net.anotheria.asg.service.IFixtureService;
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
	 * Generates the implementation.
	 * @param module the metamodule to generate
	 * @return generated implementation
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
	    clazz.addImport("net.anotheria.util.slicer.Segment");
	    clazz.addImport("net.anotheria.util.slicer.Slicer");

	    clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResultEntry");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
	    
	    clazz.addImport("net.anotheria.util.xml.XMLNode");
	    clazz.addImport("net.anotheria.util.xml.XMLAttribute");
	    
	    clazz.addImport(ServiceGenerator.getInterfaceImport(module));
	    clazz.addImport(ServiceGenerator.getExceptionImport(module));

	    clazz.setName(getImplementationName(module));
	    clazz.setParent(BaseFixtureService.class);
	    clazz.addInterface(getInterfaceName(module));
	    clazz.addInterface(IFixtureService.class);
	    
	    startClassBody();
	    appendStatement("private static "+getImplementationName(module)+" instance");
	    emptyline();
	    
	    List<MetaDocument> docs = module.getDocuments();
	    clazz.addImport(ConcurrentHashMap.class);
	    clazz.addImport(Map.class);
	    clazz.addImport(AtomicInteger.class);
	    clazz.addImport(ArrayList.class);
	    
	    for (MetaDocument doc : docs){
	    	appendStatement("private Map<String, "+doc.getName()+"> "+getMapName(doc));
	    	appendStatement("private AtomicInteger "+getIdHolderName(doc));
	    	emptyline();
	    }
	    
	    appendString("private "+getImplementationName(module)+"(){");
	    increaseIdent();
	    appendStatement("reset()");
	    //appendStatement("pService = "+JDBCPersistenceServiceGenerator.getFactoryName(module)+".create"+JDBCPersistenceServiceGenerator.getServiceName(module)+"()");
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
	    
	    appendString("@Override public void reset(){");
	    increaseIdent();
	    for (MetaDocument doc : docs){
	    	appendStatement(getMapName(doc)+" = new ConcurrentHashMap<String, "+doc.getName()+">()" );
	    	appendStatement(getIdHolderName(doc) +" = new AtomicInteger(0)");
	    	emptyline();
	    }
	    closeBlockNEW();
	    emptyline();
	    

	    String throwsClause = " throws "+getExceptionName(module)+" ";
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";

	        clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
	        clazz.addImport(DataFacadeGenerator.getXMLHelperImport(context, doc));

	        appendString("@Override public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = new Array"+listDecl+"()");
	        appendStatement("ret.addAll("+getMapName(doc)+".values())");
	        appendStatement("return ret");
	        closeBlockNEW();
	        emptyline();
	        
			appendString("@Override public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType)"+throwsClause+"{");
			increaseIdent();
			appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"(), sortType)");
			closeBlockNEW();
			emptyline();

			appendString("@Override public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("delete"+doc.getName()+"("+doc.getVariableName()+".getId())");
	        closeBlockNEW();
	        emptyline();
	        
	        appendString("@Override public void delete"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(doc.getName()+" old = "+getMapName(doc)+".remove(id)");
	        appendString("if (old!=null){");
	        increaseIdent();
	        appendStatement("fireObjectDeletedEvent(old)");
	        closeBlockNEW();
	        closeBlockNEW();
	        emptyline();

	        appendComment("Deletes multiple "+doc.getName()+" objects.");
	        appendString("@Override public void delete"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : list){");
	        increaseIdent();
	        appendStatement("delete"+doc.getName()+"("+doc.getVariableName()+")");
	        closeBlockNEW();
	        closeBlockNEW();
	        emptyline();
	        
	        appendString("@Override public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(doc.getName()+" "+doc.getVariableName()+" = "+getMapName(doc)+".get(id)");
	        appendString("if ("+doc.getVariableName()+"==null)");
	        appendIncreasedStatement("throw new "+getExceptionName(module)+"(\"No "+doc.getName()+" with id \"+id+\" found\")");
	        appendStatement("return "+doc.getVariableName());
	        closeBlockNEW();
	        emptyline();
	        
	        
	        appendString("@Override public "+doc.getName()+" import"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(getMapName(doc)+".put("+doc.getVariableName()+".getId(), "+doc.getVariableName()+")");
	        appendStatement("return "+doc.getVariableName());
	        closeBlockNEW();
	        emptyline();

            appendString("@Override public "+listDecl+" import"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = new Array"+listDecl+"()");
	        appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : list)");
	        appendIncreasedStatement("ret.add(import"+doc.getName()+"("+doc.getVariableName()+"))");

	        appendStatement("return ret");
	        closeBlockNEW();
	        emptyline();

	        
	        appendString("@Override public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("String nextId = \"\"+"+getIdHolderName(doc)+".incrementAndGet();");
	        if (module.getStorageType()==StorageType.DB){
	        	appendCommentLine("//DB Specific code");
	        	clazz.addImport(VOGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), doc));
	        	appendStatement(VOGenerator.getDocumentImplName(doc)+" new"+StringUtils.capitalize(doc.getVariableName())+" = new "+VOGenerator.getDocumentImplName(doc)+"(nextId)");
		        appendStatement("new"+StringUtils.capitalize(doc.getVariableName())+".copyAttributesFrom("+doc.getVariableName()+")");
	        }
	        if (module.getStorageType()==StorageType.CMS){
	        	appendCommentLine("//CMS Specific code");
	        	clazz.addImport(DocumentGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), doc));
	        	appendStatement(DocumentGenerator.getDocumentImplName(doc)+" new"+StringUtils.capitalize(doc.getVariableName())+" = ("+DocumentGenerator.getDocumentImplName(doc)+")"+doc.getVariableName());
	        	appendStatement("new"+StringUtils.capitalize(doc.getVariableName())+".renameTo(nextId)");
	        }
	        appendStatement(getMapName(doc)+".put("+"new"+StringUtils.capitalize(doc.getVariableName())+".getId(), "+"new"+StringUtils.capitalize(doc.getVariableName())+")");
	        appendStatement("fireObjectCreatedEvent("+"new"+StringUtils.capitalize(doc.getVariableName())+")");
	        appendStatement("return "+"new"+StringUtils.capitalize(doc.getVariableName()));
	        closeBlockNEW();
	        emptyline();
	        
	        //
	        appendComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.");
	        appendString("@Override public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = new Array"+listDecl+"()");
	        appendString(getIterator(doc));
	        appendIncreasedStatement("ret.add(create"+doc.getName()+"("+doc.getVariableName()+"))");
	        
	        appendStatement("return ret");
	        closeBlockNEW();
	        emptyline();

	        appendComment("Updates multiple new "+doc.getName()+" objects.\nReturns the updated versions.");
	        appendString("@Override public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" ret = new Array"+listDecl+"()");
	        appendString(getIterator(doc));
	        appendIncreasedStatement("ret.add(update"+doc.getName()+"("+doc.getVariableName()+"))");
	        appendStatement("return ret");
	        closeBlockNEW();
	        emptyline();

	        
	        appendString("@Override public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(doc.getName()+" oldVersion = "+getMapName(doc)+".put("+doc.getVariableName()+".getId(), "+doc.getVariableName()+")");
	        appendString("if (oldVersion!=null){");
	        increaseIdent();
	        appendIncreasedStatement("fireObjectUpdatedEvent(oldVersion, "+doc.getVariableName()+")");
	        closeBlockNEW();
	        
	        appendStatement("return "+doc.getVariableName());
	        closeBlockNEW();
	        emptyline();
	        
	        appendString("@Override public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" list = get"+doc.getMultiple()+"()");
	        appendStatement(listDecl+" ret = new Array"+listDecl+"()");
	        appendString(getIterator(doc)+"{");
	        increaseIdent();
	        appendStatement("Object propertyValue = "+doc.getVariableName()+".getPropertyValue(propertyName)");
	        appendString("if (propertyValue!=null && propertyValue.equals(value))");
	        appendIncreasedStatement("ret.add("+doc.getVariableName()+")");
	        
	        closeBlockNEW();
	        appendStatement("return ret");
	        closeBlockNEW();
	        emptyline();
	        
			appendString("@Override public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)"+throwsClause+"{");
			increaseIdent();
			appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(propertyName, value), sortType)");
			closeBlockNEW();
			
			appendComment("Executes a query on "+doc.getMultiple());
			appendString("@Override public QueryResult executeQueryOn"+doc.getMultiple()+"(DocumentQuery query)"+throwsClause+"{");
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
	        appendStatement("@Override public "+listDecl+" get"+doc.getMultiple()+"ByProperty(QueryProperty... properties)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" list = get"+doc.getMultiple()+"()");
	        appendString("if (properties==null || properties.length==0)");
	        appendIncreasedStatement("return list");
	        appendStatement(listDecl+" ret = new Array"+listDecl+"()");
	        appendString(getIterator(doc)+"{");
	        increaseIdent();
	        appendStatement("boolean mayPass = true");
	        appendString("for (QueryProperty p : properties){");
	        increaseIdent();
	        appendStatement("Object propertyValue = "+doc.getVariableName()+".getPropertyValue(p.getName())");
	        appendString("if (mayPass && (propertyValue==null || (!(p.doesMatch(propertyValue)))))");
	        appendIncreasedStatement("mayPass = false");
	        closeBlockNEW();
	        appendString("if (mayPass)");
	        appendIncreasedStatement("ret.add("+doc.getVariableName()+")");
	        closeBlockNEW();
	        appendStatement("return ret");
	        closeBlockNEW();
	        emptyline();
	        
			appendComment("Returns all "+doc.getName()+" objects, where property matches, sorted");
			appendStatement("@Override public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(property), sortType)");
	        closeBlockNEW();
			emptyline();
			
			// get elements COUNT
			appendComment("Returns " + doc.getName() + " objects count.");
			appendString("@Override public int get" + doc.getMultiple() + "Count()" + throwsClause + "{");
			increaseIdent();
			appendStatement("return " + getMapName(doc) + ".values().size()");
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
			appendString("public " + listDecl + " get" + doc.getMultiple() + "ByProperty(Segment aSegment, QueryProperty... property) {");
			increaseIdent();
			appendStatement("int pLimit = aSegment.getElementsPerSlice()");
			appendStatement("int pOffset = aSegment.getSliceNumber() * aSegment.getElementsPerSlice() - aSegment.getElementsPerSlice()");
			appendStatement(listDecl + " ret = new ArrayList<" + doc.getName() + ">()");
			appendStatement(listDecl + " src = new ArrayList<" + doc.getName() + ">()");
			appendStatement("src.addAll(" + getMapName(doc) + ".values())");
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
					+ "ByProperty(Segment aSegment, SortType aSortType, QueryProperty... aProperty){");
			increaseIdent();
			appendStatement("return StaticQuickSorter.sort(get" + doc.getMultiple() + "ByProperty(aSegment, aProperty), aSortType)");
			closeBlockNEW();
			emptyline();
			// end get elements Segment with SORTING, FILTER
	    }
	    
		boolean containsAnyMultilingualDocs = false;
	    
	    
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
	    	appendStatement("throw new RuntimeException("+quote("export"+d.getMultiple()+"ToXML() failure: ")+" , e)");
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
	    	appendStatement("throw new RuntimeException("+quote("export"+d.getMultiple()+"ToXML() failure: ")+" , e)");
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

	    	if (GeneratorDataRegistry.hasLanguageCopyMethods(d)){
				containsAnyMultilingualDocs = true;
				appendCommentLine("This method is not very fast, since it makes an update (eff. save) after each doc.");
				appendString("public void copyMultilingualAttributesInAll"+d.getMultiple()+"(String sourceLanguage, String targetLanguage){");
				increaseIdent();
				appendStatement("throw new AssertionError(\"Not implemented\")");
				closeBlockNEW();
				emptyline();

			}


	    }

	    if (containsAnyMultilingualDocs){
			appendComment("Copies all multilingual fields from sourceLanguage to targetLanguage in all data objects (documents, vo) which are part of this module and managed by this service");
			appendString("public void copyMultilingualAttributesInAllObjects(String sourceLanguage, String targetLanguage){");
			increaseIdent();
			for (MetaDocument doc : docs){
				if (GeneratorDataRegistry.hasLanguageCopyMethods(doc))
					appendStatement("copyMultilingualAttributesInAll"+doc.getMultiple()+"(sourceLanguage, targetLanguage)");
			}
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
	    
	    appendString("@Override public XMLNode exportToXML(){");
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
	    
	    if (containsAnyMultilingualDocs && GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported()){
		    appendString("@Override public XMLNode exportToXML(String[] languages){");
		    increaseIdent();
		    appendStatement("XMLNode ret = new XMLNode("+quote(module.getName())+")");
		    emptyline();
		    for (MetaDocument d : docs){
		    	appendStatement("ret.addChildNode(export"+d.getMultiple()+"ToXML(languages))");
		    }
		    emptyline();
		    appendStatement("return ret");
		    closeBlockNEW();
	    }
	    
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
		clazz.addImport(IFixtureService.class);
	}

	protected String getSupportedInterfacesList(MetaModule module){
		return super.getSupportedInterfacesList(module)+", IFixtureService.class";
	}

	@Override protected String getMoskitoSubsystem(){
		return super.getMoskitoSubsystem()+"-fixture";
	}

}
