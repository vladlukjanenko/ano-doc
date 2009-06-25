package net.anotheria.asg.service;

import net.anotheria.asg.exception.ASGRuntimeException;
/**
 * A service that holds all the data in memory. An InMemoryService can operate in different modes. Access to the mode definition and configuration is 
 * provided by this interface.
 * @author lrosenberg
 *
 * @param <T>
 */
public interface InMemoryService<T extends ASGService> {
	
	/**
	 * Pairs this instance to another service.  The paired instanceof the service shouldn't be used directly anymore (until unpaired). Pairing is useful if you want 
	 * to perform bulk write operations and let them be written back to the original service. Pairing will force all current data to be thrown away.
	 * @param instance
	 */
	void pairTo(T instance)throws ASGRuntimeException;

	/**
	 * Unpairs the service. Only previously paired service can be unpaired.
	 * @param instance
	 */
	void unpair(T instance)throws ASGRuntimeException;
	
	/**
	 * Synchs the data back to the paired instance. Should be called before unpairing.
	 */
	void synchBack()throws ASGRuntimeException;
	
	
	/**
	 * Reads the data from a given instance. Only possible if unpaired.
	 * @param anInstance
	 */
	void readFrom(T anInstance) throws ASGRuntimeException;
	
	/**
	 * Synches current data to a service instance. The results are quite unpredictable.
	 * @param anInstance
	 */
	void synchTo(T anInstance)throws ASGRuntimeException;
	
	/**
	 * Clears all data if the instance is unpaired.
	 */
	void clear()throws ASGRuntimeException;
	
	
}
