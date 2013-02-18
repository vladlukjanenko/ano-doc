package net.anotheria.asg.generator.model;


import net.anotheria.anoprise.metafactory.ServiceFactory;
import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.moskito.core.dynamic.MoskitoInvokationProxy;
import net.anotheria.moskito.core.predefined.ServiceStatsCallHandler;
import net.anotheria.moskito.core.predefined.ServiceStatsFactory;

/**
 * Base class for service generators.
 * @author another
 *
 */
public class AbstractServiceGenerator extends AbstractGenerator{
	/**
	 * Returns the interface name for the CRUD service for this module.
	 * @param m
	 * @return
	 */
	public static String getInterfaceName(MetaModule m){
	    return "I"+getServiceName(m);
	}
	/**
	 * Returns the service name for a module.
	 * @param m
	 * @return
	 */
	public static final String getServiceName(MetaModule m){
	    return m.getName()+"Service";
	}

	/**
	 * Returns the name of the factory class.
	 * @param m
	 * @return
	 */
	public String getFactoryName(MetaModule m){
	    return getServiceName(m)+"Factory";
	}
	/**
	 * Returns the implementation name for the service for this MetaModule.
	 * @param m
	 * @return
	 */
	public String getImplementationName(MetaModule m){
	    return getServiceName(m)+"Impl";
	}

	protected String getPackageName(MetaModule module){
		return GeneratorDataRegistry.getInstance().getContext().getServicePackageName(module);
	}

	protected void addAdditionalFactoryImports(GeneratedClass clazz, MetaModule module){
	}

	/**
	 * Generates a factory class.
	 * @param module
	 * @return
	 */
	protected GeneratedClass generateFactory(MetaModule module){

		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getFactoryName(module),"The factory for the "+getInterfaceName(module)+" implementation."));
		clazz.setPackageName(getPackageName(module));

		clazz.addImport("java.util.concurrent.atomic.AtomicInteger");
		clazz.addImport(MoskitoInvokationProxy.class);
		clazz.addImport(ServiceStatsCallHandler.class);
		clazz.addImport(ServiceStatsFactory.class);
		clazz.addImport("net.anotheria.asg.service.ASGService");
	    addAdditionalFactoryImports(clazz, module);
	    
	    clazz.setName(getFactoryName(module));
	    clazz.addImport(ServiceFactory.class);
	    clazz.addInterface("ServiceFactory<"+ServiceGenerator.getInterfaceName(module)+">");
	    startClassBody();
	    
	    appendStatement("private static AtomicInteger instanceCounter = new AtomicInteger(0)");
	    appendStatement("private static "+getInterfaceName(module)+" defaultInstance = create"+getServiceName(module)+"()");
	    emptyline();
	    
	    appendString("public "+getInterfaceName(module)+" create(){");
	    increaseIdent();
	    appendStatement("return create"+getServiceName(module)+"()");
	    append(closeBlock());
	    emptyline();
	    
	    appendString("public static "+getInterfaceName(module)+" create"+getServiceName(module)+"(){");
	    increaseIdent();
	    appendString("MoskitoInvokationProxy proxy = new MoskitoInvokationProxy(");
	    increaseIdent();
	    appendString("createInstance(),");
	    appendString("new ServiceStatsCallHandler(),");
	    appendString("new ServiceStatsFactory(),");
	    appendString("\""+getInterfaceName(module)+"-\"+instanceCounter.incrementAndGet(),");
	    appendString(quote("service"),",");
	    appendString(quote(getMoskitoSubsystem()),",");
	    appendString(getSupportedInterfacesList(module));
	    decreaseIdent();
	    appendString(");");
	    appendStatement("return ("+getInterfaceName(module)+") proxy.createProxy()");
	    
	    append(closeBlock());
	    emptyline();
	    
	    appendString("private static "+getInterfaceName(module)+" createInstance(){");
	    increaseIdent();
	    appendString("return "+getImplementationName(module)+".getInstance();");
	    append(closeBlock());
	    emptyline();
	    
	    appendString("static "+getInterfaceName(module)+" getDefaultInstance(){");
	    increaseIdent();
	    appendString("return defaultInstance;");
	    append(closeBlock());
	    return clazz;
	} 
	
	//returns a comma-separated list of all interfaces supported by this impl, which the proxy must map.
	protected String getSupportedInterfacesList(MetaModule module){
		return getInterfaceName(module)+".class"+", ASGService.class";
	}
	
	/**
	 * Returns the base exception name.
	 * @param module
	 * @return
	 */
	protected String getExceptionName(MetaModule module){
		return ServiceGenerator.getExceptionName(module);
	}
	
	protected String getMoskitoSubsystem(){
		return "asg";
	}
	
}
