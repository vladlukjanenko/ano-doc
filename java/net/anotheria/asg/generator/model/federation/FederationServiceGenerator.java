package net.anotheria.asg.generator.model.federation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.FederatedDocumentMapping;
import net.anotheria.asg.generator.meta.FederatedModuleDef;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaFederationModule;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.model.AbstractServiceGenerator;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.asg.generator.model.ServiceGenerator;

/**
 * Generator for the FederationService.
 * @author lrosenberg
 *
 */
public class FederationServiceGenerator extends AbstractServiceGenerator implements IGenerator{
	
	private final MetaProperty lastUpdate = new MetaProperty("lastUpdateTimestamp", MetaProperty.Type.LONG);

	
	/**
	 * Generates all artefacts.
	 */
	public List<FileEntry> generate(IGenerateable gmodule){
		
		MetaModule mod = (MetaModule)gmodule;
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ret.add(new FileEntry(generateFactory(mod)));
		ret.add(new FileEntry(generateImplementation(mod)));
		
		return ret;
	}
	
	
	public static final String FEDERATION_VARIABLE_PREFIX = "federated";
	
	/**
	 * Generates the service implementation for the module.
	 * @param moduleX
	 * @return
	 */
	private GeneratedClass generateImplementation(MetaModule moduleX){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateImplementation");
		
		MetaFederationModule module = (MetaFederationModule )moduleX;

	    String federationcomment = "Federated modules: ";
	    for (FederatedModuleDef fedDef : module.getFederatedModules()){
	    	federationcomment += fedDef.getName()+" as "+fedDef.getKey()+", ";
	    }
	    
	    clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The implementation of the "+getInterfaceName(module)+"as a federated service layer: "+federationcomment));
	    clazz.setPackageName(getPackageName(module));
	    
	    clazz.addImport("java.util.List");
	    clazz.addImport("java.util.ArrayList");
	    clazz.addImport("java.util.HashMap");
	    clazz.addImport("net.anotheria.util.sorter.SortType");
	    clazz.addImport("net.anotheria.util.sorter.StaticQuickSorter");
	    clazz.addImport("net.anotheria.util.StringUtils");
	    clazz.addImport("net.anotheria.util.slicer.Segment");
	    
	    Context context = GeneratorDataRegistry.getInstance().getContext();
	    
	    clazz.addImport(context.getServicePackageName(MetaModule.SHARED)+".BasicService");
	    
	    List<FederatedModuleDef> federatedModules = module.getFederatedModules();
	    Map<String,MetaModule> targetModules = new HashMap<String, MetaModule>();
	    for (FederatedModuleDef fedDef : federatedModules){
	    	MetaModule target = GeneratorDataRegistry.getInstance().getModule(fedDef.getName());
	    	if (target==null)
	    		throw new RuntimeException("No such module: "+fedDef.getName());
	    	clazz.addImport(ServiceGenerator.getInterfaceImport(target));
	    	clazz.addImport(ServiceGenerator.getExceptionImport(target));
	    	clazz.addImport(ServiceGenerator.getFactoryImport(target));
	    	targetModules.put(fedDef.getKey(), target);
	    }
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
	        clazz.addImport(FederationVOGenerator.getDocumentImport(context, doc));
	        clazz.addImport(DataFacadeGenerator.getDocumentFactoryImport(context, doc));

	        List<FederatedDocumentMapping> mappings = module.getMappingsForDocument(doc.getName());

	        //generate copy methods
	        for (FederatedDocumentMapping mapping : mappings){
	        	MetaDocument target = targetModules.get(mapping.getTargetKey()).getDocumentByName(mapping.getTargetDocument());
	        	clazz.addImport(DataFacadeGenerator.getDocumentImport(target));
	        }
	    }

	    
	    clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResultEntry");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
	    clazz.addImport("net.anotheria.util.xml.XMLNode");

	    clazz.setName(getImplementationName(module));
	    clazz.setParent("BasicService");
	    clazz.addInterface(getInterfaceName(module));

	    startClassBody();
	    appendStatement("private static "+getImplementationName(module)+" instance");
	    emptyline();
	    
	    String throwsClause = " throws "+ServiceGenerator.getExceptionName(module)+" ";
	    String throwClause = "throw new "+ServiceGenerator.getExceptionName(module)+"("+quote("Undelying service failed: ")+"+e.getMessage())";
	    
	    appendCommentLine("Federated services: ");
    	appendStatement("public static final char ID_DELIMITER = '-'");
	    for (FederatedModuleDef fedDef : federatedModules){
	    	MetaModule target = targetModules.get(fedDef.getKey());
	    	appendStatement(ServiceGenerator.getInterfaceName(target)+" "+FEDERATION_VARIABLE_PREFIX+fedDef.getKey());
	    	appendStatement("public static final String ID_PREFIX_"+fedDef.getKey()+" = "+quote(fedDef.getKey())+"+ID_DELIMITER");
	    	targetModules.put(fedDef.getKey(), target);
	    }
	    emptyline();
	    appendStatement("private HashMap<String, Object> federatedServiceMap");
	    
	    appendString("private "+getImplementationName(module)+"(){");
	    increaseIdent();
	    if (module.getListeners().size()>0){
	    	for (int i=0; i<module.getListeners().size(); i++){
	    		String listClassName = module.getListeners().get(i);
	    		appendStatement("addServiceListener(new "+listClassName+"())");
	    	}
	    	emptyline();
	    }
	    
	    //initialize federated servises;
	    appendStatement("federatedServiceMap = new HashMap<String, Object>("+federatedModules.size()+")");
	    for (FederatedModuleDef fedDef : federatedModules){
	    	MetaModule target = targetModules.get(fedDef.getKey());
	    	appendStatement(FEDERATION_VARIABLE_PREFIX+fedDef.getKey()+ " = "+ServiceGenerator.getFactoryName(target)+".create"+ServiceGenerator.getServiceName(target)+"()");
	    	targetModules.put(fedDef.getKey(), target);
	    	appendStatement("federatedServiceMap.put("+quote(fedDef.getKey())+", "+FEDERATION_VARIABLE_PREFIX+fedDef.getKey()+")");
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
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        List<FederatedDocumentMapping> mappings = module.getMappingsForDocument(doc.getName());

	        //System.out.println("Generating document: "+doc));
	        //generate copy methods
	        for (FederatedDocumentMapping mapping : mappings){
	        	//System.out.println(" mapping: "+mapping));
	        	MetaDocument target = targetModules.get(mapping.getTargetKey()).getDocumentByName(mapping.getTargetDocument());
	        	appendString("private "+doc.getName()+" copy("+target.getName()+" d){");
	        	increaseIdent();
	        	
	        	appendStatement(doc.getName()+" ret = "+DataFacadeGenerator.getDocumentFactoryName(doc)+".create"+doc.getName()+"(ID_PREFIX_"+mapping.getTargetKey()+"+d.getId())");
	        	//we assume that we always have identical properties, or at least all properties from federation doc have a corresponding property in target doc.
	        	List<MetaProperty> properties = doc.getProperties();
	        	for (MetaProperty p : properties){
	        		appendStatement("ret.set"+p.getAccesserName()+"(d.get"+p.getAccesserName()+"())");
	        	}
	        	
	        	//add lastupdate copy:
	        	appendStatement("(("+FederationVOGenerator.getDocumentImplName(doc)+")ret).set"+lastUpdate.getAccesserName()+"(d.get"+lastUpdate.getAccesserName()+"())");
	        	
	        	appendStatement("return ret");
	        	closeBlockNEW();
	        	emptyline();
	        	appendString("private List<"+doc.getName()+"> copy"+doc.getName()+"List"+mapping.getTargetKey()+"(List<"+target.getName()+"> list){");
	        	increaseIdent();
	        	appendStatement("List<"+doc.getName()+"> ret = new ArrayList<"+doc.getName()+">(list.size())");
	        	appendString("for ("+target.getName()+" d : list)");
	        	appendIncreasedStatement("ret.add(copy(d))");
	        	//we assume that we always have identical properties, or at least all properties from federation doc have a corresponding property in target doc.
	        	appendStatement("return ret");
	        	closeBlockNEW();
	        	emptyline();
	        }
	        //... end copy methods
	        
	        appendString("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" "+doc.getMultiple().toLowerCase()+" = new Array"+listDecl+"()");
	        for (FederatedDocumentMapping mapping : mappings){
	        	MetaDocument target = targetModules.get(mapping.getTargetKey()).getDocumentByName(mapping.getTargetDocument());
	        	appendString("try{");
	        	increaseIdent();
	        	appendStatement("List<"+target.getName()+"> source"+mapping.getTargetKey()+" = "+FEDERATION_VARIABLE_PREFIX+mapping.getTargetKey()+".get"+target.getMultiple()+"()");
	        	appendStatement(doc.getMultiple().toLowerCase()+".addAll(copy"+doc.getName()+"List"+mapping.getTargetKey()+"(source"+mapping.getTargetKey()+"))");
	        	decreaseIdent();
	        	appendString("}catch("+ServiceGenerator.getExceptionName(targetModules.get(mapping.getTargetKey()))+" e){");
	        	//TODO Add logging
	        	appendCommentLine("TODO Add logging?");
	        	appendIncreasedStatement(throwClause);
	        	appendString("}");
	        }
	        
	        //"+getModuleGetterCall(module)+".get"+doc.getMultiple()+"()"));
	        appendStatement("return "+doc.getMultiple().toLowerCase());
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
	        closeBlockNEW();
	        emptyline();
	        
	        appendString("public void delete"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("throw new RuntimeException(\"not implemented.\")");
	        closeBlockNEW();
	        emptyline();
	        
	        //delete multiple
	        appendComment("Deletes multiple "+doc.getName()+" objects.");
	        appendStatement("public void delete"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("throw new RuntimeException("+quote("Not yet implemented")+")");
	        closeBlockNEW();
	        emptyline();
	        
	        appendString("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("String tokens[] = StringUtils.tokenize(id, ID_DELIMITER)");
	        
	        

	        
	        for(FederatedDocumentMapping mapping : mappings){
	        	appendString("if (tokens[0].equals("+quote(mapping.getTargetKey())+")){");
	        	increaseIdent();
	        	appendString("try{");
	        	appendIncreasedStatement("return copy("+FEDERATION_VARIABLE_PREFIX+mapping.getTargetKey()+".get"+mapping.getTargetDocument()+"(tokens[1]))");
	        	appendString("}catch("+ServiceGenerator.getExceptionName(targetModules.get(mapping.getTargetKey()))+" e){");
	        	//TODO Add logging
	        	appendCommentLine("TODO Add logging?");
	        	appendIncreasedStatement(throwClause);
	        	appendString("}");
	        	decreaseIdent();
	        	appendString("}");
	        }
	        appendStatement("throw new RuntimeException("+quote("Unknown federated key: ")+"+tokens[0]+"+quote(" in ")+"+id)");
	        closeBlockNEW();
	        emptyline();
	        
	        appendString("public "+doc.getName()+" import"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("throw new RuntimeException(\"no import in federated services.\")");
	        closeBlockNEW();
	        emptyline();

            appendString("public "+listDecl+" import"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("throw new RuntimeException(\"no import in federated services.\")");
	        closeBlockNEW();
	        emptyline();


	        appendString("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("throw new AssertionError(\"not implemented.\")");
	        closeBlockNEW();
	        emptyline();
	        
	        
	        //create multiple
	        appendComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.");
	        appendStatement("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("throw new AssertionError(\"not implemented.\")");
	        closeBlockNEW();
	        emptyline();

	        
	        //update multiple
	        appendComment("Updates multiple new "+doc.getName()+" objects.\nReturns the updated versions.");
	        appendStatement("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("throw new RuntimeException("+quote("Not yet implemented")+")");
	        closeBlockNEW();
	        emptyline();

	        
	        appendString("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("throw new AssertionError(\"not implemented.\")");
	        closeBlockNEW();
	        emptyline();
	        
	        appendString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement(listDecl+" "+doc.getMultiple().toLowerCase()+" = new Array"+listDecl+"()");
	        for (FederatedDocumentMapping mapping : mappings){
	        	MetaDocument target = targetModules.get(mapping.getTargetKey()).getDocumentByName(mapping.getTargetDocument());
	        	appendString("try{");
	        	increaseIdent();
	        	appendStatement("List<"+target.getName()+"> source"+mapping.getTargetKey()+" = "+FEDERATION_VARIABLE_PREFIX+mapping.getTargetKey()+".get"+target.getMultiple()+"ByProperty(propertyName, value)");
	        	appendStatement(doc.getMultiple().toLowerCase()+".addAll(copy"+doc.getName()+"List"+mapping.getTargetKey()+"(source"+mapping.getTargetKey()+"))");
	        	decreaseIdent();
	        	appendString("}catch("+ServiceGenerator.getExceptionName(targetModules.get(mapping.getTargetKey()))+" e){");
	        	//TODO Add logging
	        	appendCommentLine("TODO Add logging?");
	        	appendIncreasedStatement(throwClause);
	        	appendString("}");
	        }
	        
	        //"+getModuleGetterCall(module)+".get"+doc.getMultiple()+"()"));
	        appendStatement("return "+doc.getMultiple().toLowerCase());
	        closeBlockNEW();
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
	        appendStatement("throw new RuntimeException(\"Not yet implemented\")");
	        closeBlockNEW();
	        emptyline();
	        
			appendComment("Returns all "+doc.getName()+" objects, where property matches, sorted");
			appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)"+throwsClause+"{");
	        increaseIdent();
	        appendStatement("throw new RuntimeException(\"Not yet implemented\")");
	        closeBlockNEW();
			emptyline();
			
			// get elements COUNT
			appendComment("Returns " + doc.getName() + " objects count.");
			appendString("public int get" + doc.getMultiple() + "Count()" + throwsClause + "{");
			increaseIdent();
			appendString("int pCount = 0;");
			for (FederatedDocumentMapping mapping : mappings) {
				MetaDocument target = targetModules.get(mapping.getTargetKey()).getDocumentByName(mapping.getTargetDocument());
				appendString("try {");
				increaseIdent();
				appendStatement("pCount = pCount + " + FEDERATION_VARIABLE_PREFIX + mapping.getTargetKey() + ".get" + target.getMultiple()
						+ "().size()");
				decreaseIdent();
				appendString("} catch (" + ServiceGenerator.getExceptionName(targetModules.get(mapping.getTargetKey())) + " e) {");
				appendIncreasedStatement(throwClause);
				appendString("}");
			}
			appendStatement("return pCount");
			closeBlockNEW();
			emptyline();
			// end get elements COUNT

			// get elements Segment
			// TODO need make implementation
			appendComment("Returns " + doc.getName() + " objects segment.");
			appendStatement("public " + listDecl + " get" + doc.getMultiple() + "(Segment aSegment)" + throwsClause + "{");
			increaseIdent();
			appendStatement("throw new RuntimeException(\"Not yet implemented\")");
			closeBlockNEW();
			emptyline();
			// end get elements Segment

			// get elements Segment with FILTER
			// TODO need make implementation
			appendComment("Returns " + doc.getName() + " objects segment, where property matched.");
			appendStatement("public " + listDecl + " get" + doc.getMultiple() + "ByProperty(Segment aSegment, QueryProperty... aProperty)"
					+ throwsClause + "{");
			increaseIdent();
			appendStatement("throw new RuntimeException(\"Not yet implemented\")");
			closeBlockNEW();
			emptyline();
			// end get elements Segment with FILTER

			// get elements Segment with SORTING, FILTER
			// TODO need make implementation
			appendComment("Returns " + doc.getName() + " objects segment, where property matched, sorted.");
			appendStatement("public " + listDecl + " get" + doc.getMultiple()
					+ "ByProperty(Segment aSegment, SortType aSortType, QueryProperty... aProperty)" + throwsClause + "{");
			increaseIdent();
			appendStatement("throw new RuntimeException(\"Not yet implemented\")");
			closeBlockNEW();
			emptyline();
			// end get elements Segment with SORTING, FILTER
			appendStatement("public XMLNode export" + doc.getMultiple() + "ToXML(List<" + doc.getName() + "> list" + doc.getMultiple() + "){");
			increaseIdent();
			appendStatement("return new XMLNode(" + quote("unimplemented_federated_export_" + module.getName()) + ")");
			closeBlockNEW();
			emptyline();

			emptyline();
			appendString("public XMLNode export" + doc.getMultiple() + "ToXML(String[] languages,List<" + doc.getName() + "> list" + doc.getMultiple() +")" + throwsClause + "{");
			increaseIdent();
			appendStatement("return new XMLNode(" + quote("unimplemented_federated_export_" + module.getName()) + ")");
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
	    
	    //generate export function
	    emptyline();
	    appendString("public XMLNode exportToXML()"+throwsClause+"{");
	    increaseIdent();
        appendStatement("return new XMLNode("+quote("unimplemented_federated_export_"+module.getName())+")");
	    closeBlockNEW();
	    
	    emptyline();
	    appendString("public XMLNode exportToXML(String[] languages)"+throwsClause+"{");
	    increaseIdent();
        appendStatement("return new XMLNode("+quote("unimplemented_federated_export_"+module.getName())+")");
	    closeBlockNEW();
	    
	    return clazz;
	}
	
	@Override protected String getMoskitoSubsystem(){
		return super.getMoskitoSubsystem()+"-fed";
	}

}
