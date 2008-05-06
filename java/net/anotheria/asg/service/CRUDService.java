package net.anotheria.asg.service;

import net.anotheria.asg.exception.ASGRuntimeException;

public interface CRUDService<T> {
	public T get(String id) throws ASGRuntimeException;
	
	public void delete(T t) throws ASGRuntimeException;
	
	public T update(T t) throws ASGRuntimeException;
	
	public T create(T t) throws ASGRuntimeException;

}
