package net.anotheria.asg.generator;

import net.anotheria.util.StringUtils;

/**
 * Generation result.
 * @author lrosenberg
 */
public class FileEntry {
	/**
	 * The path to store the file into.
	 */
	private String path;
	/**
	 * The name of the file.
	 */
	private String name;
	/**
	 * The content of the file.
	 */
	private String content;
	/**
	 * The type of the file. The type of the file is the extension.
	 */
	private String type;
	
	public FileEntry(){
		type = ".java";
	}
	
	public FileEntry(String aPath, String aName, String aContent, String aType){
		this();
		this.path = aPath;
		this.name = aName;
		this.content = aContent;
		type = aType;
	}
	
	public FileEntry(String aPath, String aName, String aContent){
		this();
		this.path = aPath;
		this.name = aName;
		this.content = aContent;
	}
	
	public FileEntry(GeneratedArtefact artefact){
		this();
		if (artefact!=null){
			path = artefact.getPath();
			name = artefact.getName();
			type = artefact.getFileType();
			content = artefact.createFileContent();
		}
	}
	
	public String getContent() {
		return content;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public void setContent(String string) {
		content = string;
	}

	public void setName(String string) {
		name = string;
	}

	public void setPath(String string) {
		path = string;
	}
	
	@Override public String toString(){
		return path+"/"+name;
	}
	
	/**
	 * Resolves a package name to a full path including sources dir.
	 * @param packageName 
	 * @return full path of the package
	 */
	public static String package2fullPath(String packageName){
		return "java/" + package2path(packageName); 
	}
	
	/**
	 * Resolves a package name to a path.
	 * @param packageName 
	 * @return path of package
	 */
	public static String package2path(String packageName){
		return StringUtils.replace(packageName, '.', '/'); 
	}

	/**
	 * @return type of the file
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets type of the file.
	 * @param string type of file to set
	 */
	public void setType(String string) {
		type = string;
	}

}
