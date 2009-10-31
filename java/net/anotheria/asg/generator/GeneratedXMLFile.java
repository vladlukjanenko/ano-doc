package net.anotheria.asg.generator;

/**
 * Represents a generated XML file.
 * @author lrosenberg
 */
public class GeneratedXMLFile extends GeneratedArtefact{

	/**
	 * Body of the artefact.
	 */
	private StringBuilder body;
	/**
	 * Path to the generated file.
	 */
	private String path;
	/**
	 * Encoding of the generated file.
	 */
	private String encoding;
	
	/**
	 * Creates a new artefact.
	 */
	public GeneratedXMLFile(){
		body = new StringBuilder();
	}
	
	/**
	 * Creates a new artefact.
	 */
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
