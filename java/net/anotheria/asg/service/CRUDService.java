package net.anotheria.asg.service;

import net.anotheria.asg.exception.ASGRuntimeException;

/**
 * The interface for a basic crud service.
 * @author lrosenberg
 *
 * @param <T>
 */
public interface CRUDService<T> {
	/**
	 * Returns an instance of T with the given id.
	 * @param id the id of the object.
	 * @return
	 * @throws ASGRuntimeException
	 */
	T get(String id) throws ASGRuntimeException;
	/**
	 * Deletes the object.
	 * @param t the object to delete.
	 * @throws ASGRuntimeException
	 */
	void delete(T t) throws ASGRuntimeException;
	
	/**
	 * Updates an object.
	 * @param t the object to update.
	 * @return
	 * @throws ASGRuntimeException
	 */
	T update(T t) throws ASGRuntimeException;
	
	/**
	 * Creates a new T.
	 * @param t
	 * @return
	 * @throws ASGRuntimeException
	 */
	T create(T t) throws ASGRuntimeException;
}
