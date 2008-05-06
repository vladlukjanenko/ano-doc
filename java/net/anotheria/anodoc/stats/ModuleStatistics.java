package net.anotheria.anodoc.stats;

import java.util.Enumeration;

import net.anotheria.anodoc.data.Module;
import net.anotheria.anodoc.data.NoSuchDocumentException;

/**
 * This module stores statistical information internally collected by the 
 * bgldoc framework in order to calculate storage usage (quotas) and 
 * statistical values provided by the modules.
 */
public class ModuleStatistics extends Module{
	
	public static final String _CREATED_ = "_created_";
	public static final String _LAST_UPDATE_ = "_lastupdate_";
	
	public static final long DEF_QUOTA = 4*1024*1024; //4MB
	
	
	private static final long serialVersionUID = 1260845651991002811L;
	
	public ModuleStatistics(){
		super(IStatisticsConstants.MODULE_STATISTICS);
		updateStatisticValue(_CREATED_, System.currentTimeMillis());
	}
	
	public void updateSizeValue(String anId, long aValue){
		updateValue(getSizeId(anId), aValue);
	}
	
	public long getSizeValue(String anId){
		return getStatisticsEntry(getSizeId(anId)).getCount();
	}
	
	
	public void updateStatisticValue(String statId, long count){
		String myId = getStatId(statId);
		updateValue(myId, count);
		//furchtbarer hack
		if (!statId.equals("counter"))
			updateValue(_LAST_UPDATE_, System.currentTimeMillis());
	}
	
	
	public long getStatisticValue(String anId){
		return getStatisticsEntry(getStatId(anId)).getCount();
	}
	
	private void updateValue(String anId, long aValue){
		StatisticsEntry entry = getStatisticsEntry(anId);
		entry.setCount(aValue);
		updateStatisticsEntry(entry);
	}
	
	private StatisticsEntry getStatisticsEntry(String statId){
		try{
			return (StatisticsEntry) getDocument(statId);
		}catch(NoSuchDocumentException e){
			return new StatisticsEntry(statId);
		}
	}
	
	private void updateStatisticsEntry(StatisticsEntry entry){
		putDocument(entry);
	}
	
	public long getCreationDate(){
		return getStatisticValue(_CREATED_);
	}
	
	public long getLastUpdate(){
		return getStatisticsEntry(_LAST_UPDATE_).getCount();
	}
	
	private String getStatId(String anId){
		return IStatisticsConstants._PRE_STAT + anId;
	}
	
	private String getSizeId(String anId){
		return IStatisticsConstants._PRE_SIZE + anId;
	}
	
	@SuppressWarnings("unchecked")
	public long getFutureCumulativeSize(String exchangedId, long exchangedSize){
		String eId = getSizeId(exchangedId);
		Enumeration allDocNames = getHolderNames();
		long sum = exchangedSize;
		while (allDocNames.hasMoreElements()){
			String aKey = (String)allDocNames.nextElement();
			if (aKey.startsWith(IStatisticsConstants._PRE_SIZE) &&
				(!aKey.equals(eId))){
				sum += getStatisticsEntry(aKey).getCount();
			}
		}
		return sum;
	}
	
	public long getCumulativeSize(){
		return getFutureCumulativeSize("",0);
	}
	
	public long getQuota(){
		return DEF_QUOTA;
	}
}
