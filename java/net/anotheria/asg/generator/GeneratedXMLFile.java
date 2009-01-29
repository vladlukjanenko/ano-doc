package net.anotheria.asg.generator;


public class GeneratedXMLFile extends GeneratedArtefact{

	
	private StringBuilder body;
	private String path;
	
	private String encoding;
	
	public GeneratedXMLFile(){
		body = new StringBuilder();
	}
	
	public GeneratedXMLFile(String aName, String anEncoding){
		this();
		setName(aName);
		encoding = anEncoding;
	}
	
	@Override
	public String createFileContent() {
		StringBuilder ret = new StringBuilder(body.length());
		ret.append("<?xml version=\"1.0\" encoding=\""+getEncoding()+"\" ?>"+CRLF);
		ret.append(getBody());
		return ret.toString();
	}

	@Override
	public String getFileType() {
		return ".xml";
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

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
}
