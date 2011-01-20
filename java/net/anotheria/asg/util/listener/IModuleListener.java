package net.anotheria.asg.util.listener;

import net.anotheria.anodoc.data.Module;

/**
 * A listener for module.
 * @author dsilenko
 */
public interface IModuleListener {

	/**
	 * Called if module changed.
	 * @param module changed module.
	 */
	void moduleLoaded(Module module);
}
