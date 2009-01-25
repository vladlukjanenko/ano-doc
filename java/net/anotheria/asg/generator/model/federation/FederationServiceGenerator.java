package net.anotheria.asg.generator.model.federation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import net.anotheria.asg.generator.model.AbstractServiceGenerator;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.asg.generator.model.ServiceGenerator;

public class FederationServiceGenerator extends AbstractServiceGenerator implements IGenerator{
	
	private Context context;
	MetaProperty lastUpdate = new MetaProperty("lastUpdateTimestamp", "long");

	
	public List<FileEntry> generate(IGenerateable gmodule, Context context){
		
		MetaModule mod = (MetaModule)gmodule;
		
		this.context = context;
		String packageName = getPackageName(mod);
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ret.add(new FileEntry(generateFactory(mod)));
		ret.add(new FileEntry(FileEntry.package2path(packageName), getImplementationName(mod), generateImplementation(mod)));
		
		return ret;
	}
	
	
	public static final String FEDERATION_VARIABLE_PREFIX = "federated";
	
	private String generateImplementation(MetaModule moduleX){
		
		MetaFederationModule module = (MetaFederationModule )moduleX;
	    StringBuilder ret = new StringBuilder(5000);

	    String federationcomment = "Federated modules: ";
	    for (FederatedModuleDef fedDef : module.getFederatedModules()){
	    	federationcomment += fedDef.getName()+" as "+fedDef.getKey()+", ";
	    }
		ret.append(CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The implementation of the "+getInterfaceName(module)+"as a federated service layer: "+federationcomment));

	    ret.append(writeStatement("package "+getPackageName(module)));
	    ret.append(emptyline());
	    ret.append(writeImport("java.util.List"));
	    ret.append(writeImport("java.util.ArrayList"));
	    ret.append(writeImport("java.util.HashMap"));
		ret.append(writeImport("net.anotheria.util.sorter.SortType"));
		ret.append(writeImport("net.anotheria.util.sorter.StaticQuickSorter"));
		ret.append(writeImport("net.anotheria.util.StringUtils"));
	    ret.append(writeImport(context.getServicePackageName(MetaModule.SHARED)+".BasicService"));
	    
	    List<FederatedModuleDef> federatedModules = module.getFederatedModules();
	    Map<String,MetaModule> targetModules = new HashMap<String, MetaModule>();
	    for (FederatedModuleDef fedDef : federatedModules){
	    	MetaModule target = GeneratorDataRegistry.getInstance().getModule(fedDef.getName());
	    	if (target==null)
	    		throw new RuntimeException("No such module: "+fedDef.getName());
	    	ret.append(writeImport(ServiceGenerator.getInterfaceImport(context, target)));
	    	ret.append(writeImport(ServiceGenerator.getExceptionImport(context, target)));
	    	ret.append(writeImport(ServiceGenerator.getFactoryImport(context, target)));
	    	targetModules.put(fedDef.getKey(), target);
	    }
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret.append(writeImport(DataFacadeGenerator.getDocumentImport(context, doc)));
	        ret.append(writeImport(FederationVOGenerator.getDocumentImport(context, doc)));
	        ret.append(writeImport(DataFacadeGenerator.getDocumentFactoryImport(context, doc)));

	        List<FederatedDocumentMapping> mappings = module.getMappingsForDocument(doc.getName());

	        //generate copy methods
	        for (FederatedDocumentMapping mapping : mappings){
	        	MetaDocument target = targetModules.get(mapping.getTargetKey()).getDocumentByName(mapping.getTargetDocument());
	        	ret.append(writeImport(DataFacadeGenerator.getDocumentImport(context, target)));
	        }
	    }

	    
	    ret.append(emptyline());
	    ret.append(writeImport("net.anotheria.asg.util.listener.IServiceListener"));
	    ret.append(writeImport("net.anotheria.anodoc.query2.DocumentQuery"));
	    ret.append(writeImport("net.anotheria.anodoc.query2.QueryResult"));
	    ret.append(writeImport("net.anotheria.anodoc.query2.QueryResultEntry"));
	    ret.append(writeImport("net.anotheria.anodoc.query2.QueryProperty"));

	    ret.append(emptyline());
	    ret.append(writeImport("net.anotheria.util.xml.XMLNode"));
	    ret.append(emptyline());
	    
	    ret.append(writeString("public class "+getImplementationName(module)+" extends BasicService implements "+getInterfaceName(module)+" {"));
	    increaseIdent();
	    ret.append(writeStatement("private static "+getImplementationName(module)+" instance"));
	    ret.append(emptyline());
	    
	    String throwsClause = " throws "+ServiceGenerator.getExceptionName(module)+" ";
	    String throwClause = "throw new "+ServiceGenerator.getExceptionName(module)+"("+quote("Undelying service failed: ")+"+e.getMessage())";
	    
	    ret.append(writeCommentLine("Federated services: "));
    	ret.append(writeStatement("public static final char ID_DELIMITER = '-'"));
	    for (FederatedModuleDef fedDef : federatedModules){
	    	MetaModule target = targetModules.get(fedDef.getKey());
	    	ret.append(writeStatement(ServiceGenerator.getInterfaceName(target)+" "+FEDERATION_VARIABLE_PREFIX+fedDef.getKey()));
	    	ret.append(writeStatement("public static final String ID_PREFIX_"+fedDef.getKey()+" = "+quote(fedDef.getKey())+"+ID_DELIMITER"));
	    	targetModules.put(fedDef.getKey(), target);
	    }
	    ret.append(emptyline());
	    ret.append(writeStatement("private HashMap<String, Object> federatedServiceMap"));
	    
	    ret.append(writeString("private "+getImplementationName(module)+"(){"));
	    increaseIdent();
	    if (module.getListeners().size()>0){
	    	for (int i=0; i<module.getListeners().size(); i++){
	    		String listClassName = (String)module.getListeners().get(i);
	    		ret.append(writeStatement("addServiceListener(new "+listClassName+"())"));
	    	}
	    	ret.append(emptyline());
	    }
	    
	    //initialize federated servises;
	    ret.append(writeStatement("federatedServiceMap = new HashMap<String, Object>("+federatedModules.size()+")"));
	    for (FederatedModuleDef fedDef : federatedModules){
	    	MetaModule target = targetModules.get(fedDef.getKey());
	    	ret.append(writeStatement(FEDERATION_VARIABLE_PREFIX+fedDef.getKey()+ " = "+ServiceGenerator.getFactoryName(target)+".create"+ServiceGenerator.getServiceName(target)+"()"));
	    	targetModules.put(fedDef.getKey(), target);
	    	ret.append(writeStatement("federatedServiceMap.put("+quote(fedDef.getKey())+", "+FEDERATION_VARIABLE_PREFIX+fedDef.getKey()+")"));
	    }
	    
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
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        List<FederatedDocumentMapping> mappings = module.getMappingsForDocument(doc.getName());

	        //System.out.println("Generating document: "+doc));
	        //generate copy methods
	        for (FederatedDocumentMapping mapping : mappings){
	        	//System.out.println(" mapping: "+mapping));
	        	MetaDocument target = targetModules.get(mapping.getTargetKey()).getDocumentByName(mapping.getTargetDocument());
	        	ret.append(writeString("private "+doc.getName()+" copy("+target.getName()+" d){"));
	        	increaseIdent();
	        	
	        	ret.append(writeStatement(doc.getName()+" ret = "+DataFacadeGenerator.getDocumentFactoryName(doc)+".create"+doc.getName()+"(ID_PREFIX_"+mapping.getTargetKey()+"+d.getId())"));
	        	//we assume that we always have identical properties, or at least all properties from federation doc have a corresponding property in target doc.
	        	List<MetaProperty> properties = doc.getProperties();
	        	for (MetaProperty p : properties){
	        		ret.append(writeStatement("ret.set"+p.getAccesserName()+"(d.get"+p.getAccesserName()+"())"));
	        	}
	        	
	        	//add lastupdate copy:
	        	ret.append(writeStatement("(("+FederationVOGenerator.getDocumentImplName(doc)+")ret).set"+lastUpdate.getAccesserName()+"(d.get"+lastUpdate.getAccesserName()+"())"));
	        	
	        	ret.append(writeStatement("return ret"));
	        	ret.append(closeBlock());
	        	ret.append(emptyline());
	        	ret.append(writeString("private List<"+doc.getName()+"> copy"+doc.getName()+"List"+mapping.getTargetKey()+"(List<"+target.getName()+"> list){"));
	        	increaseIdent();
	        	ret.append(writeStatement("List<"+doc.getName()+"> ret = new ArrayList<"+doc.getName()+">(list.size())"));
	        	ret.append(writeString("for ("+target.getName()+" d : list)"));
	        	ret.append(writeIncreasedStatement("ret.add(copy(d))"));
	        	//we assume that we always have identical properties, or at least all properties from federation doc have a corresponding property in target doc.
	        	ret.append(writeStatement("return ret"));
	        	ret.append(closeBlock());
	        	ret.append(emptyline());
	        }
	        //... end copy methods
	        
	        ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement(listDecl+" "+doc.getMultiple().toLowerCase()+" = new Array"+listDecl+"()"));
	        for (FederatedDocumentMapping mapping : mappings){
	        	MetaDocument target = targetModules.get(mapping.getTargetKey()).getDocumentByName(mapping.getTargetDocument());
	        	ret.append(writeString("try{"));
	        	increaseIdent();
	        	ret.append(writeStatement("List<"+target.getName()+"> source"+mapping.getTargetKey()+" = "+FEDERATION_VARIABLE_PREFIX+mapping.getTargetKey()+".get"+target.getMultiple()+"()"));
	        	ret.append(writeStatement(doc.getMultiple().toLowerCase()+".addAll(copy"+doc.getName()+"List"+mapping.getTargetKey()+"(source"+mapping.getTargetKey()+"))"));
	        	decreaseIdent();
	        	ret.append(writeString("}catch("+ServiceGenerator.getExceptionName(targetModules.get(mapping.getTargetKey()))+" e){"));
	        	//TODO Add logging
	        	ret.append(writeCommentLine("TODO Add logging?"));
	        	ret.append(writeIncreasedStatement(throwClause));
	        	ret.append(writeString("}"));
	        }
	        
	        //"+getModuleGetterCall(module)+".get"+doc.getMultiple()+"()"));
	        ret.append(writeStatement("return "+doc.getMultiple().toLowerCase()));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
			ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType)"+throwsClause+"{"));
			increaseIdent();
			ret.append(writeStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"(), sortType)"));
			ret.append(closeBlock());
			ret.append(emptyline());

	        ret.append(writeString("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("delete"+doc.getName()+"("+doc.getVariableName()+".getId())"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        ret.append(writeString("public void delete"+doc.getName()+"(String id)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("throw new RuntimeException(\"not implemented.\")"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        //delete multiple
	        ret.append(writeComment("Deletes multiple "+doc.getName()+" objects."));
	        ret.append(writeStatement("public void delete"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("throw new RuntimeException("+quote("Not yet implemented")+")"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        ret.append(writeString("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("String tokens[] = StringUtils.tokenize(id, ID_DELIMITER)"));
	        
	        

	        
	        for(FederatedDocumentMapping mapping : mappings){
	        	ret.append(writeString("if (tokens[0].equals("+quote(mapping.getTargetKey())+")){"));
	        	increaseIdent();
	        	ret.append(writeString("try{"));
	        	ret.append(writeIncreasedStatement("return copy("+FEDERATION_VARIABLE_PREFIX+mapping.getTargetKey()+".get"+mapping.getTargetDocument()+"(tokens[1]))"));
	        	ret.append(writeString("}catch("+ServiceGenerator.getExceptionName(targetModules.get(mapping.getTargetKey()))+" e){"));
	        	//TODO Add logging
	        	ret.append(writeCommentLine("TODO Add logging?"));
	        	ret.append(writeIncreasedStatement(throwClause));
	        	ret.append(writeString("}"));
	        	decreaseIdent();
	        	ret.append(writeString("}"));
	        }
	        ret.append(writeStatement("throw new RuntimeException("+quote("Unknown federated key: ")+"+tokens[0]+"+quote(" in ")+"+id)"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        ret.append(writeString("public "+doc.getName()+" import"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("throw new RuntimeException(\"no import in federated services.\")"));
	        ret.append(closeBlock());
	        ret.append(emptyline());


	        ret.append(writeString("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("if (1==1) throw new RuntimeException(\"not implemented.\")"));
	        
	        ret.append(writeString("if (hasServiceListeners()){"));
	        increaseIdent();
	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
	        ret.append(writeIncreasedStatement("myListeners.get(i).documentCreated("+doc.getVariableName()+")"));
	        ret.append(closeBlock());	
	        
	        
	        ret.append(writeStatement("return "+doc.getVariableName()));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        
	        //create multiple
	        ret.append(writeComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions."));
	        ret.append(writeStatement("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("throw new RuntimeException("+quote("Not yet implemented")+")"));
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        
	        //update multiple
	        ret.append(writeComment("Updates multiple new "+doc.getName()+" objects.\nReturns the updated versions."));
	        ret.append(writeStatement("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("throw new RuntimeException("+quote("Not yet implemented")+")"));
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        
	        ret.append(writeString("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("if (1==1) throw new RuntimeException(\"not implemented.\")"));
	        ret.append(writeStatement(doc.getName()+" oldVersion = null"));
	        
	        ret.append(writeString("//if (hasServiceListeners())"));
	        ret.append(writeIncreasedStatement("//oldVersion = module.get"+doc.getName()+"("+doc.getVariableName()+".getId())"));
	        
	        
	        ret.append(writeString("if (hasServiceListeners()){"));
	        increaseIdent();
	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
	        ret.append(writeIncreasedStatement("myListeners.get(i).documentUpdated(oldVersion, "+doc.getVariableName()+")"));
	        ret.append(closeBlock());
	        
	        ret.append(writeStatement("return "+doc.getVariableName()));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement(listDecl+" "+doc.getMultiple().toLowerCase()+" = new Array"+listDecl+"()"));
	        for (FederatedDocumentMapping mapping : mappings){
	        	MetaDocument target = targetModules.get(mapping.getTargetKey()).getDocumentByName(mapping.getTargetDocument());
	        	ret.append(writeString("try{"));
	        	increaseIdent();
	        	ret.append(writeStatement("List<"+target.getName()+"> source"+mapping.getTargetKey()+" = "+FEDERATION_VARIABLE_PREFIX+mapping.getTargetKey()+".get"+target.getMultiple()+"ByProperty(propertyName, value)"));
	        	ret.append(writeStatement(doc.getMultiple().toLowerCase()+".addAll(copy"+doc.getName()+"List"+mapping.getTargetKey()+"(source"+mapping.getTargetKey()+"))"));
	        	decreaseIdent();
	        	ret.append(writeString("}catch("+ServiceGenerator.getExceptionName(targetModules.get(mapping.getTargetKey()))+" e){"));
	        	//TODO Add logging
	        	ret.append(writeCommentLine("TODO Add logging?"));
	        	ret.append(writeIncreasedStatement(throwClause));
	        	ret.append(writeString("}"));
	        }
	        
	        //"+getModuleGetterCall(module)+".get"+doc.getMultiple()+"()"));
	        ret.append(writeStatement("return "+doc.getMultiple().toLowerCase()));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
			ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)"+throwsClause+"{"));
			increaseIdent();
			ret.append(writeStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(propertyName, value), sortType)"));
			ret.append(closeBlock());
			
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
	        ret.append(writeStatement("throw new RuntimeException(\"Not yet implemented\")"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
			ret.append(writeComment("Returns all "+doc.getName()+" objects, where property matches, sorted"));
			ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("throw new RuntimeException(\"Not yet implemented\")"));
	        ret.append(closeBlock());
			ret.append(emptyline());
			
			
	    }
	    
	    //generate export function
	    ret.append(emptyline());
	    ret.append(writeString("public XMLNode exportToXML()"+throwsClause+"{"));
	    increaseIdent();
        ret.append(writeStatement("return new XMLNode("+quote("unimplemented_federated_export_"+module.getName())+")"));
	    ret.append(closeBlock());
	    
	    ret.append(emptyline());
	    ret.append(writeString("public XMLNode exportToXML(String[] languages)"+throwsClause+"{"));
	    increaseIdent();
        ret.append(writeStatement("return new XMLNode("+quote("unimplemented_federated_export_"+module.getName())+")"));
	    ret.append(closeBlock());

	    ret.append(closeBlock());
	    return ret.toString();
	}
}
