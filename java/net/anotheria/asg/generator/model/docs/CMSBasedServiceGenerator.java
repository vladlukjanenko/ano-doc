package net.anotheria.asg.generator.model.docs;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.DataFacadeGenerator;

public class CMSBasedServiceGenerator extends AbstractGenerator implements IGenerator{
	
	private Context context;
	
	public List<FileEntry> generate(IGenerateable gmodule, Context context){
		
		MetaModule mod = (MetaModule)gmodule;
		this.context = context;
		String packageName = context.getPackageName(mod)+".service";
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		ret.add(new FileEntry(FileEntry.package2path(packageName), getFactoryName(mod), generateFactory(mod)));
		ret.add(new FileEntry(FileEntry.package2path(packageName), getImplementationName(mod), generateImplementation(mod)));
		
		return ret;
	}
	
	private String getPackageName(MetaModule module){
		return context.getPackageName(module)+".service";
	}
	
	private String generateImplementation(MetaModule module){
	    String ret = "";

		ret += CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The implementation of the "+getInterfaceName(module)+".");

		ret += writeStatement("package "+getPackageName(module));
	    ret += emptyline();
	    ret += writeImport("java.util.List");
	    ret += writeImport("java.util.ArrayList");
	    ret += writeImport("net.anotheria.anodoc.data.Property");
	    ret += writeImport("net.anotheria.anodoc.data.NoSuchPropertyException");
		ret += writeImport("net.anotheria.util.sorter.SortType");
		ret += writeImport("net.anotheria.util.sorter.StaticQuickSorter");
		ret += writeImport("net.anotheria.util.Date");
	    ret += writeImport(context.getPackageName(module)+".data."+ module.getModuleClassName());
	    ret += writeImport(context.getServicePackageName(MetaModule.SHARED)+".BasicCMSService");
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
	        ret += writeImport(DocumentGenerator.getDocumentImport(context, doc));
	    }
	    ret += emptyline();
	    ret += writeImport("net.anotheria.asg.util.listener.IServiceListener");
	    ret += writeImport("net.anotheria.anodoc.query2.DocumentQuery");
	    ret += writeImport("net.anotheria.anodoc.query2.QueryResult");
	    ret += writeImport("net.anotheria.anodoc.query2.QueryResultEntry");
	    ret += writeImport("net.anotheria.anodoc.query2.QueryProperty");
	    
	    ret += emptyline();
	    ret += writeImport("org.jdom.Element");
	    ret += emptyline();
	    
	    ret += writeString("public class "+getImplementationName(module)+" extends BasicCMSService implements "+getInterfaceName(module)+" {");
	    increaseIdent();
	    ret += writeStatement("private static "+getImplementationName(module)+" instance");
	    ret += emptyline();
	    
	    ret += writeString("private "+getImplementationName(module)+"(){");
	    increaseIdent();
	    if (module.getListeners().size()>0){
	    	for (int i=0; i<module.getListeners().size(); i++){
	    		String listClassName = (String)module.getListeners().get(i);
	    		ret += writeStatement("addServiceListener(new "+listClassName+"())");
	    	}
	    }
	    ret += closeBlock();
	    ret += emptyline();
	    
	    ret += writeString("static final "+getImplementationName(module)+" getInstance(){");
	    increaseIdent();
	    ret += writeString("if (instance==null){");
	    increaseIdent();
	    ret += writeStatement("instance = new "+getImplementationName(module)+"()");
	    ret += closeBlock();
	    ret += writeStatement("return instance");
	    ret += closeBlock();
	    ret += emptyline();
	    
	    //generate module handling.
	    ret += writeString("private "+module.getModuleClassName()+" "+getModuleGetterCall(module)+"{");
	    increaseIdent();
	    ret += writeStatement("return ("+module.getModuleClassName()+") getModule("+module.getModuleClassName()+".MODULE_ID)");
	    ret += closeBlock();
	    ret += emptyline();
	    
	    boolean containsAnyMultilingualDocs = false;
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        
	        ret += writeString("public "+listDecl+" get"+doc.getMultiple()+"(){");
	        increaseIdent();
	        ret += writeStatement("List "+doc.getMultiple().toLowerCase()+" = "+getModuleGetterCall(module)+".get"+doc.getMultiple()+"()");
	        ret += writeStatement("return "+doc.getMultiple().toLowerCase());
	        ret += closeBlock();
	        ret += emptyline();
	        
			ret += writeString("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType){");
			increaseIdent();
			ret += writeStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"(), sortType)");
			ret += closeBlock();
			ret += emptyline();

	        ret += writeString("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+"){");
	        increaseIdent();
	        ret += writeStatement("delete"+doc.getName()+"("+doc.getVariableName()+".getId())");
	        ret += closeBlock();
	        ret += emptyline();
	        
	        ret += writeString("public void delete"+doc.getName()+"(String id){");
	        increaseIdent();
	        ret += writeStatement(module.getModuleClassName()+" module = "+getModuleGetterCall(module));
	        ret += writeStatement("module.delete"+doc.getName()+"(id)");
	        ret += writeStatement("updateModule(module)");
	        ret += closeBlock();
	        ret += emptyline();

	        ret += writeString("public "+doc.getName()+" get"+doc.getName()+"(String id){");
	        increaseIdent();
	        ret += writeStatement("return "+getModuleGetterCall(module)+".get"+doc.getName()+"(id)");
	        ret += closeBlock();
	        ret += emptyline();
	        
	        ret += writeString("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+"){");
	        increaseIdent();
	        ret += writeStatement(module.getModuleClassName()+" module = "+getModuleGetterCall(module));
	        ret += writeStatement("module.create"+doc.getName()+"(("+DocumentGenerator.getDocumentName(doc)+")"+doc.getVariableName()+")");
	        ret += writeStatement("updateModule(module)");
	        
	        ret += writeString("if (hasServiceListeners()){");
	        increaseIdent();
	        ret += writeStatement("List<IServiceListener> myListeners = getServiceListeners()");
	        ret += writeString("for (int i=0; i<myListeners.size(); i++)");
	        ret += writeIncreasedStatement("myListeners.get(i).documentCreated("+doc.getVariableName()+")");
	        ret += closeBlock();	
	        
	        
	        ret += writeStatement("return "+doc.getVariableName());
	        ret += closeBlock();
	        ret += emptyline();

	        ret += writeString("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+"){");
	        increaseIdent();
	        ret += writeStatement(doc.getName()+" oldVersion = null");
	        ret += writeStatement(module.getModuleClassName()+" module = "+getModuleGetterCall(module));
	        
	        ret += writeString("if (hasServiceListeners())");
	        ret += writeIncreasedStatement("oldVersion = module.get"+doc.getName()+"("+doc.getVariableName()+".getId())");
	        
	        ret += writeStatement("module.update"+doc.getName()+"(("+DocumentGenerator.getDocumentName(doc)+")"+doc.getVariableName()+")");
	        ret += writeStatement("updateModule(module)");
	        
	        ret += writeString("if (hasServiceListeners()){");
	        increaseIdent();
	        ret += writeStatement("List<IServiceListener> myListeners = getServiceListeners()");
	        ret += writeString("for (int i=0; i<myListeners.size(); i++)");
	        ret += writeIncreasedStatement("myListeners.get(i).documentUpdated(oldVersion, "+doc.getVariableName()+")");
	        ret += closeBlock();
	        
	        ret += writeStatement("return "+doc.getVariableName());
	        ret += closeBlock();
	        ret += emptyline();
	        
	        ret += writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value){");
	        increaseIdent();
	        ret += writeStatement(listDecl+" all"+doc.getMultiple()+" = get"+doc.getMultiple()+"()");
	        ret += writeStatement(listDecl+" ret = new ArrayList<"+doc.getName()+">()");
	        ret += writeString("for (int i=0; i<all"+doc.getMultiple()+".size(); i++){");
	        increaseIdent();
	        ret += writeStatement(doc.getName()+" "+doc.getVariableName()+" = all"+doc.getMultiple()+".get(i)");
	        ret += writeString("try{");
	        increaseIdent();
	        ret += writeStatement("Property property = (("+DocumentGenerator.getDocumentName(doc)+")"+doc.getVariableName()+").getProperty(propertyName)");
	        ret += writeStatement("if (property.getValue()==null && value==null){");
	        ret += writeIncreasedStatement("ret.add("+doc.getVariableName()+")");
	        ret += writeString("}else{");
	        increaseIdent();
	        ret += writeString("if (value!=null && property.getValue().equals(value))");
	        ret += writeIncreasedStatement("ret.add("+doc.getVariableName()+")");
	        ret += closeBlock();
	        decreaseIdent();
			ret += writeString("}catch(NoSuchPropertyException nspe){");
			increaseIdent();
			ret += writeString("if (value==null)");
			ret += writeIncreasedStatement("ret.add("+doc.getVariableName()+")");
			decreaseIdent();
	        ret += writeString("}catch(Exception ignored){}");
	        
	        ret += closeBlock();
	        ret += writeString("return ret;");
	        ret += closeBlock();
	        ret += emptyline();
	        
			ret += writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType){");
			increaseIdent();
			ret += writeStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(propertyName, value), sortType)");
			ret += closeBlock();
			
			ret += writeComment("Executes a query on "+doc.getMultiple());
			ret += writeString("public QueryResult executeQueryOn"+doc.getMultiple()+"(DocumentQuery query){");
			increaseIdent();
			ret += writeStatement(listDecl+" all"+doc.getMultiple()+" = get"+doc.getMultiple()+"()");
			ret += writeStatement("QueryResult result = new QueryResult()");
			ret += writeString("for (int i=0; i<all"+doc.getMultiple()+".size(); i++){");
			increaseIdent();
			ret += writeStatement("List<QueryResultEntry> partialResult = query.match(all"+doc.getMultiple()+".get(i))");
			ret += writeStatement("result.add(partialResult)");
			ret += closeBlock();
			
			ret += writeStatement("return result");
			ret += closeBlock();
			ret += emptyline();
			
			ret += writeComment("Returns all "+doc.getName()+" objects, where property matches.");
	        ret += writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(QueryProperty... property){");
	        increaseIdent();
	        ret += writeStatement("throw new RuntimeException(\"Not yet implemented\")");
	        ret += closeBlock();
	        ret += emptyline();
	        
			ret += writeComment("Returns all "+doc.getName()+" objects, where property matches, sorted");
			ret += writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property){");
	        increaseIdent();
	        ret += writeStatement("throw new RuntimeException(\"Not yet implemented\")");
	        ret += closeBlock();
			ret += emptyline();
			
			if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
				containsAnyMultilingualDocs = true;
				ret += writeCommentLine("This method is not very fast, since it makes an update (eff. save) after each doc.");
				ret += writeString("public void copyMultilingualAttributesInAll"+doc.getMultiple()+"(String sourceLanguage, String targetLanguage){");
				increaseIdent();
				ret += writeStatement("List<"+doc.getName()+"> allDocumentsSrc = get"+doc.getMultiple()+"()");
				ret += writeStatement("List<"+doc.getName()+"> allDocuments = new ArrayList<"+doc.getName()+">(allDocumentsSrc.size())");
				ret += writeStatement("allDocuments.addAll(allDocumentsSrc)");
				ret += writeString("for ("+doc.getName()+" document : allDocuments){");
				increaseIdent();
				ret += writeStatement("document.copyLANG2LANG(sourceLanguage, targetLanguage)");
				ret += writeStatement("update"+doc.getName()+"(document)");
				ret += closeBlock();
				ret += closeBlock();
				ret += emptyline();
			
			}
			
			
	    }
	    
	    if (containsAnyMultilingualDocs){
			ret += writeComment("Copies all multilingual fields from sourceLanguage to targetLanguage in all data objects (documents, vo) which are part of this module and managed by this service");
			ret += writeString("public void copyMultilingualAttributesInAllObjects(String sourceLanguage, String targetLanguage){");
			increaseIdent();
			for (MetaDocument doc : docs){
				if (GeneratorDataRegistry.hasLanguageCopyMethods(doc))
					ret += writeStatement("copyMultilingualAttributesInAll"+doc.getMultiple()+"(sourceLanguage, targetLanguage)");
			}
			ret += closeBlock();
			ret += emptyline();
	    }

	    
	    //generate export function
	    ret += emptyline();
	    ret += writeString("public Element exportToXML(){");
	    increaseIdent();
	    ret += writeStatement("return "+getModuleGetterCall(module)+".toXMLElement()");
	    ret += closeBlock();
	    
	    
	    ret += closeBlock();
	    return ret;
	}
	
	private String getModuleGetterMethod(MetaModule module){
	    return "_get"+module.getModuleClassName();	    
	}
	
	private String getModuleGetterCall(MetaModule module){
	    return getModuleGetterMethod(module)+"()";
	}
	

	private String generateFactory(MetaModule module){
	    String ret = "";

		ret += CommentGenerator.generateJavaTypeComment(getFactoryName(module),"The factory for the "+getInterfaceName(module)+" implementation.");

	    ret += writeStatement("package "+getPackageName(module));
	    ret += emptyline();
	    
	    ret += writeString("public class "+getFactoryName(module)+"{");
	    increaseIdent();

	    ret += writeString("public static "+getInterfaceName(module)+" create"+getServiceName(module)+"(){");
	    increaseIdent();
	    ret += writeString("return "+getImplementationName(module)+".getInstance();");
	    ret += closeBlock();
	    
	    ret += closeBlock();
	    return ret;
	}

	

	
	public static String getInterfaceName(MetaModule m){
	    return "I"+getServiceName(m);
	}
	
	public static String getServiceName(MetaModule m){
	    return m.getName()+"Service";
	}

	public static String getFactoryName(MetaModule m){
	    return getServiceName(m)+"Factory";
	}
	
	public static String getImplementationName(MetaModule m){
	    return getServiceName(m)+"Impl";
	}
}
