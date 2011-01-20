package net.anotheria.asg.util.listener;

import net.anotheria.asg.data.DataObject;
/**
 * A Service Listener which simply prints out all created/updated/deleted documents.
 * @author lrosenberg
 */
public class SysOutServiceListener implements IServiceListener{

	@Override public void documentCreated(DataObject doc) {
		System.out.println("Created new document of type: "+doc.getClass()+" : "+doc);
	}

    @Override
    public void documentImported(DataObject doc) {
       System.out.println("Imported document of type: "+doc.getClass()+" : "+doc);
    }

    @Override public void documentDeleted(DataObject doc) {
		System.out.println("Deleted document of type: "+doc.getClass()+" : "+doc);
	}

	@Override public void documentUpdated(DataObject oldVersion, DataObject newVersion) {
		System.out.println("Updated a document of type: "+oldVersion.getClass()+" old: "+oldVersion+" new: "+newVersion);		
	}

	@Override public void persistenceChanged() {
		System.out.println("Persistence changed");
	}

}
