package net.anotheria.anodoc.util.context;

/**
 * A factory for call context creation. You may supply your own CallContextFactory implementation to provide your own CallContexts.
 * @author another
 *
 */
public interface CallContextFactory {
	/**
	 * Creates a new context.
	 * @return
	 */
	CallContext createContext();
}
