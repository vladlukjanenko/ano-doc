package net.anotheria.asg.generator.apputil;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.exception.ASGRuntimeException;
import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.ConfiguratorGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
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
		
		/*ret += writeImport("net.anotheria.anodoc.service.IModuleFactory");
		ret += writeImport("net.anotheria.anodoc.service.IModuleService");
		ret += writeImport("net.anotheria.anodoc.service.ModuleServiceFactory");
		ret += writeImport("net.anotheria.anodoc.util.CommonHashtableModuleStorage");
		ret += emptyline();
		ret += writeImport(context.getPackageName()+".data.*");
		*/
		
		ret += writeImport("java.util.List");
		ret += writeImport("java.util.ArrayList");
		ret += writeImport("java.io.File");
		ret += writeImport("java.io.FileOutputStream");
		ret += writeImport("java.io.OutputStream");
		ret += writeImport("java.io.IOException");
		ret += emptyline();
		ret += writeImport("org.jdom.Element");
		ret += writeImport("org.jdom.Attribute");
		ret += writeImport("org.jdom.Document");
		ret += writeImport("org.jdom.output.XMLOutputter");
		ret += emptyline();
		ret += writeImport("net.anotheria.util.Date");
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
		
		ret += writeString("public "+getExporterClassName(context)+"(){");
		increaseIdent();
		ret += writeStatement(ConfiguratorGenerator.getConfiguratorClassName()+".configure()");
		ret += closeBlock();
		ret += emptyline();
		
		
		ret += writeComment("Create an XML Document (jdom) with data from all modules.");
		ret += writeString("public Document createCompleteXMLExport() throws ASGRuntimeException{");
		increaseIdent();
		ret += writeStatement("ArrayList<Element> elements = new ArrayList<Element>()");
		for (MetaModule m : modules){
			ret += writeStatement("elements.add("+ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"().exportToXML())");
		}
		ret += writeStatement("return createExport(elements)");
		ret += closeBlock();
		ret += emptyline();

		ret += writeComment("Write XML data from all modules into given stream.");
		ret += writeString("public void writeCompleteXMLExportToStream(OutputStream target) throws IOException, ASGRuntimeException{");
		increaseIdent();
		ret += writeStatement("new XMLOutputter().output(createCompleteXMLExport(), target)");
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
			ret += writeString("public Document create"+m.getName()+"XMLExport() throws ASGRuntimeException{");
			increaseIdent();
			ret += writeStatement("ArrayList<Element> elements = new ArrayList<Element>(1)");
			ret += writeStatement("elements.add("+ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"().exportToXML())");
			ret += writeStatement("return createExport(elements)");
			ret += closeBlock();
			ret += emptyline();

			ret += writeComment("Write "+m.getName()+" as XML into given stream.");
			ret += writeString("public void write"+m.getName()+"XMLExportToStream(OutputStream target) throws IOException, ASGRuntimeException{");
			increaseIdent();
			ret += writeStatement("new XMLOutputter().output(create"+m.getName()+"XMLExport(), target)");
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
		ret += writeString("private void writeToFile(Document doc, File target) throws IOException{");
		increaseIdent();
		ret += writeStatement("FileOutputStream fOut = null");
		ret += writeString("try{");
		increaseIdent();
		ret += writeStatement("fOut = new FileOutputStream(target)");
		ret += writeStatement("XMLOutputter outputter = new XMLOutputter()");
		ret += writeStatement("outputter.output(doc, fOut)");
		ret += writeStatement("fOut.flush()");
		ret += writeStatement("fOut.close()");
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

		ret += writeString("private Document createExport(List<Element> elements){");
		increaseIdent();
		ret += writeStatement("Document doc = new Document()");
		ret += writeStatement("Element root = new Element("+quote("export")+")");
		ret += writeStatement("root.setAttribute(new Attribute("+quote("timestamp")+", \"\"+System.currentTimeMillis()))");
		ret += writeStatement("root.setAttribute(new Attribute("+quote("date")+", Date.currentDate().toString()))");
		ret += writeStatement("doc.setRootElement(root)");
		ret += writeStatement("System.out.println(\"Warning, xml export disabled\")");
		ret += writeStatement("//root.setChildren(elements)");
		ret += writeStatement("return doc");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public static void main(String[] a) throws IOException,ASGRuntimeException{");
		increaseIdent();
		ret += writeStatement("BasicConfigurator.configure()");
		ret += writeStatement("new "+getExporterClassName(context)+"().writeCompleteXMLExportToFile(new File("+quote(context.getApplicationName()+"_export.xml")+"))");
		ret += closeBlock();

		
		ret += closeBlock();
		

		return new FileEntry(FileEntry.package2path(context.getServicePackageName(MetaModule.SHARED)), getExporterClassName(context),ret);
		
	}

}
