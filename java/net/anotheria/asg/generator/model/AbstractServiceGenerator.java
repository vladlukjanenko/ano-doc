package net.anotheria.asg.generator.model;


import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;

public class AbstractServiceGenerator extends AbstractGenerator{
	public static final String getInterfaceName(MetaModule m){
	    return "I"+getServiceName(m);
	}
	
	public static final String getServiceName(MetaModule m){
	    return m.getName()+"Service";
	}

	public static final String getFactoryName(MetaModule m){
	    return getServiceName(m)+"Factory";
	}
	
	public static final String getImplementationName(MetaModule m){
	    return getServiceName(m)+"Impl";
	}

	protected String getPackageName(MetaModule module){
		return GeneratorDataRegistry.getInstance().getContext().getServicePackageName(module);
	}

	protected String generateFactory(MetaModule module){
	    String ret = "";

		ret += CommentGenerator.generateJavaTypeComment(getFactoryName(module),"The factory for the "+getInterfaceName(module)+" implementation.");

	    ret += writeStatement("package "+getPackageName(module));
	    ret += emptyline();
	    
	    ret += writeImport("java.util.concurrent.atomic.AtomicInteger");
	    ret += writeImport("net.java.dev.moskito.core.dynamic.MoskitoInvokationProxy");
	    ret += writeImport("net.java.dev.moskito.core.predefined.ServiceStatsCallHandler");
	    ret += writeImport("net.java.dev.moskito.core.predefined.ServiceStatsFactory");
	    ret += emptyline();

	    
	    ret += writeString("public class "+getFactoryName(module)+"{");
	    increaseIdent();
	    ret += emptyline();
	    ret += writeStatement("private static AtomicInteger instanceCounter = new AtomicInteger(0)");
	    ret += emptyline();
	    
	    ret += writeString("public static "+getInterfaceName(module)+" create"+getServiceName(module)+"(){");
	    increaseIdent();
	    ret += writeString("MoskitoInvokationProxy proxy = new MoskitoInvokationProxy(");
	    increaseIdent();
	    ret += writeString("createInstance(),");
	    ret += writeString("new ServiceStatsCallHandler(),");
	    ret += writeString("new ServiceStatsFactory(),");
	    ret += writeString("\""+getInterfaceName(module)+"-\"+instanceCounter.incrementAndGet(),");
	    ret += writeString("\"service\",");
	    ret += writeString("\"asg\",");
	    ret += writeString(getInterfaceName(module)+".class");
	    decreaseIdent();
	    ret += writeString(");");
	    ret += writeStatement("return ("+getInterfaceName(module)+") proxy.createProxy()");
	    
	    ret += closeBlock();
	    ret += emptyline();
	    
	    ret += writeString("public static "+getInterfaceName(module)+" createInstance(){");
	    increaseIdent();
	    ret += writeString("return "+getImplementationName(module)+".getInstance();");
	    ret += closeBlock();
	    
	    ret += closeBlock();
	    return ret;
	}
}

/*


public IBe2IntegrationService createService(){
	MoskitoInvokationProxy proxy = new MoskitoInvokationProxy(
		createStub(),
		new ServiceStatsCallHandler(),
		new ServiceStatsFactory(),
		"Be2IntegrationServiceStub-"+instanceCounter.incrementAndGet(),
		"stub",
		"default",
		IBe2IntegrationService.class
		);
	return (IBe2IntegrationService) proxy.createProxy();
}
}
*/