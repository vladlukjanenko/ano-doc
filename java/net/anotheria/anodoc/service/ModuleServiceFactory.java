package net.anotheria.anodoc.service;

/**
 * Use this factory to obtain IModuleService instances.
 */
public class ModuleServiceFactory {

	private ModuleServiceFactory() {
	}

	/**
	 * Returns a IModuleService instance.
	 */
	public static IModuleService createModuleService(){
		return ModuleServiceImpl.getInstance();
	}
}
