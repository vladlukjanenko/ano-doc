package net.anotheria.anodoc.util.context;

import java.io.Serializable;
import java.util.List;

/**
 * Abstract class for CallContext which is assigned to each thread and allows multilinguality support and more.
 * @author another
 *
 */
public abstract class CallContext implements Serializable{
	
	private String currentAuthor;
	private String currentLanguage;
	private DBContext dbContext; 
	
	public void reset(){
		currentLanguage = null;
		dbContext = null;
	}

	public String getCurrentLanguage() {
		return currentLanguage == null ? getDefaultLanguage() : currentLanguage;
	}

	public void setCurrentLanguage(String currentLanguage) {
		this.currentLanguage = currentLanguage;
	}
	
	public abstract String getDefaultLanguage();
	
	public abstract List<String> getSupportedLanguages();
	
	public void setDbContext(DBContext aDbContent){
		dbContext = aDbContent;
	}
	
	public DBContext getDbContext(){
		//this is not really thread-safe, but its better to risk to create one dummy object as to 
		//synchronize here. Especially since the CallContext is ThreadLocal and therefore shouldn't 
		//be accessed from many threads at once.
		if (dbContext==null)
			dbContext = new DBContext();
		return dbContext;
	}

	public String getCurrentAuthor() {
		return currentAuthor;
	}

	public void setCurrentAuthor(String currentAuthor) {
		this.currentAuthor = currentAuthor;
	}
}

