package net.anotheria.anodoc.util.context;

public abstract class CallContext {
	
	private String currentLanguage;
	
	public void reset(){
		currentLanguage = null;
	}

	public String getCurrentLanguage() {
		return currentLanguage == null ? getDefaultLanguage() : currentLanguage;
	}

	public void setCurrentLanguage(String currentLanguage) {
		this.currentLanguage = currentLanguage;
	}
	
	public abstract String getDefaultLanguage();
		
	

}

