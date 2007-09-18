package net.anotheria.anodoc.util.context;


public abstract class CallContext {
	
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
}

