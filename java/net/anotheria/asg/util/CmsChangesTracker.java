package net.anotheria.asg.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class CmsChangesTracker {

	private CmsChangesTracker() {
	}

	public static int TRACING_SIZE = 20;
	
	public static enum Action{
		CREATE,
		UPDATE,
		IMPORT,
		DELETE;
		public String getShortName(){
			return name().charAt(0) + "";
		}
	}
	
	private static List<DocumentChange> history = new CopyOnWriteArrayList<DocumentChange>();
	
	public static void saveChange(DocumentChange documentChanges){
		history.add(0,documentChanges);
		if(history.size() > TRACING_SIZE)
			history.remove(history.size() - 1);
	}
	
	public static Collection<DocumentChange> getChanges(){
		return history;
	}
	
}
