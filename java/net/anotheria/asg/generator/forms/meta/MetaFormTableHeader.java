package net.anotheria.asg.generator.forms.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaFormTableHeader {
	private String width;
	private String key;
	
	public MetaFormTableHeader(){
		
	}
	
	public MetaFormTableHeader(String aKey, String aWidth){
		key = aKey;
		width = aWidth;
	}

	/**
	 * @return
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return
	 */
	public String getWidth() {
		return width;
	}

	/**
	 * @param string
	 */
	public void setKey(String string) {
		key = string;
	}

	/**
	 * @param string
	 */
	public void setWidth(String string) {
		width = string;
	}
	
	public String toString(){
		return key+" "+width;
	}

}
