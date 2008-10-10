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

public class XMLExporterGenerator extends AbstractGenerator{
	
	public List<FileEntry> generate(List<MetaModule> modules, Context context){
		List<FileEntry> entries = new ArrayList<FileEntry>();
		
		entries.add(generateExporter(modules, context));
		
		return entries;
		
	}

	public static String getExporterClassName(Context context){
		return StringUtils.capitalize(context.getApplicationName())+"XMLExporter";
	}
	
	private FileEntry generateExporter(List<MetaModule> modules, Context context){
		String ret = "";
		
		ret += writeStatement("package "+context.getServicePackageName(MetaModule.SHARED));
		
		ret += writeImport("java.util.List");
		ret += writeImport("java.util.ArrayList");
		ret += writeImport("java.io.File");
		ret += writeImport("java.io.FileOutputStream");
		ret += writeImport("java.io.OutputStream");
		ret += writeImport("java.io.IOException");
		ret += writeImport("java.io.OutputStreamWriter");
		ret += writeImport("java.nio.charset.Charset");
		ret += writeImport("java.util.concurrent.atomic.AtomicLong");
		ret += emptyline();
		//ret += writeImport("org.jdom.Element");
		//ret += writeImport("org.jdom.Attribute");
		//ret += writeImport("org.jdom.Document");
		//ret += writeImport("org.jdom.output.XMLOutputter");
		ret += writeImport("net.anotheria.util.xml.XMLNode");
		ret += writeImport("net.anotheria.util.xml.XMLTree");
		ret += writeImport("net.anotheria.util.xml.XMLAttribute");
		ret += writeImport("net.anotheria.util.xml.XMLWriter");
		ret += emptyline();
		ret += writeImport("net.anotheria.util.Date");
		ret += writeImport("net.anotheria.util.IOUtils");
		ret += writeImport("net.anotheria.util.StringUtils");
		ret += writeImport(ASGRuntimeException.class.getName());
		ret += writeImport("org.apache.log4j.BasicConfigurator");
		ret += emptyline();
		for (MetaModule m : modules){
			ret += writeImport(ServiceGenerator.getFactoryImport(context, m));
		}
		
		ret += emptyline();
		ret += writeString("public class "+getExporterClassName(context)+"{");
		increaseIdent();
		ret += emptyline();
		ret += writeStatement("private static AtomicLong exp = new AtomicLong()");
		ret += writeStatement("private static String[] LANGUAGES = null");

		ret += writeString("static {");
		increaseIdent();
		ret += writeStatement(ConfiguratorGenerator.getConfiguratorClassName()+".configure()");
		ret += writeStatement("String expLanguages = System.getProperty("+quote("anosite.export.languages")+")");
		ret += writeString("if (expLanguages!=null && expLanguages.length()>0)");
		ret += writeIncreasedStatement("LANGUAGES = StringUtils.tokenize(expLanguages, ',')");
		ret += closeBlock();
		ret += emptyline();
		
		
		ret += writeComment("Create an XML Document (ano-util) with data from all modules.");
		ret += writeString("public static XMLTree createCompleteXMLExport() throws ASGRuntimeException{");
		increaseIdent();
		ret += writeStatement("ArrayList<XMLNode> nodes = new ArrayList<XMLNode>()");
		for (MetaModule m : modules){
			ret += writeStatement("nodes.add("+ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"().exportToXML(LANGUAGES))");
		}
		ret += writeStatement("return createExport(nodes)");
		ret += closeBlock();
		ret += emptyline();

		ret += writeComment("Write XML data from all modules into given stream.");
		ret += writeString("public static void writeCompleteXMLExportToStream(OutputStream target) throws IOException, ASGRuntimeException{");
		increaseIdent();
		ret += writeStatement("new XMLWriter().write(createCompleteXMLExport(), target)");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeComment("Write XML data from all modules into given file.");
		ret += writeString("public static void writeCompleteXMLExportToFile(File target) throws IOException, ASGRuntimeException{");
		increaseIdent();
		ret += writeStatement("writeToFile(createCompleteXMLExport(), target)");
		ret += closeBlock();
		ret += emptyline();

		
		
		//create export methods for all modules.
		for (MetaModule m : modules){
			ret += writeComment("Create an XML Document (jdom) from "+m.getName()+" data for export.");
			ret += writeString("public static XMLTree create"+m.getName()+"XMLExport() throws ASGRuntimeException{");
			increaseIdent();
			ret += writeStatement("ArrayList<XMLNode> nodes = new ArrayList<XMLNode>(1)");
			ret += writeStatement("nodes.add("+ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"().exportToXML(LANGUAGES))");
			ret += writeStatement("return createExport(nodes)");
			ret += closeBlock();
			ret += emptyline();

			ret += writeComment("Write "+m.getName()+" as XML into given stream.");
			ret += writeString("public static void write"+m.getName()+"XMLExportToStream(OutputStream target) throws IOException, ASGRuntimeException{");
			increaseIdent();
			ret += writeStatement("new XMLWriter().write(create"+m.getName()+"XMLExport(), target)");
			ret += closeBlock();
			ret += emptyline();
			
			ret += writeComment("Write "+m.getName()+" as XML into given file.");
			ret += writeString("public static void write"+m.getName()+"XMLExportToFile(File target) throws IOException, ASGRuntimeException{");
			increaseIdent();
			ret += writeStatement("writeToFile(create"+m.getName()+"XMLExport(), target)");
			ret += closeBlock();
			ret += emptyline();
		}
		
		//private methods
		ret += writeString("private static void writeToFile(XMLTree tree, File target) throws IOException{");
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

		ret += writeString("private static XMLTree createExport(List<XMLNode> nodes){");
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
		ret += writeString("if (a.length==0)");
		ret += writeIncreasedStatement("interactiveMode(a)");
		ret += writeString("else");
		ret += writeIncreasedStatement("automaticMode(a)");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public static void interactiveMode(String a[]) throws IOException,ASGRuntimeException{");
		increaseIdent();
		ret += writeString("while(true){");
		increaseIdent();
		ret += writeStatement("System.out.println("+quote("Please make your choice:")+")");
		ret += writeStatement("System.out.println("+quote("0 - Quit")+")");
		ret += writeStatement("System.out.println("+quote("1 - Complete export")+")");
		int i=2;
		for (MetaModule m : modules){
			ret += writeStatement("System.out.println("+quote(""+i+" - Export "+m.getName()+" ["+m.getStorageType()+"]")+")");
			i++;
		}
		ret += writeStatement("String myInput = IOUtils.readlineFromStdIn()");
		ret += writeStatement("XMLTree tree = createExportForInput(myInput)");
		ret += writeString("if (tree==null)");
		ret += writeIncreasedStatement("System.exit(0)");
		ret += writeStatement("FileOutputStream fOut = new FileOutputStream(new File(\"export-\"+exp.incrementAndGet()+\".xml\"))");
		ret += writeStatement("OutputStreamWriter writer = new OutputStreamWriter(fOut, Charset.forName("+quote(GeneratorDataRegistry.getInstance().getContext().getEncoding())+"))");
		ret += writeStatement("tree.write(writer)");
		ret += writeStatement("writer.flush()");
		ret += writeStatement("writer.close()");
		ret += closeBlock();
		
		ret += closeBlock();
		ret += emptyline();
		
		
		ret += writeString("public static void automaticMode(String a[]) throws IOException,ASGRuntimeException{");
		increaseIdent();
		ret += writeStatement("new "+getExporterClassName(context)+"().writeCompleteXMLExportToFile(new File("+quote(context.getApplicationName()+"_export.xml")+"))");
		ret += closeBlock();
		ret += emptyline();
		
		
		ret += writeString("public static final XMLTree createExportForInput(String input) throws ASGRuntimeException{");
		increaseIdent();
		ret += writeString("if ("+quote("0")+".equals(input))");
		ret += writeIncreasedStatement("return null");
		
		ret += writeString("if ("+quote("1")+".equals(input))");
		ret += writeIncreasedStatement("return createCompleteXMLExport()");

		//create"+m.getName()+"XMLExport()
		i=2;
		for (MetaModule m : modules){
			ret += writeString("if ("+quote(""+i)+".equals(input))");
			ret += writeIncreasedStatement("return create"+m.getName()+"XMLExport()");
			i++;
		}
		
		ret += writeStatement("throw new RuntimeException("+quote("Unrecognized input: ")+" +input)");

		ret += closeBlock();
		ret += emptyline();
		ret += closeBlock();
		

		return new FileEntry(FileEntry.package2path(context.getServicePackageName(MetaModule.SHARED)), getExporterClassName(context),ret);
		
	}

}
