package net.anotheria.asg.generator;

import java.util.ArrayList;
import java.util.List;

public class GeneratedClass {
	
	public static final String CRLF = AbstractGenerator.CRLF;
	
	private List<String> imports;
	private List<String> interfaces;
	
	private StringBuilder body;
	
	private String name;
	private String parent;
	
	private String packageName;
	
	private String typeComment;
	
	private TypeOfClass type = TypeOfClass.getDefault();
	
	private boolean generateLogger = false;
	private boolean abstractClass = false;
	
	public boolean isAbstractClass() {
		return abstractClass;
	}

	public void setAbstractClass(boolean abstractClass) {
		this.abstractClass = abstractClass;
	}

	public GeneratedClass(){
		body = new StringBuilder();
		
		imports = new ArrayList<String>();
		interfaces = new ArrayList<String>();
	}
	
	public String createClassFileContent(){
		StringBuilder ret = new StringBuilder(body.length()+200);
		
		if (typeComment!=null && typeComment.length()>0)
			ret.append(getTypeComment());
		ret.append("package "+getPackageName()+";");
		ret.append(CRLF).append(CRLF);
		
		for (String imp : imports){
			ret.append("import ").append(imp).append(";").append(CRLF);
		}
		
		ret.append(CRLF);
		
		String nameDeclaration = "public "+(isAbstractClass()?"abstract ":"")+type.toJava()+" "+getName();
		if (getParent()!=null && getParent().length()>0)
			nameDeclaration += " extends "+getParent();
		if (interfaces!=null && interfaces.size()>0){
			nameDeclaration += " implements ";
			for (int i=0; i<interfaces.size(); i++){
				if (i>0)
					nameDeclaration += ", ";
				nameDeclaration += interfaces.get(i);
			}
		}
			
		
		ret.append(nameDeclaration).append("{");
		ret.append(CRLF).append(CRLF);
		
		if (generateLogger){
			ret.append("\tprivate static Logger log = Logger.getLogger("+getName()+".class);").append(CRLF).append(CRLF);
		}
		
		
		ret.append(getBody());
		ret.append("}").append(CRLF);
		
		return ret.toString();
	}

	public void addInterface(String anInterface){
		interfaces.add(anInterface);
	}
	
	public void addImport(Class<?> clazz){
		addImport(clazz.getName());
	}
	
	public void addImport(String anImport){
		if (!imports.contains(anImport))
			imports.add(anImport);
	}
	
	public List<String> getImports() {
		return imports;
	}

	public void setImports(List<String> imports) {
		this.imports = imports;
	}

	public List<String> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<String> interfaces) {
		this.interfaces = interfaces;
	}

	public StringBuilder getBody() {
		return body;
	}

	public void setBody(StringBuilder body) {
		this.body = body;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name.indexOf('{')!=-1)
			System.err.println("Warning, illegal name: "+name);
		this.name = name;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getTypeComment() {
		return typeComment;
	}

	public void setTypeComment(String typeComment) {
		this.typeComment = typeComment;
	}

	public TypeOfClass getType() {
		return type;
	}

	public void setType(TypeOfClass type) {
		this.type = type;
	}

	public boolean isGenerateLogger() {
		return generateLogger;
	}

	public void setGenerateLogger(boolean generateLogger) {
		if (generateLogger)
			addImport("org.apache.log4j.Logger");
		this.generateLogger = generateLogger;
	}

}
