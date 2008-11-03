package net.anotheria.asg.generator.apputil;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.exception.ASGRuntimeException;
import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.ConfiguratorGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.util.StringUtils;

public class XMLImporterGenerator extends AbstractGenerator{
	
	public List<FileEntry> generate(List<MetaModule> modules, Context context){
		List<FileEntry> entries = new ArrayList<FileEntry>();
		
		entries.add(generateImporter(modules, context));
		
		return entries;
		
	}

	public static String getImporterClassName(Context context){
		return StringUtils.capitalize(context.getApplicationName())+"XMLImporter";
	}
	
	private String generateDocumentParser(){
		String ret = "";
		ret += writeString("public static Document parseDocument(String content) throws JDOMException, IOException{");
		increaseIdent();
		ret += writeStatement("SAXBuilder reader = new SAXBuilder();");
		ret += writeStatement("reader.setValidation(false)");
		ret += writeStatement("Document doc = reader.build(new StringReader(content))");
		ret += writeStatement("return doc");
		
		ret += closeBlock();
		return ret;

	}
	
	private FileEntry generateImporter(List<MetaModule> modules, Context context){
		String ret = "";
		
		ret += writeStatement("package "+context.getServicePackageName(MetaModule.SHARED));
		
		ret += writeImport("java.util.List");
		ret += writeImport("java.util.ArrayList");
		ret += writeImport("java.io.File");
		ret += writeImport("java.io.FileOutputStream");
		ret += writeImport("java.io.OutputStream");
		ret += writeImport("java.io.IOException");
		ret += writeImport("java.io.OutputStreamWriter");
		ret += emptyline();
		
		ret += writeImport("org.jdom.Element");
		ret += writeImport("org.jdom.Attribute");
		ret += writeImport("org.jdom.Document");


		ret += writeImport("java.io.StringReader");
		ret += writeImport("org.jdom.JDOMException");
		ret += writeImport("org.jdom.input.SAXBuilder");

		ret += writeImport("net.anotheria.util.xml.XMLNode");
		ret += writeImport("net.anotheria.util.xml.XMLTree");
		ret += writeImport("net.anotheria.util.xml.XMLAttribute");
		ret += writeImport("net.anotheria.util.xml.XMLWriter");
		ret += emptyline();
		ret += writeImport("net.anotheria.util.Date");
		ret += writeImport(ASGRuntimeException.class.getName());
		ret += writeImport("org.apache.log4j.BasicConfigurator");
		ret += emptyline();
		for (MetaModule m : modules){
			ret += writeImport(ServiceGenerator.getFactoryImport(context, m));
		}
		
		ret += emptyline();
		ret += writeString("public class "+getImporterClassName(context)+"{");
		increaseIdent();
		ret += emptyline();
		
		ret += writeString("public "+getImporterClassName(context)+"(){");
		increaseIdent();
		ret += writeStatement(ConfiguratorGenerator.getConfiguratorClassName()+".configure()");
		ret += closeBlock();
		ret += emptyline();
		
		
		//NEW
		ret += generateDocumentParser();
		ret += emptyline();
		
		//OLD
		
		ret += writeComment("Create an XML Document (ano-util) with data from all modules.");
		ret += writeString("public XMLTree createCompleteXMLExport() throws ASGRuntimeException{");
		increaseIdent();
		ret += writeStatement("ArrayList<XMLNode> nodes = new ArrayList<XMLNode>()");
		for (MetaModule m : modules){
			ret += writeStatement("nodes.add("+ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"().exportToXML())");
		}
		ret += writeStatement("return createExport(nodes)");
		ret += closeBlock();
		ret += emptyline();

		ret += writeComment("Write XML data from all modules into given stream.");
		ret += writeString("public void writeCompleteXMLExportToStream(OutputStream target) throws IOException, ASGRuntimeException{");
		increaseIdent();
		ret += writeStatement("new XMLWriter().write(createCompleteXMLExport(), target)");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeComment("Write XML data from all modules into given file.");
		ret += writeString("public void writeCompleteXMLExportToFile(File target) throws IOException, ASGRuntimeException{");
		increaseIdent();
		ret += writeStatement("writeToFile(createCompleteXMLExport(), target)");
		ret += closeBlock();
		ret += emptyline();

		
		
		//create export methods for all modules.
		for (MetaModule m : modules){
			ret += writeComment("Create an XML Document (jdom) from "+m.getName()+" data for export.");
			ret += writeString("public XMLTree create"+m.getName()+"XMLExport() throws ASGRuntimeException{");
			increaseIdent();
			ret += writeStatement("ArrayList<XMLNode> nodes = new ArrayList<XMLNode>(1)");
			ret += writeStatement("nodes.add("+ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"().exportToXML())");
			ret += writeStatement("return createExport(nodes)");
			ret += closeBlock();
			ret += emptyline();

			ret += writeComment("Write "+m.getName()+" as XML into given stream.");
			ret += writeString("public void write"+m.getName()+"XMLExportToStream(OutputStream target) throws IOException, ASGRuntimeException{");
			increaseIdent();
			ret += writeStatement("new XMLWriter().write(create"+m.getName()+"XMLExport(), target)");
			ret += closeBlock();
			ret += emptyline();
			
			ret += writeComment("Write "+m.getName()+" as XML into given file.");
			ret += writeString("public void write"+m.getName()+"XMLExportToFile(File target) throws IOException, ASGRuntimeException{");
			increaseIdent();
			ret += writeStatement("writeToFile(create"+m.getName()+"XMLExport(), target)");
			ret += closeBlock();
			ret += emptyline();
		}
		
		//private methods
		ret += writeString("private void writeToFile(XMLTree tree, File target) throws IOException{");
		increaseIdent();
		ret += writeStatement("FileOutputStream fOut = null");
		ret += writeString("try{");
		increaseIdent();
		ret += writeStatement("fOut = new FileOutputStream(target)");
		ret += writeStatement("XMLWriter writer = new XMLWriter()");
		ret += writeStatement("OutputStreamWriter oWriter = writer.write(tree, fOut)");
		ret += writeStatement("oWriter.close()");
		decreaseIdent();
		ret += writeStatement("}catch(IOException e){");
		increaseIdent();
		ret += writeString("if (fOut!=null){");
		increaseIdent();
		ret += writeString("try{");
		ret += writeIncreasedStatement("fOut.close()");
		ret += writeString("}catch(IOException ignored){}");
		ret += closeBlock();
		ret += writeStatement("throw e");
		ret += closeBlock();
		ret += closeBlock();
		ret += emptyline();

		ret += writeString("private XMLTree createExport(List<XMLNode> nodes){");
		increaseIdent();
		ret += writeStatement("XMLTree tree = new XMLTree()");
		ret += writeStatement("tree.setEncoding("+quote(GeneratorDataRegistry.getInstance().getContext().getEncoding())+")");
		ret += writeStatement("XMLNode root = new XMLNode("+quote("export")+")");
		ret += writeStatement("root.addAttribute(new XMLAttribute("+quote("timestamp")+", \"\"+System.currentTimeMillis()))");
		ret += writeStatement("root.addAttribute(new XMLAttribute("+quote("date")+", Date.currentDate().toString()))");
		ret += writeStatement("tree.setRoot(root)");
		ret += writeStatement("root.setChildren(nodes)");
		ret += writeStatement("return tree");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public static void main(String[] a) throws IOException,ASGRuntimeException{");
		increaseIdent();
		ret += writeStatement("BasicConfigurator.configure()");
		ret += writeStatement("new "+getImporterClassName(context)+"().writeCompleteXMLExportToFile(new File("+quote(context.getApplicationName()+"_export.xml")+"))");
		ret += closeBlock();

		
		ret += closeBlock();
		

		return new FileEntry(FileEntry.package2path(context.getServicePackageName(MetaModule.SHARED)), getImporterClassName(context),ret);
		
	}

}
