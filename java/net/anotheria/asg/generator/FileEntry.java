package net.anotheria.asg.generator;

import net.anotheria.util.StringUtils;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class FileEntry {
	private String path;
	private String name;
	private String content;
	private String type;
	
	public FileEntry(){
		type = ".java";
	}
	
	public FileEntry(String aPath, String aName, String aContent){
		this();
		this.path = aPath;
		this.name = aName;
		this.content = aContent;
	}
	
	
	 
	/**
	 * @return
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param string
	 */
	public void setContent(String string) {
		content = string;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string
	 */
	public void setPath(String string) {
		path = string;
	}
	
	public String toString(){
		return path+"/"+name;
	}
	
	public static String package2path(String packageName){
		String ret = StringUtils.replace(packageName, '.', '/');
		ret = "java/"+ret;
		return ret; 
	}

	/**
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param string
	 */
	public void setType(String string) {
		type = string;
	}

}
