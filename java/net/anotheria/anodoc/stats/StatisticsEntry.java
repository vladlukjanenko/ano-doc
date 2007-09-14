package net.anotheria.anodoc.stats;

import net.anotheria.anodoc.data.Document;

/**
 * This document holds the value for exactly one object.
 */
public class StatisticsEntry extends Document{
	
	public static final String PROP_STAT_COUNT = "count";
	
	private static final long serialVersionUID = -7648950634435613395L;
	
	public StatisticsEntry(String entryId){
		super(entryId);
		setCount(0L);
	}
	
	public void setCount(long count){
		setLong(PROP_STAT_COUNT, count);
	}
	
	public long getCount(){
		return getLong(PROP_STAT_COUNT);
	}
}
