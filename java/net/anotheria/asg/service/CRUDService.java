package net.anotheria.asg.service;

public interface CRUDService<T> {
	public T get(String id);
	
	public void delete(T t);
	
	public T update(T t);
	
	public T create(T t);

}
