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

public class XMLExporterGenerator extends AbstractGenerator {
	
	public List<FileEntry> generate(List<MetaModule> modules){
		List<FileEntry> entries = new ArrayList<FileEntry>();
		
		entries.add(new FileEntry(generateExporter(modules)));
		
		return entries;
		
	}

	public static String getExporterClassName(Context context){
		return StringUtils.capitalize(context.getApplicationName())+"XMLExporter";
	}
	
	private GeneratedClass generateExporter(List<MetaModule> modules){

		Context context = GeneratorDataRegistry.getInstance().getContext();
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
		clazz.addImport("java.nio.charset.Charset");
		clazz.addImport("java.util.concurrent.atomic.AtomicLong");

		clazz.addImport("net.anotheria.util.xml.XMLNode");
		clazz.addImport("net.anotheria.util.xml.XMLTree");
		clazz.addImport("net.anotheria.util.xml.XMLAttribute");
		clazz.addImport("net.anotheria.util.xml.XMLWriter");
		
		clazz.addImport("net.anotheria.util.Date");
		clazz.addImport("net.anotheria.util.IOUtils");
		clazz.addImport(StringUtils.class);
		clazz.addImport(ASGRuntimeException.class);
		for (MetaModule m : modules){
			clazz.addImport(ServiceGenerator.getFactoryImport(m));
		}
		
		clazz.setName(getExporterClassName(context));
		
		startClassBody();
		appendStatement("private static AtomicLong exp = new AtomicLong()");
		appendStatement("private static String[] LANGUAGES = null");

		String LANGUAGES = GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() ?
				"LANGUAGES" : "";
		
		appendString("static {");
		increaseIdent();
		appendStatement(ConfiguratorGenerator.getConfiguratorClassName()+".configure()");
		appendStatement("String expLanguages = System.getProperty("+quote("anosite.export.languages")+")");
		appendString("if (expLanguages!=null && expLanguages.length()>0)");
		appendIncreasedStatement("LANGUAGES = StringUtils.tokenize(expLanguages, ',')");
		append(closeBlock());
		emptyline();
		
		
		appendComment("Create an XML Document (ano-util) with data from all modules.");
		appendString("public static XMLTree createCompleteXMLExport() throws ASGRuntimeException{");
		increaseIdent();
		appendStatement("ArrayList<XMLNode> nodes = new ArrayList<XMLNode>()");
		for (MetaModule m : modules){
			String langParam = (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && m.isContainsAnyMultilingualDocs())? LANGUAGES:""; 
			appendStatement("nodes.add("+ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"().exportToXML("+langParam+"))");
		}
		appendStatement("return createExport(nodes)");
		append(closeBlock());
		emptyline();

		appendComment("Write XML data from all modules into given stream.");
		appendString("public static void writeCompleteXMLExportToStream(OutputStream target) throws IOException, ASGRuntimeException{");
		increaseIdent();
		appendStatement("new XMLWriter().write(createCompleteXMLExport(), target)");
		append(closeBlock());
		emptyline();
		
		appendComment("Write XML data from all modules into given file.");
		appendString("public static void writeCompleteXMLExportToFile(File target) throws IOException, ASGRuntimeException{");
		increaseIdent();
		appendStatement("writeToFile(createCompleteXMLExport(), target)");
		append(closeBlock());
		emptyline();

		
		
		//create export methods for all modules.
		for (MetaModule m : modules){
			appendComment("Create an XML Document (jdom) from "+m.getName()+" data for export.");
			appendString("public static XMLTree create"+m.getName()+"XMLExport() throws ASGRuntimeException{");
			increaseIdent();
			appendStatement("ArrayList<XMLNode> nodes = new ArrayList<XMLNode>(1)");
			String langParam = (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && m.isContainsAnyMultilingualDocs())? LANGUAGES:"";
			appendStatement("nodes.add("+ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"().exportToXML("+langParam+"))");
			appendStatement("return createExport(nodes)");
			append(closeBlock());
			emptyline();

			appendComment("Write "+m.getName()+" as XML into given stream.");
			appendString("public static void write"+m.getName()+"XMLExportToStream(OutputStream target) throws IOException, ASGRuntimeException{");
			increaseIdent();
			appendStatement("new XMLWriter().write(create"+m.getName()+"XMLExport(), target)");
			append(closeBlock());
			emptyline();
			
			appendComment("Write "+m.getName()+" as XML into given file.");
			appendString("public static void write"+m.getName()+"XMLExportToFile(File target) throws IOException, ASGRuntimeException{");
			increaseIdent();
			appendStatement("writeToFile(create"+m.getName()+"XMLExport(), target)");
			append(closeBlock());
			emptyline();
		}
		
		//private methods
		appendString("private static void writeToFile(XMLTree tree, File target) throws IOException{");
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

		appendString("private static XMLTree createExport(List<XMLNode> nodes){");
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
		appendString("if (a.length==0)");
		appendIncreasedStatement("interactiveMode(a)");
		appendString("else");
		appendIncreasedStatement("automaticMode(a)");
		append(closeBlock());
		emptyline();
		
		appendString("public static void interactiveMode(String a[]) throws IOException,ASGRuntimeException{");
		increaseIdent();
		appendString("while(true){");
		increaseIdent();
		appendStatement("System.out.println("+quote("Please make your choice:")+")");
		appendStatement("System.out.println("+quote("0 - Quit")+")");
		appendStatement("System.out.println("+quote("1 - Complete export")+")");
		int i=2;
		for (MetaModule m : modules){
			appendStatement("System.out.println("+quote(""+i+" - Export "+m.getName()+" ["+m.getStorageType()+"]")+")");
			i++;
		}
		appendStatement("String myInput = IOUtils.readlineFromStdIn()");
		appendStatement("XMLTree tree = createExportForInput(myInput)");
		appendString("if (tree==null)");
		appendIncreasedStatement("System.exit(0)");
		appendStatement("FileOutputStream fOut = new FileOutputStream(new File(\"export-\"+exp.incrementAndGet()+\".xml\"))");
		appendStatement("OutputStreamWriter writer = new OutputStreamWriter(fOut, Charset.forName("+quote(GeneratorDataRegistry.getInstance().getContext().getEncoding())+"))");
		appendStatement("tree.write(writer)");
		appendStatement("writer.flush()");
		appendStatement("writer.close()");
		append(closeBlock());
		
		append(closeBlock());
		emptyline();
		
		
		appendString("public static void automaticMode(String a[]) throws IOException,ASGRuntimeException{");
		increaseIdent();
		appendStatement("new "+getExporterClassName(context)+"().writeCompleteXMLExportToFile(new File("+quote(context.getApplicationName()+"_export.xml")+"))");
		append(closeBlock());
		emptyline();
		
		
		appendString("public static final XMLTree createExportForInput(String input) throws ASGRuntimeException{");
		increaseIdent();
		appendString("if ("+quote("0")+".equals(input))");
		appendIncreasedStatement("return null");
		
		appendString("if ("+quote("1")+".equals(input))");
		appendIncreasedStatement("return createCompleteXMLExport()");

		//create"+m.getName()+"XMLExport()
		i=2;
		for (MetaModule m : modules){
			appendString("if ("+quote(""+i)+".equals(input))");
			appendIncreasedStatement("return create"+m.getName()+"XMLExport()");
			i++;
		}
		
		appendStatement("throw new RuntimeException("+quote("Unrecognized input: ")+" +input)");

		append(closeBlock());
		return clazz;
		
	}

}
