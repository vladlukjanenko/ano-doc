package net.anotheria.anodoc.stats;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.Module;
import net.anotheria.anodoc.service.AbstractModuleFactory;

/**
 * The factory for StatisticsEntry and ModuleStatistics.
 */
public class StatisticsFactory extends AbstractModuleFactory{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @see biz.beaglesoft.bgldoc.service.IModuleFactory#createDocument(String)
	 */
	public Document createDocument(String name) {
		return new StatisticsEntry(name);
	}

	/**
	 * @see biz.beaglesoft.bgldoc.service.IModuleFactory#recreateModule(String, String)
	 */
	public Module recreateModule(String ownerId, String copyId) {
		return new ModuleStatistics();
	}

}
