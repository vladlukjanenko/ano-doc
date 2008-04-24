package net.anotheria.asg.generator.model;


import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;

public class AbstractServiceGenerator extends AbstractGenerator{
	public static String getInterfaceName(MetaModule m){
	    return "I"+getServiceName(m);
	}
	
	public static final String getServiceName(MetaModule m){
	    return m.getName()+"Service";
	}

	public String getFactoryName(MetaModule m){
	    return getServiceName(m)+"Factory";
	}
	
	public String getImplementationName(MetaModule m){
	    return getServiceName(m)+"Impl";
	}

	protected String getPackageName(MetaModule module){
		return GeneratorDataRegistry.getInstance().getContext().getServicePackageName(module);
	}

	protected String writeAdditionalFactoryImports(MetaModule module){
		return "";
	}
	
	protected String generateFactory(MetaModule module){

		startNewJob();
		append(CommentGenerator.generateJavaTypeComment(getFactoryName(module),"The factory for the "+getInterfaceName(module)+" implementation."));

	    appendStatement("package "+getPackageName(module));
	    append(emptyline());
	    
	    appendImport("java.util.concurrent.atomic.AtomicInteger");
	    appendImport("net.java.dev.moskito.core.dynamic.MoskitoInvokationProxy");
	    appendImport("net.java.dev.moskito.core.predefined.ServiceStatsCallHandler");
	    appendImport("net.java.dev.moskito.core.predefined.ServiceStatsFactory");
	    appendImport("net.anotheria.asg.service.ASGService");
	    append(writeAdditionalFactoryImports(module));
	    append(emptyline());
	    
	    
	    
	    appendString("public class "+getFactoryName(module)+"{");
	    increaseIdent();
	    append(emptyline());
	    appendStatement("private static AtomicInteger instanceCounter = new AtomicInteger(0)");
	    append(emptyline());
	    
	    appendString("public static "+getInterfaceName(module)+" create"+getServiceName(module)+"(){");
	    increaseIdent();
	    appendString("MoskitoInvokationProxy proxy = new MoskitoInvokationProxy(");
	    increaseIdent();
	    appendString("createInstance(),");
	    appendString("new ServiceStatsCallHandler(),");
	    appendString("new ServiceStatsFactory(),");
	    appendString("\""+getInterfaceName(module)+"-\"+instanceCounter.incrementAndGet(),");
	    appendString(quote("service"),",");
	    appendString("\"asg\",");
	    appendString(getSupportedInterfacesList(module));
	    decreaseIdent();
	    appendString(");");
	    appendStatement("return ("+getInterfaceName(module)+") proxy.createProxy()");
	    
	    append(closeBlock());
	    append(emptyline());
	    
	    appendString("public static "+getInterfaceName(module)+" createInstance(){");
	    increaseIdent();
	    appendString("return "+getImplementationName(module)+".getInstance();");
	    append(closeBlock());
	    
	    append(closeBlock());
	    return getCurrentJobContent().toString();
	}
	
	//returns a comma-separated list of all interfaces supported by this impl, which the proxy must map.
	protected String getSupportedInterfacesList(MetaModule module){
		return getInterfaceName(module)+".class"+", ASGService.class";
	}
	
	protected String getExceptionName(MetaModule module){
		return ServiceGenerator.getExceptionName(module);
	}
	
}
