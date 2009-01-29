package net.anotheria.asg.generator;


public class GeneratedJSPFile extends GeneratedArtefact{

	
	private StringBuilder body;
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

	public StringBuilder getBody() {
		return body;
	}
	
	public void setPackage(String aPackage){
		path = FileEntry.package2path(aPackage);
	}

}
