package net.anotheria.asg.metafactory;

import net.anotheria.asg.service.ASGService;

/**
 * Factory definition for service factory.
 * @author lrosenberg
 */
public interface ServiceFactory<T extends ASGService> {
	T create();
}
