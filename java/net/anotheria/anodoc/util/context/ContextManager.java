package net.anotheria.anodoc.util.context;

public class ContextManager {
	
	private static CallContextFactory factory;
	
	private static ThreadLocal<CallContext> callContext = new ThreadLocal<CallContext>(){
		@Override
		protected synchronized CallContext initialValue(){
			return factory.createContext();
		}
	};
	
	public static CallContext getCallContext(){
		return callContext.get();
	}
	
	public static void setCallContext(CallContext value){
		callContext.set(value);
	}
	

	public static CallContextFactory getFactory() {
		return factory;
	}

	public static void setFactory(CallContextFactory factory) {
		ContextManager.factory = factory;
	}
}

