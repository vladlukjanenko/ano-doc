package net.anotheria.asg.util;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class CmsChangesTracker {

	public static enum Action{
		CREATE,
		UPDATE,
		DELETE;
		public String getShortName(){
			return name().charAt(0) + "";
		}
	}
	
	public static class DocumentChange{
		String userName;
		String documentName;
		Action action;
		long timestamp;
		//add cahnge properties
	}
	
	private static Queue<DocumentChange> history = new ConcurrentLinkedQueue<DocumentChange>();
	
	public static void saveChange(String userName, String documentName, Action action, long timestamp){
		//create new change in the history
//		history.offer()
		history.offer();
	}
	
	
	public static Collection<DocumentChange> getChanges(){
		//return changes history
		return null;
	}
	
}
