package net.anotheria.asg.generator.apputil;

import net.anotheria.asg.exception.ASGRuntimeException;
import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.ConfiguratorGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class XMLImporterGenerator extends AbstractGenerator{
	
	public List<FileEntry> generate(List<MetaModule> modules, Context context){
		List<FileEntry> entries = new ArrayList<FileEntry>();
		
		entries.add(generateImporter(modules, context));
		
		return entries;
		
	}

	public static String getImporterClassName(Context context){
		return StringUtils.capitalize(context.getApplicationName())+"XMLImporter";
	}
	
	private void generateDocumentParser(){
		appendString("public static Document parseDocument(String content) throws JDOMException, IOException{");
		increaseIdent();
		appendStatement("SAXBuilder reader = new SAXBuilder();");
		appendStatement("reader.setValidation(false)");
		appendStatement("Document doc = reader.build(new StringReader(content))");
		appendStatement("return doc");
		append(closeBlock());
	}
	
	private FileEntry generateImporter(List<MetaModule> modules, Context context){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(context.getServicePackageName(MetaModule.SHARED));
		
		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		clazz.addImport("java.io.File");
		clazz.addImport("java.io.FileOutputStream");
		clazz.addImport("java.io.OutputStream");
		clazz.addImport("java.io.IOException");
		clazz.addImport("java.io.OutputStreamWriter");
		
		clazz.addImport("org.jdom.Element");
		clazz.addImport("org.jdom.Attribute");
		clazz.addImport("org.jdom.Document");


		clazz.addImport("java.io.StringReader");
		clazz.addImport("org.jdom.JDOMException");
		clazz.addImport("org.jdom.input.SAXBuilder");

		clazz.addImport("net.anotheria.util.xml.XMLNode");
		clazz.addImport("net.anotheria.util.xml.XMLTree");
		clazz.addImport("net.anotheria.util.xml.XMLAttribute");
		clazz.addImport("net.anotheria.util.xml.XMLWriter");
		clazz.addImport("net.anotheria.util.Date");
		clazz.addImport(ASGRuntimeException.class.getName());
		emptyline();
		for (MetaModule m : modules){
			clazz.addImport(ServiceGenerator.getFactoryImport(m));
		}
		
		emptyline();
		clazz.setName(getImporterClassName(context));

		startClassBody();
		appendString("public "+getImporterClassName(context)+"(){");
		increaseIdent();
		appendStatement(ConfiguratorGenerator.getConfiguratorClassName()+".configure()");
		append(closeBlock());
		emptyline();
		
		
		//NEW
		generateDocumentParser();
		emptyline();
		
		//OLD
		
		appendComment("Create an XML Document (ano-util) with data from all modules.");
		appendString("public XMLTree createCompleteXMLExport() throws ASGRuntimeException{");
		increaseIdent();
		appendStatement("ArrayList<XMLNode> nodes = new ArrayList<XMLNode>()");
		for (MetaModule m : modules){
			appendStatement("nodes.add("+ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"().exportToXML())");
		}
		appendStatement("return createExport(nodes)");
		append(closeBlock());
		emptyline();

		appendComment("Write XML data from all modules into given stream.");
		appendString("public void writeCompleteXMLExportToStream(OutputStream target) throws IOException, ASGRuntimeException{");
		increaseIdent();
		appendStatement("new XMLWriter().write(createCompleteXMLExport(), target)");
		append(closeBlock());
		emptyline();
		
		appendComment("Write XML data from all modules into given file.");
		appendString("public void writeCompleteXMLExportToFile(File target) throws IOException, ASGRuntimeException{");
		increaseIdent();
		appendStatement("writeToFile(createCompleteXMLExport(), target)");
		append(closeBlock());
		emptyline();

		
		
		//create export methods for all modules.
		for (MetaModule m : modules){
			appendComment("Create an XML Document (jdom) from "+m.getName()+" data for export.");
			appendString("public XMLTree create"+m.getName()+"XMLExport() throws ASGRuntimeException{");
			increaseIdent();
			appendStatement("ArrayList<XMLNode> nodes = new ArrayList<XMLNode>(1)");
			appendStatement("nodes.add("+ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"().exportToXML())");
			appendStatement("return createExport(nodes)");
			append(closeBlock());
			emptyline();

			appendComment("Write "+m.getName()+" as XML into given stream.");
			appendString("public void write"+m.getName()+"XMLExportToStream(OutputStream target) throws IOException, ASGRuntimeException{");
			increaseIdent();
			appendStatement("new XMLWriter().write(create"+m.getName()+"XMLExport(), target)");
			append(closeBlock());
			emptyline();
			
			appendComment("Write "+m.getName()+" as XML into given file.");
			appendString("public void write"+m.getName()+"XMLExportToFile(File target) throws IOException, ASGRuntimeException{");
			increaseIdent();
			appendStatement("writeToFile(create"+m.getName()+"XMLExport(), target)");
			append(closeBlock());
			emptyline();
		}
		
		//private methods
		appendString("private void writeToFile(XMLTree tree, File target) throws IOException{");
		increaseIdent();
		appendStatement("FileOutputStream fOut = null");
		appendString("try{");
		increaseIdent();
		appendStatement("fOut = new FileOutputStream(target)");
		appendStatement("XMLWriter writer = new XMLWriter()");
		appendStatement("OutputStreamWriter oWriter = writer.write(tree, fOut)");
		appendStatement("oWriter.close()");
		decreaseIdent();
		appendStatement("}catch(IOException e){");
		increaseIdent();
		appendString("if (fOut!=null){");
		increaseIdent();
		appendString("try{");
		appendIncreasedStatement("fOut.close()");
		appendString("}catch(IOException ignored){}");
		append(closeBlock());
		appendStatement("throw e");
		append(closeBlock());
		append(closeBlock());
		emptyline();

		appendString("private XMLTree createExport(List<XMLNode> nodes){");
		increaseIdent();
		appendStatement("XMLTree tree = new XMLTree()");
		appendStatement("tree.setEncoding("+quote(GeneratorDataRegistry.getInstance().getContext().getEncoding())+")");
		appendStatement("XMLNode root = new XMLNode("+quote("export")+")");
		appendStatement("root.addAttribute(new XMLAttribute("+quote("timestamp")+", \"\"+System.currentTimeMillis()))");
		appendStatement("root.addAttribute(new XMLAttribute("+quote("date")+", Date.currentDate().toString()))");
		appendStatement("tree.setRoot(root)");
		appendStatement("root.setChildren(nodes)");
		appendStatement("return tree");
		append(closeBlock());
		emptyline();
		
		appendString("public static void main(String[] a) throws IOException,ASGRuntimeException{");
		increaseIdent();
		appendStatement("new "+getImporterClassName(context)+"().writeCompleteXMLExportToFile(new File("+quote(context.getApplicationName()+"_export.xml")+"))");
		append(closeBlock());

		return new FileEntry(clazz);
		
	}

}
