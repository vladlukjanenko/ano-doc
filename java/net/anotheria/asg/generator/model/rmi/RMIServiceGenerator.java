package net.anotheria.asg.generator.model.rmi;


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
import net.anotheria.asg.generator.TypeOfClass;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.AbstractServiceGenerator;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.asg.generator.model.inmemory.InMemoryServiceGenerator;
import net.anotheria.util.ExecutionTimer;
import net.anotheria.util.StringUtils;

/**
 * Generates a RMI-Backed distribution of a module interface and the according factory.
 * @author another
 *
 */
public class RMIServiceGenerator extends AbstractServiceGenerator implements IGenerator{
	
	private Context context;
	private MetaModule module;
	
	@Override public List<FileEntry> generate(IGenerateable gmodule){
		
		module = (MetaModule)gmodule;
		
		if (!module.isEnabledByOptions(GenerationOptions.RMI))
			return new ArrayList<FileEntry>();
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ExecutionTimer timer = new ExecutionTimer("RMI Generator");
		timer.startExecution(module.getName()+"Factory");
		//ret.add(new FileEntry(FileEntry.package2path(packageName), getFactoryName(mod), generateFactory(mod)));
		timer.stopExecution(module.getName()+"Factory");
		

		ret.add(new FileEntry(generateRemoteException(module)));
		ret.add(new FileEntry(generateLookup(module)));
		ret.add(new FileEntry(generateServer(module)));

		timer.startExecution(module.getName()+"Stub");
		ret.add(new FileEntry(generateStub(module)));
		timer.stopExecution(module.getName()+"Stub");

		timer.startExecution(module.getName()+"Skeleton");
		ret.add(new FileEntry(generateSkeleton(module)));
		timer.stopExecution(module.getName()+"Skeleton");

		timer.startExecution(module.getName()+"Interface");
		ret.add(new FileEntry(generateRemoteInterface(module)));
		timer.stopExecution(module.getName()+"Interface");

		//timer.printExecutionTimesOrderedByCreation();
		
		return ret;
	}

	/**
	 * Returns the name of the remote exception.
	 * @param m
	 * @return
	 */
	public String getRemoteExceptionName(MetaModule m){
	    return "RemoteExceptionWrapper";
	}

	/**
	 * Returns the implementation name of the rmi service for this module.
	 */
	public String getImplementationName(MetaModule m){
	    return "RMI"+getServiceName(m)+"Impl";
	}
	
	/**
	 * 
	 */
	public String getFactoryName(MetaModule m){
	    return "RMI"+getServiceName(m)+"Factory";
	}

	
	protected String getPackageName(MetaModule module){
		return getPackageName(GeneratorDataRegistry.getInstance().getContext(), module);
	}
	
	protected void addAdditionalFactoryImports(GeneratedClass clazz, MetaModule module){
		clazz.addImport(context.getServicePackageName(module)+"."+getInterfaceName(module));
	}
	
	public static String getPackageName(Context context, MetaModule module){
		return context.getPackageName(module)+".service.rmi";
	}
	
	private GeneratedClass generateRemoteException(MetaModule module){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getRemoteExceptionName(module), this));
		clazz.setPackageName(getPackageName(module));
		clazz.addImport("java.rmi.RemoteException");
		clazz.addImport(ServiceGenerator.getExceptionImport(module));
		
		clazz.setName("RemoteExceptionWrapper");
		clazz.setParent(ServiceGenerator.getExceptionName(module));
		 
		startClassBody();
		appendString("public RemoteExceptionWrapper(RemoteException e){");
		increaseIdent();
		appendStatement("super(e)");
		append(closeBlock());
		
		return clazz;
	}

	private GeneratedClass generateRemoteInterface(MetaModule module){
	    
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getInterfaceName(module), this));
		clazz.setPackageName(getPackageName(module));
		clazz.setType(TypeOfClass.INTERFACE);
		
		clazz.addImport("java.util.List");
		clazz.addImport("net.anotheria.util.sorter.SortType");
	    
	    
		clazz.addImport("net.anotheria.util.xml.XMLNode");
		clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
	   
		clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
		clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
		clazz.addImport("java.rmi.RemoteException");
		clazz.addImport(ServiceGenerator.getExceptionImport(module));
		clazz.addImport("net.anotheria.anodoc.util.context.CallContext");
		clazz.addImport("net.anotheria.asg.service.remote.RemoteService");
		
		clazz.setName(getInterfaceName(module));
		clazz.setParent("RemoteService");

		startClassBody();
	    
	    boolean containsAnyMultilingualDocs = false;

//	    String throwsClause = " throws "+getExceptionName(module)+", RemoteException";
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
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
	        		"Deltes mutiple "+doc.getName()+" objects.", 
        			"", 
        			"delete"+doc.getMultiple(), 
        			listDecl+" list"
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
	    
        writeInterfaceFun(
        		"creates an xml element with all contained data.", 
    			"XMLNode", 
    			"exportToXML", 
    			"String[] languages"
    			);
        
	    return clazz;
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
	
	private GeneratedClass generateLookup(MetaModule module){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getLookupName(module), this));
		clazz.setPackageName(getPackageName(module));

		clazz.addImport("org.apache.log4j.Logger");
		clazz.addImport("java.rmi.registry.Registry");
		clazz.addImport("java.rmi.registry.LocateRegistry");
		clazz.addImport("net.anotheria.asg.util.rmi.RMIConfig");
		clazz.addImport("net.anotheria.asg.util.rmi.RMIConfigFactory");

		clazz.setName(getLookupName(module));
		
		startClassBody();
		
	    appendStatement("private static Logger log = Logger.getLogger(", quote(getLookupName(module)), ")");
	    emptyline();
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
	    emptyline();
        
	    appendString("public static final String getServiceId(){");
	    appendIncreasedStatement("return ", quote(StringUtils.replace(ServiceGenerator.getInterfaceImport(module), '.', '_')));
	    appendString("}");

	    appendString("static final "+getInterfaceName(module)+" getRemote() throws Exception{");
	    appendIncreasedStatement("return ("+getInterfaceName(module)+") rmiRegistry.lookup(getServiceId())");
	    appendString("}");
	    
	    return clazz;
	}

	/**
	 * Generates the startable rmi server class.
	 * @param module
	 * @return
	 */
	private GeneratedClass generateServer(MetaModule module){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getServerName(module), this));
		clazz.setPackageName(getPackageName(module));
		
	    clazz.addImport("org.apache.log4j.xml.DOMConfigurator");
	    clazz.addImport("java.rmi.registry.Registry");
	    clazz.addImport("java.rmi.registry.LocateRegistry");
	    clazz.addImport("java.rmi.server.UnicastRemoteObject");
	    
	    clazz.addImport("net.anotheria.asg.util.rmi.RMIConfig");
	    clazz.addImport("net.anotheria.asg.util.rmi.RMIConfigFactory");
	    clazz.addImport(ServiceGenerator.getInterfaceImport(module));
	    clazz.addImport(ServiceGenerator.getFactoryImport(module));
	    clazz.addImport("net.anotheria.asg.service.InMemoryService");
	    clazz.addImport(InMemoryServiceGenerator.getInMemoryFactoryImport(module));
	    
	    clazz.setName(getServerName(module));
	    clazz.setGenerateLogger(true);
	    
	    startClassBody();

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
	    
	    emptyline();
	    appendString("try{");
	    appendIncreasedStatement("startService(rmiRegistry)");
	    appendString("}catch(Exception e){");
	    appendIncreasedStatement("log.fatal(", quote("Couldn't start service"), ", e)");
	    appendIncreasedStatement("System.err.println(", quote("Couldn't start service"), ")");
	    appendIncreasedStatement("System.exit(-2)");
	    appendString("}");
	    append(closeBlock());
	    
	    appendString("@SuppressWarnings(", quote("unchecked"), ")");
	    appendString("public static void startService(Registry registry) throws Exception{");
	    increaseIdent();
	    appendStatement("log.info(", quote("Starting " + getServerName(module)) + ")");
	    appendStatement(ServiceGenerator.getInterfaceName(module)+" myService = "+ServiceGenerator.getFactoryName(module)+".create"+module.getName()+"Service()");
	    appendStatement("String mode = System.getProperty(", quote("rmi.server.service.mode"),")");
	    appendString("if(mode != null && mode.equals(", quote("inMemory"),")){");
	    increaseIdent();
	    appendStatement("log.info(", quote("Switch to InMemory mode"), ")");
	    openTry();
	    appendStatement(ServiceGenerator.getInterfaceName(module), " inMemoryService = ", InMemoryServiceGenerator.getInMemoryFactoryName(module)+".create"+module.getName()+"Service()");
	    appendStatement("log.info(", quote("Reading " + ServiceGenerator.getInterfaceName(module) + " In Memory ..."), ")");
	    appendStatement("long startTime = System.currentTimeMillis()");
	    appendStatement("((InMemoryService<", ServiceGenerator.getInterfaceName(module), ">)inMemoryService).readFrom(myService)");
	    appendStatement("long duration = System.currentTimeMillis() - startTime");
	    appendStatement("log.info(", quote("InMemory " + ServiceGenerator.getInterfaceName(module) + " Fillage = "), "+ duration + ", quote(" ms."), ")");
	    appendStatement("myService = inMemoryService");
	    decreaseIdent();
	    appendString("} catch (Exception e) {");
	    increaseIdent();	
	    appendStatement("log.fatal(", quote("Could not read UserService In Memory: "), "+ e)");
	    appendStatement("throw e");
	    append(closeBlock());
	    append(closeBlock());
	    
	    
	    appendStatement(getInterfaceName(module)+" mySkeleton = new "+getSkeletonName(module)+"(myService)");
	    appendStatement(getInterfaceName(module)+" rmiServant = ("+getInterfaceName(module)+") UnicastRemoteObject.exportObject(mySkeleton, 0);");
		appendCommentLine("//register service.");
		appendStatement("String serviceId = "+getLookupName(module)+".getServiceId()");
		appendStatement("registry.rebind(serviceId, rmiServant)");
		appendStatement("log.info(", quote(getServerName(module)+" for service "), " + serviceId + ", quote(" is up and running.")+")");

	    append(closeBlock());
	    return clazz;
	}

	/**
	 * Generates the rmi stub.
	 * @param module
	 * @return
	 */
	private GeneratedClass generateStub(MetaModule module){
	    
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getStubName(module), this));
		clazz.setPackageName(getPackageName(module));

		clazz.addImport("java.util.List");
		clazz.addImport("net.anotheria.util.sorter.SortType");
		clazz.addImport("net.anotheria.util.xml.XMLNode");
	    clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
	   
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
	    clazz.addImport("net.anotheria.asg.service.remote.BaseRemoteServiceStub");
	    clazz.addImport("net.anotheria.asg.util.listener.IServiceListener");
	    clazz.addImport("net.anotheria.anodoc.util.context.ContextManager");
	    clazz.addImport("java.rmi.RemoteException");
	    clazz.addImport(ServiceGenerator.getExceptionImport(module));
	    clazz.addImport(ServiceGenerator.getInterfaceImport(module));
	    
	    clazz.setName(getStubName(module));
	    clazz.setParent("BaseRemoteServiceStub<"+getInterfaceName(module)+">");
	    clazz.addInterface(ServiceGenerator.getInterfaceName(module));

	    
	    boolean containsAnyMultilingualDocs = false;
	    
	    clazz.setGenerateLogger(true);
	    startClassBody();
	    
	    appendStatement("private ", getInterfaceName(module), " delegate");
	    emptyline();
	    appendString("protected void notifyDelegateFailed(){");
	    appendIncreasedStatement("delegate = null");
	    appendString("}");
	    emptyline();
	    
	    appendString("protected "+getInterfaceName(module)+" getDelegate() throws "+ServiceGenerator.getExceptionName(module)+"{");
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
	    emptyline();

	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));

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
		    		"Deletes multiple "+doc.getName()+" objects.",
		    		"",
		    		"delete"+doc.getMultiple(),
		    		listDecl+" list",
		    		"list",
		    		"list"
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
	    		
	    writeStubFun(
	    		"creates an xml element with all contained data in selected languages",
	    		"XMLNode",
	    		"exportToXML",
	    		"String[] languages",
	    		"languages",
	    		null
	    		);
		
	    ////********** //////
	    appendString("public void addServiceListener(IServiceListener listener){");
	    appendIncreasedStatement("throw new RuntimeException(", quote("Method not supported"), ")");
	    appendString("}");
	    emptyline();

	    appendString("public void removeServiceListener(IServiceListener listener){");
	    appendIncreasedStatement("throw new RuntimeException(", quote("Method not supported"), ")");
	    appendString("}");
	    emptyline();

	    appendString("public boolean hasServiceListeners(){");
	    appendIncreasedStatement("throw new RuntimeException(", quote("Method not supported"), ")");
	    appendString("}");
	    return clazz;
	}
	
	/**
	 * Generates the rmi skeleton.
	 * @param module
	 * @return
	 */
	private GeneratedClass generateSkeleton(MetaModule module){
	    
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getSkeletonName(module), this));
		clazz.setPackageName(getPackageName(module));
		
	    clazz.addImport("java.util.List");
	    clazz.addImport("net.anotheria.util.sorter.SortType");
	    
	    
	    clazz.addImport("net.anotheria.util.xml.XMLNode");
	    clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
	   
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
	    clazz.addImport("net.anotheria.anodoc.util.context.CallContext");
	    clazz.addImport("net.anotheria.anodoc.util.context.ContextManager");
	    clazz.addImport("net.anotheria.asg.service.remote.BaseRemoteServiceSkeleton");
	    clazz.addImport(ServiceGenerator.getExceptionImport(module));
	    clazz.addImport(ServiceGenerator.getInterfaceImport(module));

	    clazz.setName(getSkeletonName(module));
	    clazz.setParent("BaseRemoteServiceSkeleton");
	    clazz.addInterface(getInterfaceName(module));
	    	    
	    boolean containsAnyMultilingualDocs = false;
	    
	    clazz.setGenerateLogger(true);
	    startClassBody();
	    
	    appendStatement("private ", ServiceGenerator.getInterfaceName(module), " service");
	    emptyline();
	    
	    appendString(getSkeletonName(module)+"("+ServiceGenerator.getInterfaceName(module)+" aService){");
	    appendIncreasedStatement("service = aService");
	    appendString("}");
	    emptyline();
	    
	    List<MetaDocument> docs = module.getDocuments();


	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
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
		    		"Deletes mutiple "+doc.getName()+" objects.",
		    		"",
		    		"delete"+doc.getMultiple(),
		    		listDecl+" list",
		    		"list",
		    		"list"
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
	    		
	    writeSkeletonFun(
	    		"creates an xml element with all contained data.",
	    		"XMLNode",
	    		"exportToXML",
	    		"String languages[]",
	    		"languages",
	    		null
	    		);
	    
	    appendString("public byte[] getEcho(byte[] echoRequest){");
		appendIncreasedStatement("return echoRequest");
		appendString("}");
	    
		return clazz;
	}

	/**
	 * Writes a function into the stub.
	 * @param comment comment of the action.
	 * @param returnType the rerutn type of the action.
	 * @param funName name of the function.
	 * @param parametersFull full parameter list.
	 * @param parametersStripped stripped parameter list.
 	 * @param parametersForLogging parameters for logigng.
	 */
	private void writeStubFun(String comment, String returnType, String funName, String parametersFull, String parametersStripped, String parametersForLogging){
        appendComment(comment);
        appendString("public ",(returnType.length()>0 ? returnType+" ": "void "), funName, "("+parametersFull+")"+" throws "+getExceptionName(module)+", RemoteExceptionWrapper{");
        increaseIdent();
        openTry();
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
	    emptyline();
	}
	
	/**
	 * Writes a function to the interface.
	 * @param comment
	 * @param returnType
	 * @param funName
	 * @param parametersFull
	 */
	private void writeInterfaceFun(String comment, String returnType, String funName, String parametersFull){
        appendComment(comment);
        appendStatement("public ",(returnType.length()>0 ? returnType+" ": "void "), funName, "("+parametersFull+")"+" throws "+getExceptionName(module)+", RemoteException");
	    emptyline();
        appendComment(comment);
        appendStatement("public ",(returnType.length()>0 ? returnType+" ": "void "), funName, "(CallContext callContext"+(parametersFull.length()>0 ? ", ": "")+ parametersFull+") throws "+getExceptionName(module)+", RemoteException");
	    emptyline();
	}
	
	private void writeSkeletonFun(String comment, String returnType, String funName, String parametersFull, String parametersStripped, String parametersForLogging){
        appendComment(comment);
        appendString("public ",(returnType.length()>0 ? returnType+" ": "void "), funName, "("+parametersFull+")"+" throws "+getExceptionName(module)+"{");
        increaseIdent();
        openTry();
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
	    emptyline();

	    //version with callcontext
        appendComment(comment);
        appendString("public ",(returnType.length()>0 ? returnType+" ": "void "), funName, "(CallContext callContext", (parametersStripped!=null && parametersStripped.length()>0 ? ", ":""), parametersFull, ")"+" throws "+getExceptionName(module)+"{");
        increaseIdent();
        openTry();
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
	    emptyline();

	}

}
