package net.anotheria.asg.metafactory;

import net.anotheria.asg.service.ASGService;

public interface ServiceFactory<T extends ASGService> {
	public T create();
}
