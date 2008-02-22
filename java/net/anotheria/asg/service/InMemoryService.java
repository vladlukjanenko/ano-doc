package net.anotheria.asg.service;

import net.anotheria.asg.exception.ASGRuntimeException;

public interface InMemoryService<T extends ASGService> {
	
	/**
	 * Pairs this instance to another service.  The paired instanceof the service shouldn't be used directly anymore (until unpaired). Pairing is useful if you want 
	 * to perform bulk write operations and let them be written back to the original service. Pairing will force all current data to be thrown away.
	 * @param instance
	 */
	public void pairTo(T instance);

	/**
	 * Unpairs the service. Only previously paired service can be unpaired.
	 * @param instance
	 */
	public void unpair(T instance);
	
	/**
	 * Synchs the data back to the paired instance. Should be called before unpairing.
	 */
	public void synchBack();
	
	
	/**
	 * Reads the data from a given instance. Only possible if unpaired.
	 * @param anInstance
	 */
	public void readFrom(T anInstance) throws ASGRuntimeException;
	
	/**
	 * Synches current data to a service instance. The results are quite unpredictable.
	 * @param anInstance
	 */
	public void synchTo(T anInstance);
	
	/**
	 * Clears all data if the instance is unpaired.
	 */
	public void clear();
	
	
}
