package net.anotheria.asg.util.listener;

import net.anotheria.asg.data.DataObject;
import net.anotheria.asg.util.CmsChangesTracker.Action;
/**
 * 
 * @author 
 */
public class DocumentChangedListener implements IServiceListener{

	
	@Override public void documentCreated(DataObject doc) {
	}

    @Override
    public void documentImported(DataObject doc) {
    }

    @Override public void documentDeleted(DataObject doc) {
	}

	@Override public void documentUpdated(DataObject oldVersion, DataObject newVersion) {
		trackChanges(newVersion, Action.UPDATE);
	}
	
	private void trackChanges(DataObject doc, Action action){
		//CmsChangesTracker.saveChange ....
	}
}
