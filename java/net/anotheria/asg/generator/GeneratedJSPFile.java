package net.anotheria.asg.generator;

/**
 * Represents a generated jsp file.
 * @author lrosenberg
 */
public class GeneratedJSPFile extends GeneratedArtefact{

	/**
	 * Body of the artefact.
	 */
	private StringBuilder body;
	/**
	 * Path of the artefact.
	 */
	private String path;
	
	
	public GeneratedJSPFile(){
		body = new StringBuilder();
	}
	
	@Override
	public String createFileContent() {
		StringBuilder ret = new StringBuilder(body.length());
		ret.append(getBody());
		return ret.toString();
	}

	@Override
	public String getFileType() {
		return ".jsp";
	}

	@Override
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override public StringBuilder getBody() {
		return body;
	}
	
	public void setPackage(String aPackage){
		path = FileEntry.package2fullPath(aPackage);
	}
}
