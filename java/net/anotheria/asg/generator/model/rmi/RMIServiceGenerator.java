package net.anotheria.asg.generator.model.rmi;


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

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a RMI-Backed distribution of a module interface and the according factory.
 * @author lrosenberg
 *
 */
public class RMIServiceGenerator extends AbstractServiceGenerator implements IGenerator{
	
	/**
	 * Currently generated module.
	 */
	private MetaModule module;
	
	@Override public List<FileEntry> generate(IGenerateable gmodule){
		
		module = (MetaModule)gmodule;
		
		if (!module.isEnabledByOptions(GenerationOptions.RMI))
			return new ArrayList<FileEntry>();
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ExecutionTimer timer = new ExecutionTimer("RMI Generator");

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

        timer.startExecution(module.getName()+"Factory");
        ret.add(new FileEntry(generateFactory(module)));
		//ret.add(new FileEntry(FileEntry.package2path(packageName), getFactoryName(mod), generateFactory(mod)));
		timer.stopExecution(module.getName()+"Factory");

		//timer.printExecutionTimesOrderedByCreation();
		
		return ret;
	}

	/**
	 * Returns the name of the remote exception.
	 * @param m
	 * @return name of exception
	 */
	public String getRemoteExceptionName(MetaModule m){
	    return "RemoteExceptionWrapper";
	}

	/**
	 * Returns the implementation name of the rmi service for this module.
     * Currently implementation not used! Hack  for using  Stub.
	 * @param m
	 * @return name of service
	 */
	public String getImplementationName(MetaModule m){
	    return getStubName(m);//"RMI"+getServiceName(m)+"Impl";
	}
	
	/**
	 * @return the name of the factory for the service.
	 */
	public String getFactoryName(MetaModule m){
	    return "RMI"+getServiceName(m)+"Factory";
	}

	/**
	 * @return the package name for the given module.
	 */
	protected String getPackageName(MetaModule aModule){
		return getPackageName(GeneratorDataRegistry.getInstance().getContext(), aModule);
	}
	
	protected void addAdditionalFactoryImports(GeneratedClass clazz, MetaModule aModule){
       clazz.addImport(GeneratorDataRegistry.getInstance().getContext().getServicePackageName(aModule)+"."+ServiceGenerator.getInterfaceName(aModule));
	}
	
	public static String getPackageName(Context context, MetaModule module){
		return context.getPackageName(module)+".service.rmi";
	}
	
	/**
	 * Generates the RemoteException class for this modules service.
	 * @param aModule
	 * @return
	 */
	private GeneratedClass generateRemoteException(MetaModule aModule){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateRemoteException");
		
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getRemoteExceptionName(aModule), this));
		clazz.setPackageName(getPackageName(aModule));
		clazz.addImport("java.rmi.RemoteException");
		clazz.addImport(ServiceGenerator.getExceptionImport(aModule));
		
		clazz.setName("RemoteExceptionWrapper");
		clazz.setParent(ServiceGenerator.getExceptionName(aModule));
		 
		startClassBody();
		appendString("public RemoteExceptionWrapper(RemoteException e){");
		increaseIdent();
		appendStatement("super(e)");
		closeBlockNEW();
		
		return clazz;
	}

	/**
	 * Generates the interface for the remote invocation for the given module.
	 * @param aModule
	 * @return generated interface
	 */
	private GeneratedClass generateRemoteInterface(MetaModule aModule){
	    
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateRemoteInterface");
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getInterfaceName(aModule), this));
		clazz.setPackageName(getPackageName(aModule));
		clazz.setType(TypeOfClass.INTERFACE);
		
		clazz.addImport("java.util.List");
		clazz.addImport("net.anotheria.util.sorter.SortType");
		clazz.addImport("net.anotheria.util.slicer.Segment");
	    	    
		clazz.addImport("net.anotheria.util.xml.XMLNode");
		clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
	   
		clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
		clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
		clazz.addImport("java.rmi.RemoteException");
		clazz.addImport(ServiceGenerator.getExceptionImport(aModule));
		clazz.addImport("net.anotheria.anodoc.util.context.CallContext");
		clazz.addImport("net.anotheria.asg.service.remote.RemoteService");
		
		clazz.setName(getInterfaceName(aModule));
		clazz.setParent("RemoteService");

		startClassBody();
	    
	    boolean containsAnyMultilingualDocs = false;

//	    String throwsClause = " throws "+getExceptionName(module)+", RemoteException";
	    
	    List<MetaDocument> docs = aModule.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
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
	        		"Imports multiple "+doc.getName()+" objects.\nReturns the imported versions.", 
        			listDecl, 
        			"import"+doc.getMultiple(), 
        			listDecl+" list"
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
	        		"Returns all "+doc.getName()+" objects, where property matches, sorted.", 
        			listDecl, 
        			"get"+doc.getMultiple()+"ByProperty", 
        			"SortType sortType, QueryProperty... property"
        			);

	        // get elements COUNT
			writeInterfaceFun("Returns " + doc.getMultiple() + "objects count.", "int", "get" + doc.getMultiple() + "Count", "");
			// end get elements COUNT

			// get elements Segment
			writeInterfaceFun("Returns " + doc.getName() + " objects segment.", listDecl, "get" + doc.getMultiple(), "Segment aSegment");
			// end get elements Segment

			// get elements Segment with FILTER
			writeInterfaceFun("Returns " + doc.getName() + " objects segment, where property matched.", listDecl, "get" + doc.getMultiple()
					+ "ByProperty", "Segment aSegment, QueryProperty... property");
			// end get elements Segment with FILTER

			// get elements Segment with SORTING, FILTER
			writeInterfaceFun("Returns " + doc.getName() + " objects segment, where property matched, sorted.", listDecl, "get"
					+ doc.getMultiple() + "ByProperty", "Segment aSegment, SortType sortType, QueryProperty... property");
			// end get elements Segment with SORTING, FILTER
			
			if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
		        writeInterfaceFun(
		        		"In all documents of type "+doc.getName()+" copies all multilingual fields from sourceLanguage to targetLanguage", 
	        			"", 
	        			"copyMultilingualAttributesInAll"+doc.getMultiple(), 
	        			"String sourceLanguage, String targetLanguage"
	        			);
				containsAnyMultilingualDocs = true;
			}
			
			writeInterfaceFun(
					"Creates an xml element with selected contained data.",
					"XMLNode",
					"export"+doc.getMultiple()+"ToXML",
					"List<"+doc.getName()+"> list"
			);

			if (containsAnyMultilingualDocs && GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported()) {
				writeInterfaceFun(
						"Creates an xml element with selected contained data.",
						"XMLNode",
						"export"+doc.getMultiple()+"ToXML",
						"String[] languages, List<"+doc.getName()+"> list"
				);
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
        		"Executes a query on all data objects (documents, vo) which are part of this module and managed by this service", 
    			"QueryResult", 
    			"executeQueryOnAllObjects", 
    			"DocumentQuery query"
    			);
	    
        writeInterfaceFun(
        		"creates an xml element with all contained data.", 
    			"XMLNode", 
    			"exportToXML", 
    			""
    			);
	    
        if (containsAnyMultilingualDocs && GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported()){
	        writeInterfaceFun(
	        		"creates an xml element with all contained data.", 
	    			"XMLNode", 
	    			"exportToXML", 
	    			"String[] languages"
	    			);
        }
        
	    return clazz;
	}
	
	/**
	 * Returns the name of the remote interface.
	 * @param mod
	 * @return name of interface
	 */
	public static String getInterfaceName(MetaModule mod){
		return "Remote"+getServiceName(mod);
	}
	
	/**
	 * Returns the name of the rmi stub.
	 * @param mod
	 * @return name of stub
	 */
	public static String getStubName(MetaModule mod){
		return "Remote"+getServiceName(mod)+"Stub";
	}

	/**
	 * Returns the name of the rmi server class.
	 * @param mod
	 * @return name of service class
	 */
	public static String getServerName(MetaModule mod){
		return mod.getName()+"Server";
	}

	/**
	 * Returns the name of the rmi lookup function class.
	 * @param mod
	 * @return name of rmi lookup function class
	 */
	public static String getLookupName(MetaModule mod){
		return getServiceName(mod)+"RMILookup";
	}

	/**
	 * Returns the name of the rmi skeleton class.
	 * @param mod
	 * @return name of the skeleton class
	 */
	public static String getSkeletonName(MetaModule mod){
		return "Remote"+getServiceName(mod)+"Skeleton";
	}
	
	/**
	 * Generates lookup utility class.
	 * @param aModule
	 * @return utility class
	 */
	private GeneratedClass generateLookup(MetaModule aModule){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateLookup");
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getLookupName(aModule), this));
		clazz.setPackageName(getPackageName(aModule));

		clazz.addImport("org.slf4j.Logger");
		clazz.addImport("org.slf4j.LoggerFactory");
		clazz.addImport("org.slf4j.MarkerFactory");

		clazz.addImport("java.rmi.registry.Registry");
		clazz.addImport("java.rmi.registry.LocateRegistry");
		clazz.addImport("net.anotheria.asg.util.rmi.RMIConfig");
		clazz.addImport("net.anotheria.asg.util.rmi.RMIConfigFactory");

		clazz.setName(getLookupName(aModule));
		
		startClassBody();
		
	    appendStatement("private static Logger log = LoggerFactory.getLogger(", quote(getLookupName(aModule)), ")");
	    emptyline();
	    appendStatement("private static Registry rmiRegistry");
	    appendString("static{");
	    increaseIdent();
	    appendCommentLine("lookup rmi registry");
	    appendStatement("RMIConfig config = RMIConfigFactory.getRMIConfig()");
	    appendString("try{");
	    appendIncreasedStatement("rmiRegistry = LocateRegistry.getRegistry(config.getRegistryHost(), config.getRegistryPort())");
	    appendString("}catch(Exception e){");
	    appendIncreasedStatement("log.error(MarkerFactory.getMarker(\"FATAL\"), \"Coulnd't obtain rmi registry\", e)");
	    appendString("}");
        closeBlockNEW();
	    emptyline();
        
	    appendString("public static final String getServiceId(){");
	    appendIncreasedStatement("return ", quote(StringUtils.replace(ServiceGenerator.getInterfaceImport(aModule), '.', '_')));
	    appendString("}");

	    appendString("static final "+getInterfaceName(aModule)+" getRemote() throws Exception{");
	    appendIncreasedStatement("return ("+getInterfaceName(aModule)+") rmiRegistry.lookup(getServiceId())");
	    appendString("}");
	    
	    return clazz;
	}

	/**
	 * Generates the startable rmi server class.
	 * @param aModule
	 * @return startable server class
	 */
	private GeneratedClass generateServer(MetaModule aModule){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateServer");
		
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getServerName(aModule), this));
		clazz.setPackageName(getPackageName(aModule));
		
	    clazz.addImport("java.rmi.registry.Registry");
	    clazz.addImport("java.rmi.registry.LocateRegistry");
	    clazz.addImport("java.rmi.server.UnicastRemoteObject");
	    
	    clazz.addImport("net.anotheria.asg.util.rmi.RMIConfig");
	    clazz.addImport("net.anotheria.asg.util.rmi.RMIConfigFactory");
	    clazz.addImport(ServiceGenerator.getInterfaceImport(aModule));
	    clazz.addImport(ServiceGenerator.getFactoryImport(aModule));
	    clazz.addImport("net.anotheria.asg.service.InMemoryService");
	    clazz.addImport(InMemoryServiceGenerator.getInMemoryFactoryImport(aModule));
	   	clazz.addImport("org.slf4j.MarkerFactory");
	    clazz.setName(getServerName(aModule));
	    clazz.setGenerateLogger(true);
	    
	    startClassBody();

	    appendString("public static void main(String a[]){");
	    increaseIdent();
	    appendStatement("Registry rmiRegistry = null");
	    appendCommentLine("lookup rmi registry");
	    appendStatement("RMIConfig config = RMIConfigFactory.getRMIConfig()");
	    appendString("try{");
	    appendIncreasedStatement("rmiRegistry = LocateRegistry.getRegistry(config.getRegistryHost(), config.getRegistryPort())");
	    appendString("}catch(Exception e){");
	    appendIncreasedStatement("log.error(MarkerFactory.getMarker(\"FATAL\"), \"Coulnd't obtain rmi registry\", e)");
	    appendIncreasedStatement("System.err.println(\"Coulnd't obtain rmi registry\")");
	    appendIncreasedStatement("System.exit(-1)");
	    appendString("}");
	    
	    emptyline();
	    appendString("try{");
	    appendIncreasedStatement("startService(rmiRegistry)");
	    appendString("}catch(Exception e){");
	    appendIncreasedStatement("log.error(MarkerFactory.getMarker(\"FATAL\"), ", quote("Couldn't start service"), ", e)");
	    appendIncreasedStatement("System.err.println(", quote("Couldn't start service"), ")");
	    appendIncreasedStatement("System.exit(-2)");
	    appendString("}");
	    closeBlockNEW();
	    
	    appendString("@SuppressWarnings(", quote("unchecked"), ")");
	    appendString("public static void startService(Registry registry) throws Exception{");
	    increaseIdent();
	    appendStatement("log.info(", quote("Starting " + getServerName(aModule)) + ")");
	    appendStatement(ServiceGenerator.getInterfaceName(aModule)+" myService = "+ServiceGenerator.getFactoryName(aModule)+".create"+aModule.getName()+"Service()");
	    appendStatement("String mode = System.getProperty(", quote("rmi.server.service.mode"),")");
	    appendString("if(mode != null && mode.equals(", quote("inMemory"), ")){");
	    increaseIdent();
	    appendStatement("log.info(", quote("Switch to InMemory mode"), ")");
	    openTry();
	    appendStatement(ServiceGenerator.getInterfaceName(aModule), " inMemoryService = ", InMemoryServiceGenerator.getInMemoryFactoryName(aModule) + ".create" + aModule.getName() + "Service()");
	    appendStatement("log.info(", quote("Reading " + ServiceGenerator.getInterfaceName(aModule) + " In Memory ..."), ")");
	    appendStatement("long startTime = System.currentTimeMillis()");
	    appendStatement("((InMemoryService<", ServiceGenerator.getInterfaceName(aModule), ">)inMemoryService).readFrom(myService)");
	    appendStatement("long duration = System.currentTimeMillis() - startTime");
	    appendStatement("log.info(", quote("InMemory " + ServiceGenerator.getInterfaceName(aModule) + " Fillage = "), "+ duration + ", quote(" ms."), ")");
	    appendStatement("myService = inMemoryService");
	    decreaseIdent();
	    appendString("} catch (Exception e) {");
	    increaseIdent();	
	    appendStatement("log.error(MarkerFactory.getMarker(\"FATAL\"), ", quote("Could not read UserService In Memory: "), "+ e)");
	    appendStatement("throw e");
	    closeBlockNEW();
	    closeBlockNEW();
	    
	    
	    appendStatement(getInterfaceName(aModule)+" mySkeleton = new "+getSkeletonName(aModule)+"(myService)");
	    appendStatement(getInterfaceName(aModule)+" rmiServant = ("+getInterfaceName(aModule)+") UnicastRemoteObject.exportObject(mySkeleton, 0);");
		appendCommentLine("//register service.");
		appendStatement("String serviceId = "+getLookupName(aModule)+".getServiceId()");
		appendStatement("registry.rebind(serviceId, rmiServant)");
		appendStatement("log.info(", quote(getServerName(aModule)+" for service "), " + serviceId + ", quote(" is up and running.")+")");

	    closeBlockNEW();
	    return clazz;
	}

	/**
	 * Generates the rmi stub.
	 * @param aModule
	 * @return
	 */
	private GeneratedClass generateStub(MetaModule aModule){
	    
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateStub");
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getStubName(aModule), this));
		clazz.setPackageName(getPackageName(aModule));

		clazz.addImport("java.util.List");
		clazz.addImport("net.anotheria.util.sorter.SortType");
		clazz.addImport("net.anotheria.util.xml.XMLNode");
		clazz.addImport("net.anotheria.util.slicer.Segment");
	    clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
	   
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
	    clazz.addImport("net.anotheria.asg.service.remote.BaseRemoteServiceStub");
	    clazz.addImport("net.anotheria.asg.util.listener.IServiceListener");
	    clazz.addImport("net.anotheria.anodoc.util.context.ContextManager");
	    clazz.addImport("java.rmi.RemoteException");
	    clazz.addImport(ServiceGenerator.getExceptionImport(aModule));
	    clazz.addImport(ServiceGenerator.getInterfaceImport(aModule));
	    
	    clazz.setName(getStubName(aModule));
	    clazz.setParent("BaseRemoteServiceStub<"+getInterfaceName(aModule)+">");
	    clazz.addInterface(ServiceGenerator.getInterfaceName(aModule));

	    
	    boolean containsAnyMultilingualDocs = false;
	    
	    clazz.setGenerateLogger(true);
	    startClassBody();
        //Static instance
        appendStatement("private static "+getStubName(aModule)+" instance");
	    emptyline();
	    
	    appendStatement("private ", getInterfaceName(aModule), " delegate");
	    emptyline();
	    appendString("protected void notifyDelegateFailed(){");
	    appendIncreasedStatement("delegate = null");
	    appendString("}");
	    emptyline();

        //Private constructor
        appendString("private "+getStubName(aModule)+"(){ }");
	    emptyline();

        appendString("public static final "+getStubName(aModule)+" getInstance(){");
	    increaseIdent();
	    appendString("if (instance==null){");
	    increaseIdent();
	    appendStatement("instance = new "+getStubName(aModule)+"()");
	    closeBlockNEW();
	    appendStatement("return instance");
	    closeBlockNEW();
	    emptyline();
	    
	    appendString("protected "+getInterfaceName(aModule)+" getDelegate() throws "+ServiceGenerator.getExceptionName(aModule)+"{");
        increaseIdent();
	    appendString("if (delegate==null){");
        increaseIdent();
        appendString("synchronized(this){");
        increaseIdent();
	    appendString("if (delegate==null){");
        increaseIdent();
        appendString("try{");
        increaseIdent();
        appendStatement("delegate = "+getLookupName(aModule)+".getRemote()");
        decreaseIdent();
        appendString("}catch(Exception e){");
        appendIncreasedStatement("throw new "+ServiceGenerator.getExceptionName(aModule)+"(e)");
	    appendString("}");
        
        closeBlockNEW();
        closeBlockNEW();
        closeBlockNEW();
	    appendStatement("return delegate");
        closeBlockNEW();
	    emptyline();

	    List<MetaDocument> docs = aModule.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
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
		    		"Imports multiple "+doc.getName()+" objects.\nReturns the imported versions.",
		    		listDecl,
		    		"import"+doc.getMultiple(),
		    		listDecl+" list",
		    		"list",
		    		"list"
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
		    		"Returns all "+doc.getName()+" objects, where property matches, sorted.",
		    		listDecl,
		    		"get"+doc.getMultiple()+"ByProperty",
		    		"SortType sortType, QueryProperty... property",
		    		"sortType, property",
		    		"sortType + "+quote(", ")+" + java.util.Arrays.asList(property)"
		    		);
			
		    // get elements COUNT
			writeStubFun("Returns all " + doc.getMultiple() + " count.", "int", "get" + doc.getMultiple() + "Count", "", "", null);
			// end get elements COUNT

			// get elements Segment
			writeStubFun("Returns " + doc.getMultiple() + " objects segment.", listDecl, "get" + doc.getMultiple(), "Segment aSegment",
					"aSegment", null);
			// end get elements Segment

			// get elements Segment with FILTER
			writeStubFun("Returns " + doc.getName() + " objects segment, where property matched.", listDecl, "get"
					+ doc.getMultiple() + "ByProperty", "Segment aSegment, QueryProperty... property", "aSegment, property",
					"java.util.Arrays.asList(property)");
			// end get elements Segment with FILTER

			// get elements Segment with SORTING, FILTER
			writeStubFun("Returns " + doc.getName() + " objects segment, where property matched, sorted.", listDecl, "get"
					+ doc.getMultiple() + "ByProperty", "Segment aSegment, SortType sortType, QueryProperty... property",
					"aSegment, sortType, property", "sortType + " + quote(", ") + " + java.util.Arrays.asList(property)");
			// end get elements Segment with SORTING, FILTER
			
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
			
			writeStubFun(
					"Creates an xml element with all contained data.",
					"XMLNode",
					"export"+doc.getMultiple()+"ToXML",
					"List<"+doc.getName()+"> list"+doc.getMultiple(),
					"list"+doc.getMultiple(),
					null
			);

			if (containsAnyMultilingualDocs && GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported()) {
				writeStubFun(
						"Creates an xml element with all contained data in selected languages",
						"XMLNode",
						"export"+doc.getMultiple()+"ToXML",
						"String[] languages, List<"+doc.getName()+"> list"+doc.getMultiple(),
						"languages, list"+doc.getMultiple(),
						null
				);
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
	    		"Executes a query on all data objects (documents, vo) which are part of this module and managed by this service",
	    		"QueryResult",
	    		"executeQueryOnAllObjects",
	    		"DocumentQuery query",
	    		"query",
	    		"query"
	    		);
	    
	    writeStubFun(
	    		"creates an xml element with all contained data.",
	    		"XMLNode",
	    		"exportToXML",
	    		"",
	    		"",
	    		null
	    		);
	    
	    if (containsAnyMultilingualDocs && GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported()){
		    writeStubFun(
		    		"creates an xml element with all contained data in selected languages",
		    		"XMLNode",
		    		"exportToXML",
		    		"String[] languages",
		    		"languages",
		    		null
		    		);
	    }
		
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
	 * @param aModule
	 * @return
	 */
	private GeneratedClass generateSkeleton(MetaModule aModule){
	    
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateSkeleton");
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getSkeletonName(aModule), this));
		clazz.setPackageName(getPackageName(aModule));
		
	    clazz.addImport("java.util.List");
	    clazz.addImport("net.anotheria.util.sorter.SortType");
	    clazz.addImport("net.anotheria.util.slicer.Segment"); 
	    clazz.addImport("net.anotheria.util.xml.XMLNode");
	    clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
	   
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
	    clazz.addImport("net.anotheria.anodoc.util.context.CallContext");
	    clazz.addImport("net.anotheria.anodoc.util.context.ContextManager");
	    clazz.addImport("net.anotheria.asg.service.remote.BaseRemoteServiceSkeleton");
	    clazz.addImport(ServiceGenerator.getExceptionImport(aModule));
	    clazz.addImport(ServiceGenerator.getInterfaceImport(aModule));

	    clazz.setName(getSkeletonName(aModule));
	    clazz.setParent("BaseRemoteServiceSkeleton");
	    clazz.addInterface(getInterfaceName(aModule));
	    	    
	    boolean containsAnyMultilingualDocs = false;
	    
	    clazz.setGenerateLogger(true);
	    startClassBody();
	    
	    appendStatement("private ", ServiceGenerator.getInterfaceName(aModule), " service");
	    emptyline();
	    
	    appendString(getSkeletonName(aModule)+"("+ServiceGenerator.getInterfaceName(aModule)+" aService){");
	    appendIncreasedStatement("service = aService");
	    appendString("}");
	    emptyline();
	    
	    List<MetaDocument> docs = aModule.getDocuments();


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
		    		"Imports multiple new "+doc.getName()+" objects.\nReturns the imported versions.",
		    		listDecl,
		    		"import"+doc.getMultiple(),
		    		listDecl+" list",
		    		"list",
		    		"list"
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
		    		"Returns all "+doc.getName()+" objects, where property matches, sorted.",
		    		listDecl,
		    		"get"+doc.getMultiple()+"ByProperty",
		    		"SortType sortType, QueryProperty... property",
		    		"sortType, property",
		    		"sortType + "+quote(", ")+" + java.util.Arrays.asList(property)"
		    		);
		    
		    // get elements COUNT
			writeSkeletonFun("Returns all " + doc.getMultiple() + " count.", "int", "get" + doc.getMultiple() + "Count", "", "", null);
			// end get elements COUNT

			// get elements Segment
			writeSkeletonFun("Returns " + doc.getName() + " objects segment.", listDecl, "get" + doc.getMultiple(), "Segment aSegment",
					"aSegment", null);
			// end get elements Segment

			// get elements Segment with FILTER
			writeSkeletonFun("Returns " + doc.getName() + " objects segment, where property matched.", listDecl, "get" + doc.getMultiple()
					+ "ByProperty", "Segment aSegment, QueryProperty... property", "aSegment, property",
					"java.util.Arrays.asList(property)");
			// end get elements Segment with FILTER

			// get elements Segment with SORTING, FILTER
			writeSkeletonFun("Returns " + doc.getName() + " objects segment, where property matched, sorted.", listDecl, "get"
					+ doc.getMultiple() + "ByProperty", "Segment aSegment, SortType sortType, QueryProperty... property",
					"aSegment, sortType, property", "sortType + " + quote(", ") + " + java.util.Arrays.asList(property)");
			// end get elements Segment with SORTING, FILTER
			
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

			writeExportSkeletonFun(
					"creates an xml element with selected contained data.",
					"XMLNode",
					"export" + doc.getMultiple() + "ToXML",
					"List<" + doc.getName() + "> list"+doc.getMultiple(),
					"list"+doc.getMultiple()
			);

			if (containsAnyMultilingualDocs && GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported()) {
				writeExportSkeletonFun(
						"creates an xml element with selected contained data.",
						"XMLNode",
						"export" + doc.getMultiple() + "ToXML",
						"String languages[] languages ,List<" + doc.getName() + ">, list"+doc.getMultiple(),
						"languages, list"+doc.getMultiple()
				);
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
	    		"Executes a query on all data objects (documents, vo) which are part of this module and managed by this service",
	    		"QueryResult",
	    		"executeQueryOnAllObjects",
	    		"DocumentQuery query",
	    		"query",
	    		"query"
	    		);
	    
	    writeSkeletonFun(
	    		"creates an xml element with all contained data.",
	    		"XMLNode",
	    		"exportToXML",
	    		"",
	    		"",
	    		null
	    		);
	    
	    if (containsAnyMultilingualDocs && GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported()){
		    writeSkeletonFun(
		    		"creates an xml element with all contained data.",
		    		"XMLNode",
		    		"exportToXML",
		    		"String languages[]",
		    		"languages",
		    		null
		    		);
	    }
	    
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
        closeBlockNEW();
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
        closeBlockNEW();
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
        closeBlockNEW();
	    emptyline();

	}

	/**
	 * Writes Export Sceleton function.
	 * @param comment comment
	 * @param returnType return type
	 * @param funName function name
	 * @param parametersFull params with param type
	 * @param parametersStripped params name
	 */
	private void writeExportSkeletonFun(String comment, String returnType, String funName, String parametersFull, String parametersStripped) {
		appendComment(comment);
		appendString("public ", (returnType.length() > 0 ? returnType + " " : "void "), funName, "(" + parametersFull + ") throws " + getExceptionName(module) + "{");
		increaseIdent();
		openTry();
		appendStatement((returnType.length() > 0 ? "return " : ""), "service.", funName, "(", parametersStripped, ")");
		decreaseIdent();
		appendString("} catch (Throwable unexpectedError){");
		appendIncreasedStatement("String errorMessage = ", quote(funName + "()"));
		appendIncreasedStatement("log.error(errorMessage, unexpectedError)");
		appendIncreasedStatement("throw new " + ServiceGenerator.getExceptionName(module) + "(errorMessage, unexpectedError)");

		appendString("}");
		closeBlockNEW();
		emptyline();
		//version with callcontext
		appendComment(comment);
		appendString("public ", (returnType.length() > 0 ? returnType + " " : "void "), funName, "(CallContext callContext", (parametersStripped != null && parametersStripped.length() > 0 ? ", " : ""), parametersFull, ")" + " throws " + getExceptionName(module) + "{");
		increaseIdent();
		openTry();
		appendStatement("ContextManager.setCallContext(callContext)");
		appendStatement((returnType.length() > 0 ? "return " : ""), "service.", funName, "(", parametersStripped, ")");
		decreaseIdent();
		appendString("} catch (Throwable unexpectedError){");
		appendIncreasedStatement("String errorMessage = ", quote(funName + "()"));
		appendIncreasedStatement("log.error(errorMessage, unexpectedError)");
		appendIncreasedStatement("throw new " + ServiceGenerator.getExceptionName(module) + "(errorMessage, unexpectedError)");
		appendString("}");
		closeBlockNEW();
		emptyline();
	}

	@Override protected String getMoskitoSubsystem(){
		return super.getMoskitoSubsystem()+"-rmi";
	}

}
