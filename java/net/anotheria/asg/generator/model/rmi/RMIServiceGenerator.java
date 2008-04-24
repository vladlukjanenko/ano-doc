package net.anotheria.asg.generator.model.rmi;


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
import net.anotheria.util.StringUtils;

/**
 * Generates a DB-Backed implementation of a module interface and the according factory.
 * @author another
 *
 */
public class RMIServiceGenerator extends AbstractServiceGenerator implements IGenerator{
	
	private Context context;
	private MetaModule module;
	
	public List<FileEntry> generate(IGenerateable gmodule, Context context){
		
		module = (MetaModule)gmodule;
		
		this.context = context;
		String packageName = getPackageName(context, module);
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ExecutionTimer timer = new ExecutionTimer("RMI Generator");
		timer.startExecution(module.getName()+"Factory");
		//ret.add(new FileEntry(FileEntry.package2path(packageName), getFactoryName(mod), generateFactory(mod)));
		timer.stopExecution(module.getName()+"Factory");
		

		ret.add(new FileEntry(FileEntry.package2path(packageName), getRemoteExceptionName(module), generateRemoteException(module)));
		ret.add(new FileEntry(FileEntry.package2path(packageName), getLookupName(module), generateLookup(module)));
		ret.add(new FileEntry(FileEntry.package2path(packageName), getServerName(module), generateServer(module)));

		timer.startExecution(module.getName()+"Stub");
		ret.add(new FileEntry(FileEntry.package2path(packageName), getStubName(module), generateStub(module)));
		timer.stopExecution(module.getName()+"Stub");

		timer.startExecution(module.getName()+"Skeleton");
		ret.add(new FileEntry(FileEntry.package2path(packageName), getSkeletonName(module), generateSkeleton(module)));
		timer.stopExecution(module.getName()+"Skeleton");

		timer.startExecution(module.getName()+"Interface");
		ret.add(new FileEntry(FileEntry.package2path(packageName), getInterfaceName(module), generateRemoteInterface(module)));
		timer.stopExecution(module.getName()+"Interface");

		//timer.printExecutionTimesOrderedByCreation();
		
		return ret;
	}

	public String getRemoteExceptionName(MetaModule m){
	    return "RemoteExceptionWrapper";
	}

	public String getImplementationName(MetaModule m){
	    return "RMI"+getServiceName(m)+"Impl";
	}
	
	public String getFactoryName(MetaModule m){
	    return "RMI"+getServiceName(m)+"Factory";
	}

	
	protected String getPackageName(MetaModule module){
		return getPackageName(context, module);
	}
	
	protected String writeAdditionalFactoryImports(MetaModule module){
		String ret = writeImport(context.getServicePackageName(module)+"."+getInterfaceName(module));
		//ret += writeImport("net.anotheria.asg.service.InMemoryService");
		return ret;
	}
	
	

	
	public static String getPackageName(Context context, MetaModule module){
		return context.getPackageName(module)+".service.rmi";
	}
	
	/*
	protected String getSupportedInterfacesList(MetaModule module){
		return super.getSupportedInterfacesList(module)+", InMemoryService.class";
	}*/
	
	private String generateRemoteException(MetaModule module){
		startNewJob();
		append(CommentGenerator.generateJavaTypeComment(getRemoteExceptionName(module)));
	    appendStatement("package "+getPackageName(module));
	    appendEmptyline();
	    appendImport("java.rmi.RemoteException");
	    appendImport(ServiceGenerator.getExceptionImport(context, module));
	    appendEmptyline();
		 
		appendString("public class RemoteExceptionWrapper extends "+ServiceGenerator.getExceptionName(module)+"{");
		increaseIdent();
		appendString("public RemoteExceptionWrapper(RemoteException e){");
		increaseIdent();
		appendStatement("super(e)");
		append(closeBlock());
		append(closeBlock());
		
		return getCurrentJobContent().toString();
	}

	private String generateRemoteInterface(MetaModule module){
	    
		startNewJob();
		append(CommentGenerator.generateJavaTypeComment(getInterfaceName(module)));
 
	    appendStatement("package "+getPackageName(module));
	    appendEmptyline();
	    appendImport("java.util.List");
	    appendImport("net.anotheria.util.sorter.SortType");
	    appendEmptyline();
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        appendImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    }
	    appendEmptyline();
	    
	    appendImport("net.anotheria.util.xml.XMLNode");
	    appendEmptyline();
	    appendImport("net.anotheria.anodoc.query2.DocumentQuery");
	   
	    appendImport("net.anotheria.anodoc.query2.QueryResult");
	    appendImport("net.anotheria.anodoc.query2.QueryProperty");
	    appendEmptyline();
	    //appendImport("net.anotheria.asg.service.ASGService");
	    //appendEmptyline();
	    appendImport("java.rmi.Remote");
	    appendImport("java.rmi.RemoteException");
	    appendEmptyline();
	    appendImport(ServiceGenerator.getExceptionImport(context, module));
	    appendEmptyline();
	    appendImport("net.anotheria.anodoc.util.context.CallContext");
	    appendEmptyline();

	    appendString("public interface "+getInterfaceName(module)+" extends Remote {");
	    increaseIdent();
	    
	    boolean containsAnyMultilingualDocs = false;

	    String throwsClause = " throws "+getExceptionName(module)+", RemoteException";
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        
	        
	        writeInterfaceFun(
	        			"Returns all "+doc.getMultiple()+" objects stored.", 
	        			listDecl, 
	        			"get"+doc.getMultiple(), 
	        			""
	        			);
	        
	        writeInterfaceFun(
	        		"Returns all "+doc.getMultiple()+" objects sorted by given sortType.", 
        			listDecl, 
        			"get"+doc.getMultiple(), 
        			"SortType sortType"
        			);
	        
	        writeInterfaceFun(
	        		"Deletes a "+doc.getName()+" object by id.", 
        			"", 
        			"delete"+doc.getName(), 
        			"String id"
        			);

	        writeInterfaceFun(
	        		"Deletes a "+doc.getName()+" object.", 
        			"", 
        			"delete"+doc.getName(), 
        			doc.getName()+" "+doc.getVariableName()
        			);

	        writeInterfaceFun(
	        		"Returns the "+doc.getName()+" object with the specified id.", 
        			doc.getName(), 
        			"get"+doc.getName(), 
        			"String id"
        			);
	        
	        writeInterfaceFun(
	        		"Imports a "+doc.getName()+" object.\nReturns the imported version.", 
        			doc.getName(), 
        			"import"+doc.getName(), 
        			doc.getName()+" "+doc.getVariableName()
        			);

	        writeInterfaceFun(
	        		"Creates a new "+doc.getName()+" object.\nReturns the created version.", 
        			doc.getName(), 
        			"create"+doc.getName(), 
        			doc.getName()+" "+doc.getVariableName()
        			);

	        writeInterfaceFun(
	        		"Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.", 
        			listDecl, 
        			"create"+doc.getMultiple(), 
        			listDecl+" list"
        			);

	        writeInterfaceFun(
	        		"Updates a "+doc.getName()+" object.\nReturns the updated version.", 
        			doc.getName(), 
        			"update"+doc.getName(), 
        			doc.getName()+" "+doc.getVariableName()
        			);

	        writeInterfaceFun(
	        		"Updates mutiple "+doc.getName()+" objects.\nReturns the updated versions.", 
        			listDecl, 
        			"update"+doc.getMultiple(), 
        			listDecl+" list"
        			);

	        
	        //special functions
	        writeInterfaceFun(
	        		"Returns all "+doc.getName()+" objects, where property with given name equals object.", 
        			listDecl, 
        			"get"+doc.getMultiple()+"ByProperty", 
        			"String propertyName, Object value"
        			);

	        writeInterfaceFun(
	        		"Returns all "+doc.getName()+" objects, where property with given name equals object, sorted.", 
        			listDecl, 
        			"get"+doc.getMultiple()+"ByProperty", 
        			"String propertyName, Object value, SortType sortType"
        			);
	        
	        writeInterfaceFun(
	        		"Executes a query", 
        			"QueryResult", 
        			"executeQueryOn"+doc.getMultiple(), 
        			"DocumentQuery query"
        			);
			
			
	        writeInterfaceFun(
	        		"Returns all "+doc.getName()+" objects, where property matches.", 
        			listDecl, 
        			"get"+doc.getMultiple()+"ByProperty", 
        			"QueryProperty... property"
        			);

	        writeInterfaceFun(
	        		"Returns all "+doc.getName()+" objects, where property matches, sorted", 
        			listDecl, 
        			"get"+doc.getMultiple()+"ByProperty", 
        			"SortType sortType, QueryProperty... property"
        			);

	        
			
			if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
		        writeInterfaceFun(
		        		"In all documents of type "+doc.getName()+" copies all multilingual fields from sourceLanguage to targetLanguage", 
	        			"", 
	        			"copyMultilingualAttributesInAll"+doc.getMultiple(), 
	        			"String sourceLanguage, String targetLanguage"
	        			);
				containsAnyMultilingualDocs = true;
			}
	    }
	    
	    if (containsAnyMultilingualDocs){
	        writeInterfaceFun(
	        		"Copies all multilingual fields from sourceLanguage to targetLanguage in all data objects (documents, vo) which are part of this module and managed by this service", 
	    			"", 
	    			"copyMultilingualAttributesInAllObjects", 
	    			"String sourceLanguage, String targetLanguage"
	    			);
	    }
	    
        writeInterfaceFun(
        		"creates an xml element with all contained data.", 
    			"XMLNode", 
    			"exportToXML", 
    			""
    			);
	    
	    append(closeBlock());
	    return getCurrentJobContent().toString();
	}
	
	public static String getInterfaceName(MetaModule mod){
		return "Remote"+getServiceName(mod);
	}
	
	public static String getStubName(MetaModule mod){
		return "Remote"+getServiceName(mod)+"Stub";
	}

	public static String getServerName(MetaModule mod){
		return mod.getName()+"Server";
	}

	public static String getLookupName(MetaModule mod){
		return getServiceName(mod)+"RMILookup";
	}

	public static String getSkeletonName(MetaModule mod){
		return "Remote"+getServiceName(mod)+"Skeleton";
	}
	
	private String generateLookup(MetaModule module){
		startNewJob();
		append(CommentGenerator.generateJavaTypeComment(getLookupName(module)));
 
	    appendStatement("package "+getPackageName(module));
	    appendEmptyline();
	    appendImport("org.apache.log4j.Logger");
	    appendEmptyline();
	    appendImport("java.rmi.registry.Registry");
	    appendImport("java.rmi.registry.LocateRegistry");
	    appendEmptyline();
	    appendImport("net.anotheria.asg.util.rmi.RMIConfig");
	    appendImport("net.anotheria.asg.util.rmi.RMIConfigFactory");
	    appendEmptyline();
	    

	    appendString("public class "+getLookupName(module)+"{");
	    increaseIdent();
	    appendStatement("private static Logger log = Logger.getLogger(", quote(getLookupName(module)), ")");
	    appendEmptyline();
	    appendStatement("private static Registry rmiRegistry");
	    appendString("static{");
	    increaseIdent();
	    appendCommentLine("lookup rmi registry");
	    appendStatement("RMIConfig config = RMIConfigFactory.getRMIConfig()");
	    appendString("try{");
	    appendIncreasedStatement("rmiRegistry = LocateRegistry.getRegistry(config.getRegistryHost(), config.getRegistryPort())");
	    appendString("}catch(Exception e){");
	    appendIncreasedStatement("log.fatal(\"Coulnd't obtain rmi registry\", e)");
	    appendString("}");
        append(closeBlock());
	    appendEmptyline();
        
	    appendString("public static final String getServiceId(){");
	    appendIncreasedStatement("return ", quote(StringUtils.replace(ServiceGenerator.getInterfaceImport(context, module), '.', '_')));
	    appendString("}");

	    appendString("static final "+getInterfaceName(module)+" getRemote() throws Exception{");
	    appendIncreasedStatement("return ("+getInterfaceName(module)+") rmiRegistry.lookup(getServiceId())");
	    appendString("}");
	    
	    appendString(closeBlock());
	    
	    return getCurrentJobContent().toString();
	}

	private String generateServer(MetaModule module){
		startNewJob();
		append(CommentGenerator.generateJavaTypeComment(getServerName(module)));
 
	    appendStatement("package "+getPackageName(module));
	    appendEmptyline();
	    appendImport("org.apache.log4j.Logger");
	    appendImport("org.apache.log4j.xml.DOMConfigurator");
	    appendEmptyline();
	    appendImport("java.rmi.registry.Registry");
	    appendImport("java.rmi.registry.LocateRegistry");
	    appendImport("java.rmi.server.UnicastRemoteObject");
	    
	    appendEmptyline();
	    appendImport("net.anotheria.asg.util.rmi.RMIConfig");
	    appendImport("net.anotheria.asg.util.rmi.RMIConfigFactory");
	    appendEmptyline();
	    appendImport(ServiceGenerator.getInterfaceImport(context, module));
	    appendImport(ServiceGenerator.getFactoryImport(context, module));
	    

	    appendString("public class "+getServerName(module)+"{");
	    increaseIdent();
	    appendStatement("private static Logger log = Logger.getLogger(", quote(getServerName(module)), ")");
	    appendEmptyline();

	    appendString("public static void main(String a[]){");
	    increaseIdent();
	    appendStatement("DOMConfigurator.configureAndWatch(",quote("/log4j.xml"), ")");
	    appendStatement("Registry rmiRegistry = null");
	    appendCommentLine("lookup rmi registry");
	    appendStatement("RMIConfig config = RMIConfigFactory.getRMIConfig()");
	    appendString("try{");
	    appendIncreasedStatement("rmiRegistry = LocateRegistry.getRegistry(config.getRegistryHost(), config.getRegistryPort())");
	    appendString("}catch(Exception e){");
	    appendIncreasedStatement("log.fatal(\"Coulnd't obtain rmi registry\", e)");
	    appendIncreasedStatement("System.err.println(\"Coulnd't obtain rmi registry\")");
	    appendIncreasedStatement("System.exit(-1)");
	    appendString("}");
	    
	    appendEmptyline();
	    appendString("try{");
	    appendIncreasedStatement("startService(rmiRegistry)");
	    appendString("}catch(Exception e){");
	    appendIncreasedStatement("log.fatal(", quote("Couldn't start service"), ", e)");
	    appendIncreasedStatement("System.err.println(", quote("Couldn't start service"), ")");
	    appendIncreasedStatement("System.exit(-2)");
	    appendString("}");
	    append(closeBlock());
	    

	    appendString("public static void startService(Registry registry) throws Exception{");
	    increaseIdent();
	    appendStatement(ServiceGenerator.getInterfaceName(module)+" myService = "+ServiceGenerator.getFactoryName(module)+".create"+module.getName()+"Service()");
	    appendStatement(getInterfaceName(module)+" mySkeleton = new "+getSkeletonName(module)+"(myService)");
	    appendStatement(getInterfaceName(module)+" rmiServant = ("+getInterfaceName(module)+") UnicastRemoteObject.exportObject(mySkeleton, 0);");
		appendCommentLine("//register service.");
		appendStatement("String serviceId = "+getLookupName(module)+".getServiceId()");
		appendStatement("registry.rebind(serviceId, rmiServant)");
		appendStatement("log.info(", quote(getServerName(module)+" for service "), " + serviceId + ", quote(" is up and running.")+")");

	    append(closeBlock());
	    
	    
	    appendString(closeBlock());
	    
	    return getCurrentJobContent().toString();
	}

	private String generateStub(MetaModule module){
	    
		startNewJob();
		append(CommentGenerator.generateJavaTypeComment(getStubName(module)));
 
	    appendStatement("package "+getPackageName(module));
	    appendEmptyline();
	    appendImport("java.util.List");
	    appendImport("net.anotheria.util.sorter.SortType");
	    appendEmptyline();
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        appendImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    }
	    appendEmptyline();
	    
	    appendImport("net.anotheria.util.xml.XMLNode");
	    appendEmptyline();
	    appendImport("net.anotheria.anodoc.query2.DocumentQuery");
	   
	    appendImport("net.anotheria.anodoc.query2.QueryResult");
	    appendImport("net.anotheria.anodoc.query2.QueryProperty");
	    appendEmptyline();
	    //appendImport("net.anotheria.asg.service.ASGService");
	    appendImport("net.anotheria.asg.util.listener.IServiceListener");
	    appendImport("net.anotheria.anodoc.util.context.ContextManager");
	    appendEmptyline();
	    appendImport("java.rmi.RemoteException");
	    appendEmptyline();
	    appendImport(ServiceGenerator.getExceptionImport(context, module));
	    appendImport(ServiceGenerator.getInterfaceImport(context, module));
	    appendEmptyline();
	    

	    appendImport("org.apache.log4j.Logger");
	    appendEmptyline();

	    appendString("public class "+getStubName(module)+" implements "+ServiceGenerator.getInterfaceName(module)+" {");
	    increaseIdent();
	    appendEmptyline();

	    
	    boolean containsAnyMultilingualDocs = false;
	    
	    appendStatement("private static Logger log = Logger.getLogger(", quote(getStubName(module)), ")");
	    appendEmptyline();
	    appendStatement("private ", getInterfaceName(module), " delegate");
	    appendEmptyline();
	    appendString("private void notifyDelegateFailed(){");
	    appendIncreasedStatement("delegate = null");
	    appendString("}");
	    appendEmptyline();
	    
	    appendString("private "+getInterfaceName(module)+" getDelegate() throws "+ServiceGenerator.getExceptionName(module)+"{");
        increaseIdent();
	    appendString("if (delegate==null){");
        increaseIdent();
        appendString("synchronized(this){");
        increaseIdent();
	    appendString("if (delegate==null){");
        increaseIdent();
        appendString("try{");
        increaseIdent();
        appendStatement("delegate = "+getLookupName(module)+".getRemote()");
        decreaseIdent();
        appendString("}catch(Exception e){");
        appendIncreasedStatement("throw new "+ServiceGenerator.getExceptionName(module)+"(e)");
	    appendString("}");
        
        append(closeBlock());
        append(closeBlock());
        append(closeBlock());
	    appendStatement("return delegate");
        append(closeBlock());
	    appendEmptyline();

	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        
		    writeStubFun(
		    		"Returns all "+doc.getMultiple()+" objects stored.",
		    		listDecl,
		    		"get"+doc.getMultiple(),
		    		"",
		    		"",
		    		null
		    		);

		    writeStubFun(
		    		"Returns all "+doc.getMultiple()+" objects sorted by given sortType.",
		    		listDecl,
		    		"get"+doc.getMultiple(),
		    		"SortType sortType",
		    		"sortType",
		    		"sortType"
		    		);
	        
		    writeStubFun(
		    		"Deletes a "+doc.getName()+" object by id.",
		    		"",
		    		"delete"+doc.getName(),
		    		"String id",
		    		"id",
		    		"id"
		    		);
	        
		    writeStubFun(
		    		"Deletes a "+doc.getName()+" object.",
		    		"",
		    		"delete"+doc.getName(),
		    		doc.getName()+" "+doc.getVariableName(),
		    		doc.getVariableName(),
		    		doc.getVariableName()
		    		);
			
		    writeStubFun(
		    		"Returns the "+doc.getName()+" object with the specified id.",
		    		doc.getName(),
		    		"get"+doc.getName(),
		    		"String id",
		    		"id",
		    		"id"
		    		);
	        
		    writeStubFun(
		    		"Creates a new "+doc.getName()+" object.\nReturns the created version.",
		    		doc.getName(),
		    		"create"+doc.getName(),
		    		doc.getName()+" "+doc.getVariableName(),
		    		doc.getVariableName(),
		    		doc.getVariableName()
		    		);
	        
		    writeStubFun(
		    		"Imports a new "+doc.getName()+" object.\nReturns the imported version.",
		    		doc.getName(),
		    		"import"+doc.getName(),
		    		doc.getName()+" "+doc.getVariableName(),
		    		doc.getVariableName(),
		    		doc.getVariableName()
		    		);

		    writeStubFun(
		    		"Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.",
		    		listDecl,
		    		"create"+doc.getMultiple(),
		    		listDecl+" list",
		    		"list",
		    		"list"
		    		);
	        
	        
		    writeStubFun(
		    		"Updates a "+doc.getName()+" object.\nReturns the updated version.",
		    		doc.getName(),
		    		"update"+doc.getName(),
		    		doc.getName()+" "+doc.getVariableName(),
		    		doc.getVariableName(),
		    		doc.getVariableName()
		    		);

		    writeStubFun(
		    		"Updates mutiple "+doc.getName()+" objects.\nReturns the updated versions.",
		    		listDecl,
		    		"update"+doc.getMultiple(),
		    		listDecl+" list",
		    		"list",
		    		"list"
		    		);

	        
	        //special functions
		    writeStubFun(
		    		"Returns all "+doc.getName()+" objects, where property with given name equals object.",
		    		listDecl,
		    		"get"+doc.getMultiple()+"ByProperty",
		    		"String propertyName, Object value",
		    		"propertyName, value",
		    		"propertyName + "+quote(", ")+" + value"
		    		);

		    writeStubFun(
		    		"Returns all "+doc.getName()+" objects, where property with given name equals object, sorted.",
		    		listDecl,
		    		"get"+doc.getMultiple()+"ByProperty",
		    		"String propertyName, Object value, SortType sortType",
		    		"propertyName, value, sortType",
		    		"propertyName + "+quote(", ")+" + value + "+quote(", ")+" + sortType"
		    		);
		    

		    writeStubFun(
		    		"Executes a query",
		    		"QueryResult",
		    		"executeQueryOn"+doc.getMultiple(),
		    		"DocumentQuery query",
		    		"query",
		    		"query"
		    		);
		    
		    writeStubFun(
		    		"Returns all "+doc.getName()+" objects, where property matches.",
		    		listDecl,
		    		"get"+doc.getMultiple()+"ByProperty",
		    		"QueryProperty... property",
		    		"property",
		    		"java.util.Arrays.asList(property)"
		    		);
		    
		    writeStubFun(
		    		"Returns all "+doc.getName()+" objects, where property matches, sorted",
		    		listDecl,
		    		"get"+doc.getMultiple()+"ByProperty",
		    		"SortType sortType, QueryProperty... property",
		    		"sortType, property",
		    		"sortType + "+quote(", ")+" + java.util.Arrays.asList(property)"
		    		);
			
			
			
			if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
			    writeStubFun(
			    		"In all documents of type "+doc.getName()+" copies all multilingual fields from sourceLanguage to targetLanguage",
			    		"",
			    		"copyMultilingualAttributesInAll"+doc.getMultiple(),
			    		"String sourceLanguage, String targetLanguage",
			    		"sourceLanguage, targetLanguage",
			    		"sourceLanguage + "+quote(", ")+" + targetLanguage"
			    		);

				containsAnyMultilingualDocs = true;
			}
	    }
	    
	    if (containsAnyMultilingualDocs){
		    writeStubFun(
		    		"Copies all multilingual fields from sourceLanguage to targetLanguage in all data objects (documents, vo) which are part of this module and managed by this service",
		    		"",
		    		"copyMultilingualAttributesInAllObjects",
		    		"String sourceLanguage, String targetLanguage",
		    		"sourceLanguage, targetLanguage",
		    		"sourceLanguage + "+quote(", ")+" + targetLanguage"
		    		);
	    }
	    
	    
	    writeStubFun(
	    		"creates an xml element with all contained data.",
	    		"XMLNode",
	    		"exportToXML",
	    		"",
	    		"",
	    		null
	    		);
	    		
	    
	    ////********** //////
	    appendString("public void addServiceListener(IServiceListener listener){");
	    appendIncreasedStatement("throw new RuntimeException(", quote("Method not supported"), ")");
	    appendString("}");
	    appendEmptyline();

	    appendString("public void removeServiceListener(IServiceListener listener){");
	    appendIncreasedStatement("throw new RuntimeException(", quote("Method not supported"), ")");
	    appendString("}");
	    appendEmptyline();

	    appendString("public boolean hasServiceListeners(){");
	    appendIncreasedStatement("throw new RuntimeException(", quote("Method not supported"), ")");
	    appendString("}");
	    appendEmptyline();
	    
	    
	    
	    append(closeBlock());
	    return getCurrentJobContent().toString();
	}
	
	
	private String generateSkeleton(MetaModule module){
	    
		startNewJob();
		append(CommentGenerator.generateJavaTypeComment(getSkeletonName(module)));
 
	    appendStatement("package "+getPackageName(module));
	    appendEmptyline();
	    appendImport("java.util.List");
	    appendImport("net.anotheria.util.sorter.SortType");
	    appendEmptyline();
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        appendImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    }
	    appendEmptyline();
	    
	    appendImport("net.anotheria.util.xml.XMLNode");
	    appendEmptyline();
	    appendImport("net.anotheria.anodoc.query2.DocumentQuery");
	   
	    appendImport("net.anotheria.anodoc.query2.QueryResult");
	    appendImport("net.anotheria.anodoc.query2.QueryProperty");
	    appendImport("net.anotheria.anodoc.util.context.CallContext");
	    appendImport("net.anotheria.anodoc.util.context.ContextManager");
	    appendEmptyline();
	    
	    //appendImport("net.anotheria.asg.service.ASGService");
	    appendEmptyline();
	    appendImport(ServiceGenerator.getExceptionImport(context, module));
	    appendImport(ServiceGenerator.getInterfaceImport(context, module));
	    appendEmptyline();

	    appendImport("org.apache.log4j.Logger");
	    appendEmptyline();

	    appendString("public class "+getSkeletonName(module)+" implements "+getInterfaceName(module)+" {");
	    increaseIdent();
	    appendEmptyline();

	    
	    boolean containsAnyMultilingualDocs = false;
	    
	    appendStatement("private static Logger log = Logger.getLogger(", quote(getSkeletonName(module)), ")");
	    appendEmptyline();
	    appendStatement("private ", ServiceGenerator.getInterfaceName(module), " service");
	    appendEmptyline();
	    
	    appendString(getSkeletonName(module)+"("+ServiceGenerator.getInterfaceName(module)+" aService){");
	    appendIncreasedStatement("service = aService");
	    appendString("}");
	    appendEmptyline();

	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        
		    writeSkeletonFun(
		    		"Returns all "+doc.getMultiple()+" objects stored.",
		    		listDecl,
		    		"get"+doc.getMultiple(),
		    		"",
		    		"",
		    		null
		    		);

		    writeSkeletonFun(
		    		"Returns all "+doc.getMultiple()+" objects sorted by given sortType.",
		    		listDecl,
		    		"get"+doc.getMultiple(),
		    		"SortType sortType",
		    		"sortType",
		    		"sortType"
		    		);
	        
		    writeSkeletonFun(
		    		"Deletes a "+doc.getName()+" object by id.",
		    		"",
		    		"delete"+doc.getName(),
		    		"String id",
		    		"id",
		    		"id"
		    		);
	        
		    writeSkeletonFun(
		    		"Deletes a "+doc.getName()+" object.",
		    		"",
		    		"delete"+doc.getName(),
		    		doc.getName()+" "+doc.getVariableName(),
		    		doc.getVariableName(),
		    		doc.getVariableName()
		    		);
			
		    writeSkeletonFun(
		    		"Returns the "+doc.getName()+" object with the specified id.",
		    		doc.getName(),
		    		"get"+doc.getName(),
		    		"String id",
		    		"id",
		    		"id"
		    		);
	        
		    writeSkeletonFun(
		    		"Imports a new "+doc.getName()+" object.\nReturns the imported version.",
		    		doc.getName(),
		    		"import"+doc.getName(),
		    		doc.getName()+" "+doc.getVariableName(),
		    		doc.getVariableName(),
		    		doc.getVariableName()
		    		);

		    writeSkeletonFun(
		    		"Creates a new "+doc.getName()+" object.\nReturns the created version.",
		    		doc.getName(),
		    		"create"+doc.getName(),
		    		doc.getName()+" "+doc.getVariableName(),
		    		doc.getVariableName(),
		    		doc.getVariableName()
		    		);
	        
		    writeSkeletonFun(
		    		"Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.",
		    		listDecl,
		    		"create"+doc.getMultiple(),
		    		listDecl+" list",
		    		"list",
		    		"list"
		    		);
	        
	        
		    writeSkeletonFun(
		    		"Updates a "+doc.getName()+" object.\nReturns the updated version.",
		    		doc.getName(),
		    		"update"+doc.getName(),
		    		doc.getName()+" "+doc.getVariableName(),
		    		doc.getVariableName(),
		    		doc.getVariableName()
		    		);

		    writeSkeletonFun(
		    		"Updates mutiple "+doc.getName()+" objects.\nReturns the updated versions.",
		    		listDecl,
		    		"update"+doc.getMultiple(),
		    		listDecl+" list",
		    		"list",
		    		"list"
		    		);

	        
	        //special functions
		    writeSkeletonFun(
		    		"Returns all "+doc.getName()+" objects, where property with given name equals object.",
		    		listDecl,
		    		"get"+doc.getMultiple()+"ByProperty",
		    		"String propertyName, Object value",
		    		"propertyName, value",
		    		"propertyName + "+quote(", ")+" + value"
		    		);

		    writeSkeletonFun(
		    		"Returns all "+doc.getName()+" objects, where property with given name equals object, sorted.",
		    		listDecl,
		    		"get"+doc.getMultiple()+"ByProperty",
		    		"String propertyName, Object value, SortType sortType",
		    		"propertyName, value, sortType",
		    		"propertyName + "+quote(", ")+" + value + "+quote(", ")+" + sortType"
		    		);
		    

		    writeSkeletonFun(
		    		"Executes a query",
		    		"QueryResult",
		    		"executeQueryOn"+doc.getMultiple(),
		    		"DocumentQuery query",
		    		"query",
		    		"query"
		    		);
		    
		    writeSkeletonFun(
		    		"Returns all "+doc.getName()+" objects, where property matches.",
		    		listDecl,
		    		"get"+doc.getMultiple()+"ByProperty",
		    		"QueryProperty... property",
		    		"property",
		    		"java.util.Arrays.asList(property)"
		    		);
		    
		    writeSkeletonFun(
		    		"Returns all "+doc.getName()+" objects, where property matches, sorted",
		    		listDecl,
		    		"get"+doc.getMultiple()+"ByProperty",
		    		"SortType sortType, QueryProperty... property",
		    		"sortType, property",
		    		"sortType + "+quote(", ")+" + java.util.Arrays.asList(property)"
		    		);
			
			
			
			if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
			    writeSkeletonFun(
			    		"In all documents of type "+doc.getName()+" copies all multilingual fields from sourceLanguage to targetLanguage",
			    		"",
			    		"copyMultilingualAttributesInAll"+doc.getMultiple(),
			    		"String sourceLanguage, String targetLanguage",
			    		"sourceLanguage, targetLanguage",
			    		"sourceLanguage + "+quote(", ")+" + targetLanguage"
			    		);

				containsAnyMultilingualDocs = true;
			}
	    }
	    
	    if (containsAnyMultilingualDocs){
		    writeSkeletonFun(
		    		"Copies all multilingual fields from sourceLanguage to targetLanguage in all data objects (documents, vo) which are part of this module and managed by this service",
		    		"",
		    		"copyMultilingualAttributesInAllObjects",
		    		"String sourceLanguage, String targetLanguage",
		    		"sourceLanguage, targetLanguage",
		    		"sourceLanguage + "+quote(", ")+" + targetLanguage"
		    		);
	    }
	    
	    
	    writeSkeletonFun(
	    		"creates an xml element with all contained data.",
	    		"XMLNode",
	    		"exportToXML",
	    		"",
	    		"",
	    		null
	    		);
	    		
	    
	    append(closeBlock());
	    return getCurrentJobContent().toString();
	}

	
	private void writeStubFun(String comment, String returnType, String funName, String parametersFull, String parametersStripped, String parametersForLogging){
        appendComment(comment);
        appendString("public ",(returnType.length()>0 ? returnType+" ": "void "), funName, "("+parametersFull+")"+" throws "+getExceptionName(module)+", RemoteExceptionWrapper{");
        increaseIdent();
        appendTry();
        appendStatement((returnType.length()>0 ? "return " : "" ),"getDelegate().",funName, "(ContextManager.getCallContext()", (parametersStripped!=null && parametersStripped.length()>0 ? ", ":""),  parametersStripped ,")");
        decreaseIdent();
        appendString("} catch (RemoteException e){");
        if (parametersForLogging!=null && parametersForLogging.length()>0)
        	appendIncreasedStatement("log.error(", quote(funName+"("), " + "+parametersForLogging+" + ", quote(")") , ", e)");
        else
        	appendIncreasedStatement("log.error(", quote(funName+"()") , ", e)");
        appendIncreasedStatement("notifyDelegateFailed()");
        appendIncreasedStatement("throw new RemoteExceptionWrapper(e)");
        appendString("}");
        append(closeBlock());
	    appendEmptyline();
	}

	private void writeInterfaceFun(String comment, String returnType, String funName, String parametersFull){
        appendComment(comment);
        appendStatement("public ",(returnType.length()>0 ? returnType+" ": "void "), funName, "("+parametersFull+")"+" throws "+getExceptionName(module)+", RemoteException");
	    appendEmptyline();
        appendComment(comment);
        appendStatement("public ",(returnType.length()>0 ? returnType+" ": "void "), funName, "(CallContext callContext"+(parametersFull.length()>0 ? ", ": "")+ parametersFull+") throws "+getExceptionName(module)+", RemoteException");
	    appendEmptyline();
	}

	private void writeSkeletonFun(String comment, String returnType, String funName, String parametersFull, String parametersStripped, String parametersForLogging){
        appendComment(comment);
        appendString("public ",(returnType.length()>0 ? returnType+" ": "void "), funName, "("+parametersFull+")"+" throws "+getExceptionName(module)+"{");
        increaseIdent();
        appendTry();
        appendStatement((returnType.length()>0 ? "return " : "" ),"service.",funName, "(", parametersStripped ,")");
        decreaseIdent();
        appendString("} catch ("+ServiceGenerator.getExceptionName(module)+" e){");
        appendIncreasedStatement("throw(e)");
        appendString("} catch (Throwable unexpectedError){");
        if (parametersForLogging!=null && parametersForLogging.length()>0){
        	appendIncreasedStatement("String errorMessage = ", quote(funName+"("), " + "+parametersForLogging+" + ", quote(")"));
        }else{
        	appendIncreasedStatement("String errorMessage = ", quote(funName+"()"));
        }
    	appendIncreasedStatement("log.error(errorMessage, unexpectedError)");
    	appendIncreasedStatement("throw new "+ServiceGenerator.getExceptionName(module)+"(errorMessage, unexpectedError)");

    	appendString("}");
        append(closeBlock());
	    appendEmptyline();

	    //version with callcontext
        appendComment(comment);
        appendString("public ",(returnType.length()>0 ? returnType+" ": "void "), funName, "(CallContext callContext", (parametersStripped!=null && parametersStripped.length()>0 ? ", ":""), parametersFull, ")"+" throws "+getExceptionName(module)+"{");
        increaseIdent();
        appendTry();
        appendStatement("ContextManager.setCallContext(callContext)");
        appendStatement((returnType.length()>0 ? "return " : "" ),"service.",funName, "(", parametersStripped ,")");
        decreaseIdent();
        appendString("} catch ("+ServiceGenerator.getExceptionName(module)+" e){");
        appendIncreasedStatement("throw(e)");
        appendString("} catch (Throwable unexpectedError){");
        if (parametersForLogging!=null && parametersForLogging.length()>0){
        	appendIncreasedStatement("String errorMessage = ", quote(funName+"("), " + "+parametersForLogging+" + ", quote(")"));
        }else{
        	appendIncreasedStatement("String errorMessage = ", quote(funName+"()"));
        }
    	appendIncreasedStatement("log.error(errorMessage, unexpectedError)");
    	appendIncreasedStatement("throw new "+ServiceGenerator.getExceptionName(module)+"(errorMessage, unexpectedError)");

    	appendString("}");
        append(closeBlock());
	    appendEmptyline();

	}

}
