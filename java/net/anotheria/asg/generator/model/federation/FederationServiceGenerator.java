package net.anotheria.asg.generator.model.federation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.FederatedDocumentMapping;
import net.anotheria.asg.generator.meta.FederatedModuleDef;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaFederationModule;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.asg.generator.model.ServiceGenerator;

public class FederationServiceGenerator extends AbstractGenerator implements IGenerator{
	
	private Context context;
	MetaProperty lastUpdate = new MetaProperty("lastUpdateTimestamp", "long");

	
	public List<FileEntry> generate(IGenerateable gmodule, Context context){
		
		MetaModule mod = (MetaModule)gmodule;
		
		this.context = context;
		String packageName = getPackageName(mod);
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ret.add(new FileEntry(FileEntry.package2path(packageName), getFactoryName(mod), generateFactory(mod)));
		ret.add(new FileEntry(FileEntry.package2path(packageName), getImplementationName(mod), generateImplementation(mod)));
		
		return ret;
	}
	
	private String getPackageName(MetaModule module){
		return context.getServicePackageName(module);
	}
	
	public static final String FEDERATION_VARIABLE_PREFIX = "federated";
	
	private String generateImplementation(MetaModule moduleX){
		
		MetaFederationModule module = (MetaFederationModule )moduleX;
	    String ret = "";

	    String federationcomment = "Federated modules: ";
	    for (FederatedModuleDef fedDef : module.getFederatedModules()){
	    	federationcomment += fedDef.getName()+" as "+fedDef.getKey()+", ";
	    }
		ret += CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The implementation of the "+getInterfaceName(module)+"as a federated service layer: "+federationcomment);

	    ret += writeStatement("package "+getPackageName(module));
	    ret += emptyline();
	    ret += writeImport("java.util.List");
	    ret += writeImport("java.util.ArrayList");
	    ret += writeImport("java.util.HashMap");
		ret += writeImport("net.anotheria.util.sorter.SortType");
		ret += writeImport("net.anotheria.util.Date");
		ret += writeImport("net.anotheria.util.StringUtils");
	    ret += writeImport(context.getServicePackageName(MetaModule.SHARED)+".BasicService");
	    
	    List<FederatedModuleDef> federatedModules = module.getFederatedModules();
	    Map<String,MetaModule> targetModules = new HashMap<String, MetaModule>();
	    for (FederatedModuleDef fedDef : federatedModules){
	    	MetaModule target = GeneratorDataRegistry.getInstance().getModule(fedDef.getName());
	    	ret += writeImport(ServiceGenerator.getInterfaceImport(context, target));
	    	ret += writeImport(ServiceGenerator.getExceptionImport(context, target));
	    	ret += writeImport(ServiceGenerator.getFactoryImport(context, target));
	    	targetModules.put(fedDef.getKey(), target);
	    }
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
	        ret += writeImport(FederationVOGenerator.getDocumentImport(context, doc));
	        ret += writeImport(DataFacadeGenerator.getDocumentFactoryImport(context, doc));

	        List<FederatedDocumentMapping> mappings = module.getMappingsForDocument(doc.getName());

	        //generate copy methods
	        for (FederatedDocumentMapping mapping : mappings){
	        	MetaDocument target = targetModules.get(mapping.getTargetKey()).getDocumentByName(mapping.getTargetDocument());
	        	ret += writeImport(DataFacadeGenerator.getDocumentImport(context, target));
	        }
	    }

	    
	    ret += emptyline();
	    ret += writeImport("net.anotheria.asg.util.listener.IServiceListener");
	    ret += writeImport("net.anotheria.anodoc.query2.DocumentQuery");
	    ret += writeImport("net.anotheria.anodoc.query2.QueryResult");
	    ret += writeImport("net.anotheria.anodoc.query2.QueryResultEntry");
	    ret += writeImport("net.anotheria.anodoc.query2.QueryProperty");

	    ret += emptyline();
	    ret += writeImport("org.jdom.Element");
	    ret += emptyline();
	    
	    ret += writeString("public class "+getImplementationName(module)+" extends BasicService implements "+getInterfaceName(module)+" {");
	    increaseIdent();
	    ret += writeStatement("private static "+getImplementationName(module)+" instance");
	    ret += emptyline();
	    
	    String throwsClause = " throws "+ServiceGenerator.getExceptionName(module)+" ";
	    String throwClause = "throw new "+ServiceGenerator.getExceptionName(module)+"("+quote("Undelying service failed: ")+"+e.getMessage())";
	    
	    ret += writeCommentLine("Federated services: ");
    	ret += writeStatement("public static final char ID_DELIMITER = '-'");
	    for (FederatedModuleDef fedDef : federatedModules){
	    	MetaModule target = targetModules.get(fedDef.getKey());
	    	ret += writeStatement(ServiceGenerator.getInterfaceName(target)+" "+FEDERATION_VARIABLE_PREFIX+fedDef.getKey());
	    	ret += writeStatement("public static final String ID_PREFIX_"+fedDef.getKey()+" = "+quote(fedDef.getKey())+"+ID_DELIMITER");
	    	targetModules.put(fedDef.getKey(), target);
	    }
	    ret += emptyline();
	    ret += writeStatement("private HashMap<String, Object> federatedServiceMap");
	    
	    ret += writeString("private "+getImplementationName(module)+"(){");
	    increaseIdent();
	    if (module.getListeners().size()>0){
	    	for (int i=0; i<module.getListeners().size(); i++){
	    		String listClassName = (String)module.getListeners().get(i);
	    		ret += writeStatement("addServiceListener(new "+listClassName+"())");
	    	}
	    	ret += emptyline();
	    }
	    
	    //initialize federated servises;
	    ret += writeStatement("federatedServiceMap = new HashMap<String, Object>("+federatedModules.size()+")");
	    for (FederatedModuleDef fedDef : federatedModules){
	    	MetaModule target = targetModules.get(fedDef.getKey());
	    	ret += writeStatement(FEDERATION_VARIABLE_PREFIX+fedDef.getKey()+ " = "+ServiceGenerator.getFactoryName(target)+".create"+ServiceGenerator.getServiceName(target)+"()");
	    	targetModules.put(fedDef.getKey(), target);
	    	ret += writeStatement("federatedServiceMap.put("+quote(fedDef.getKey())+", "+FEDERATION_VARIABLE_PREFIX+fedDef.getKey()+")");
	    }
	    
	    ret += closeBlock();
	    ret += emptyline();
	    
	    ret += writeString("static final "+getImplementationName(module)+" getInstance(){");
	    increaseIdent();
	    ret += writeString("if (instance==null){");
	    increaseIdent();
	    ret += writeStatement("instance = new "+getImplementationName(module)+"()");
	    ret += closeBlock();
	    ret += writeStatement("return instance");
	    ret += closeBlock();
	    ret += emptyline();
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        List<FederatedDocumentMapping> mappings = module.getMappingsForDocument(doc.getName());

	        //System.out.println("Generating document: "+doc);
	        //generate copy methods
	        for (FederatedDocumentMapping mapping : mappings){
	        	//System.out.println(" mapping: "+mapping);
	        	MetaDocument target = targetModules.get(mapping.getTargetKey()).getDocumentByName(mapping.getTargetDocument());
	        	ret += writeString("private "+doc.getName()+" copy("+target.getName()+" d){");
	        	increaseIdent();
	        	
	        	ret += writeStatement(doc.getName()+" ret = "+DataFacadeGenerator.getDocumentFactoryName(doc)+".create"+doc.getName()+"(ID_PREFIX_"+mapping.getTargetKey()+"+d.getId())");
	        	//we assume that we always have identical properties, or at least all properties from federation doc have a corresponding property in target doc.
	        	List<MetaProperty> properties = doc.getProperties();
	        	for (MetaProperty p : properties){
	        		ret += writeStatement("ret.set"+p.getAccesserName()+"(d.get"+p.getAccesserName()+"())");
	        	}
	        	
	        	//add lastupdate copy:
	        	ret += writeStatement("(("+FederationVOGenerator.getDocumentImplName(doc)+")ret).set"+lastUpdate.getAccesserName()+"(d.get"+lastUpdate.getAccesserName()+"())");
	        	
	        	ret += writeStatement("return ret");
	        	ret += closeBlock();
	        	ret += emptyline();
	        	ret += writeString("private List<"+doc.getName()+"> copy"+doc.getName()+"List"+mapping.getTargetKey()+"(List<"+target.getName()+"> list){");
	        	increaseIdent();
	        	ret += writeStatement("List<"+doc.getName()+"> ret = new ArrayList<"+doc.getName()+">(list.size())");
	        	ret += writeString("for ("+target.getName()+" d : list)");
	        	ret += writeIncreasedStatement("ret.add(copy(d))");
	        	//we assume that we always have identical properties, or at least all properties from federation doc have a corresponding property in target doc.
	        	ret += writeStatement("return ret");
	        	ret += closeBlock();
	        	ret += emptyline();
	        }
	        //... end copy methods
	        
	        ret += writeString("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause+"{");
	        increaseIdent();
	        ret += writeStatement(listDecl+" "+doc.getMultiple().toLowerCase()+" = new Array"+listDecl+"();");
	        for (FederatedDocumentMapping mapping : mappings){
	        	MetaDocument target = targetModules.get(mapping.getTargetKey()).getDocumentByName(mapping.getTargetDocument());
	        	ret += writeString("try{");
	        	increaseIdent();
	        	ret += writeStatement("List<"+target.getName()+"> source"+mapping.getTargetKey()+" = "+FEDERATION_VARIABLE_PREFIX+mapping.getTargetKey()+".get"+target.getMultiple()+"()");
	        	ret += writeStatement(doc.getMultiple().toLowerCase()+".addAll(copy"+doc.getName()+"List"+mapping.getTargetKey()+"(source"+mapping.getTargetKey()+"))");
	        	decreaseIdent();
	        	ret += writeString("}catch("+ServiceGenerator.getExceptionName(targetModules.get(mapping.getTargetKey()))+" e){");
	        	//TODO Add logging
	        	ret += writeCommentLine("TODO Add logging?");
	        	ret += writeIncreasedStatement(throwClause);
	        	ret += writeString("}");
	        }
	        
	        //"+getModuleGetterCall(module)+".get"+doc.getMultiple()+"()");
	        ret += writeStatement("return "+doc.getMultiple().toLowerCase());
	        ret += closeBlock();
	        ret += emptyline();
	        
			ret += writeString("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType)"+throwsClause+"{");
			increaseIdent();
			ret += writeStatement("return sorter.sort(get"+doc.getMultiple()+"(), sortType)");
			ret += closeBlock();
			ret += emptyline();

	        ret += writeString("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        ret += writeStatement("delete"+doc.getName()+"("+doc.getVariableName()+".getId())");
	        ret += closeBlock();
	        ret += emptyline();
	        
	        ret += writeString("public void delete"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        ret += writeStatement("throw new RuntimeException(\"not implemented.\")");
	        ret += closeBlock();
	        ret += emptyline();

	        ret += writeString("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause+"{");
	        increaseIdent();
	        ret += writeStatement("String tokens[] = StringUtils.tokenize(id, ID_DELIMITER)");
	        
	        

	        
	        for(FederatedDocumentMapping mapping : mappings){
	        	ret += writeString("if (tokens[0].equals("+quote(mapping.getTargetKey())+")){");
	        	increaseIdent();
	        	ret += writeString("try{");
	        	ret += writeIncreasedStatement("return copy("+FEDERATION_VARIABLE_PREFIX+mapping.getTargetKey()+".get"+mapping.getTargetDocument()+"(tokens[1]))");
	        	ret += writeString("}catch("+ServiceGenerator.getExceptionName(targetModules.get(mapping.getTargetKey()))+" e){");
	        	//TODO Add logging
	        	ret += writeCommentLine("TODO Add logging?");
	        	ret += writeIncreasedStatement(throwClause);
	        	ret += writeString("}");
	        	decreaseIdent();
	        	ret += writeString("}");
	        }
	        ret += writeStatement("throw new RuntimeException("+quote("Unknown federated key: ")+"+tokens[0]+"+quote(" in ")+"+id)");
	        ret += closeBlock();
	        ret += emptyline();
	        
	        ret += writeString("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        ret += writeStatement("if (1==1) throw new RuntimeException(\"not implemented.\")");
	        
	        ret += writeString("if (hasServiceListeners()){");
	        increaseIdent();
	        ret += writeStatement("List myListeners = getServiceListeners()");
	        ret += writeString("for (int i=0; i<myListeners.size(); i++)");
	        ret += writeIncreasedStatement("((IServiceListener)myListeners.get(i)).documentCreated("+doc.getVariableName()+")");
	        ret += closeBlock();	
	        
	        
	        ret += writeStatement("return "+doc.getVariableName());
	        ret += closeBlock();
	        ret += emptyline();

	        ret += writeString("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{");
	        increaseIdent();
	        ret += writeStatement("if (1==1) throw new RuntimeException(\"not implemented.\")");
	        ret += writeStatement(doc.getName()+" oldVersion = null");
	        
	        ret += writeString("//if (hasServiceListeners())");
	        ret += writeIncreasedStatement("//oldVersion = module.get"+doc.getName()+"("+doc.getVariableName()+".getId())");
	        
	        
	        ret += writeString("if (hasServiceListeners()){");
	        increaseIdent();
	        ret += writeStatement("List myListeners = getServiceListeners()");
	        ret += writeString("for (int i=0; i<myListeners.size(); i++)");
	        ret += writeIncreasedStatement("((IServiceListener)myListeners.get(i)).documentUpdated(oldVersion, "+doc.getVariableName()+")");
	        ret += closeBlock();
	        
	        ret += writeStatement("return "+doc.getVariableName());
	        ret += closeBlock();
	        ret += emptyline();
	        
	        ret += writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)"+throwsClause+"{");
	        increaseIdent();
	        ret += writeStatement(listDecl+" "+doc.getMultiple().toLowerCase()+" = new Array"+listDecl+"();");
	        for (FederatedDocumentMapping mapping : mappings){
	        	MetaDocument target = targetModules.get(mapping.getTargetKey()).getDocumentByName(mapping.getTargetDocument());
	        	ret += writeString("try{");
	        	increaseIdent();
	        	ret += writeStatement("List<"+target.getName()+"> source"+mapping.getTargetKey()+" = "+FEDERATION_VARIABLE_PREFIX+mapping.getTargetKey()+".get"+target.getMultiple()+"ByProperty(propertyName, value)");
	        	ret += writeStatement(doc.getMultiple().toLowerCase()+".addAll(copy"+doc.getName()+"List"+mapping.getTargetKey()+"(source"+mapping.getTargetKey()+"))");
	        	decreaseIdent();
	        	ret += writeString("}catch("+ServiceGenerator.getExceptionName(targetModules.get(mapping.getTargetKey()))+" e){");
	        	//TODO Add logging
	        	ret += writeCommentLine("TODO Add logging?");
	        	ret += writeIncreasedStatement(throwClause);
	        	ret += writeString("}");
	        }
	        
	        //"+getModuleGetterCall(module)+".get"+doc.getMultiple()+"()");
	        ret += writeStatement("return "+doc.getMultiple().toLowerCase());
	        ret += closeBlock();
	        ret += emptyline();
	        
			ret += writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)"+throwsClause+"{");
			increaseIdent();
			ret += writeStatement("return sorter.sort(get"+doc.getMultiple()+"ByProperty(propertyName, value), sortType)");
			ret += closeBlock();
			
			ret += writeComment("Executes a query on "+doc.getMultiple());
			ret += writeString("public QueryResult executeQueryOn"+doc.getMultiple()+"(DocumentQuery query)"+throwsClause+"{");
			increaseIdent();
			ret += writeStatement(listDecl+" all"+doc.getMultiple()+" = get"+doc.getMultiple()+"()");
			ret += writeStatement("QueryResult result = new QueryResult()");
			ret += writeString("for (int i=0; i<all"+doc.getMultiple()+".size(); i++){");
			increaseIdent();
			ret += writeStatement("List<QueryResultEntry> partialResult = query.match(all"+doc.getMultiple()+".get(i))");
			ret += writeStatement("result.add(partialResult)");
			ret += closeBlock();
			
			ret += writeStatement("return result");
			ret += closeBlock();
			ret += emptyline();
			
			ret += writeComment("Returns all "+doc.getName()+" objects, where property matches.");
	        ret += writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(QueryProperty... property)"+throwsClause+"{");
	        increaseIdent();
	        ret += writeStatement("throw new RuntimeException(\"Not yet implemented\")");
	        ret += closeBlock();
	        ret += emptyline();
	        
			ret += writeComment("Returns all "+doc.getName()+" objects, where property matches, sorted");
			ret += writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)"+throwsClause+"{");
	        increaseIdent();
	        ret += writeStatement("throw new RuntimeException(\"Not yet implemented\")");
	        ret += closeBlock();
			ret += emptyline();
			
			
	    }
	    
	    //generate export function
	    ret += emptyline();
	    ret += writeString("public Element exportToXML()"+throwsClause+"{");
	    increaseIdent();
	    ret += writeStatement("throw new RuntimeException(\"not implemented\")");
	    ret += closeBlock();
	    
	    
	    ret += closeBlock();
	    return ret;
	}
	
	private String generateFactory(MetaModule module){
	    String ret = "";

		ret += CommentGenerator.generateJavaTypeComment(getFactoryName(module),"The factory for the "+getInterfaceName(module)+" implementation.");

	    ret += writeStatement("package "+getPackageName(module));
	    ret += emptyline();
	    
	    ret += writeString("public class "+getFactoryName(module)+"{");
	    increaseIdent();

	    ret += writeString("public static "+getInterfaceName(module)+" create"+getServiceName(module)+"(){");
	    increaseIdent();
	    ret += writeString("return "+getImplementationName(module)+".getInstance();");
	    ret += closeBlock();
	    
	    ret += closeBlock();
	    return ret;
	}

	

	
	public static String getInterfaceName(MetaModule m){
	    return "I"+getServiceName(m);
	}
	
	public static String getServiceName(MetaModule m){
	    return m.getName()+"Service";
	}

	public static String getFactoryName(MetaModule m){
	    return getServiceName(m)+"Factory";
	}
	
	public static String getImplementationName(MetaModule m){
	    return getServiceName(m)+"Impl";
	}
	
}
