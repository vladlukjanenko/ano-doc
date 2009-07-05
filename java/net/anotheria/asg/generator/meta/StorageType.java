package net.anotheria.asg.generator.meta;

/**
 * Types of storage supported by the generator.
 * @author lrosenberg
 *
 */
public enum StorageType {
	/**
	 * CMS - AnoDoc baked storage in files.
	 */
	CMS,
	/**
	 * Storage in a jdbc-driven db.
	 */
	DB,
	/**
	 * Federation, no real storage, but a layer on top of another storages. Useful for generalization.
	 */
	FEDERATION,
}
