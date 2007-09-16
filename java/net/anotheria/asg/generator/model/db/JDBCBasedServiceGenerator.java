package net.anotheria.asg.generator.model.db;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.DataFacadeGenerator;

public class JDBCBasedServiceGenerator extends AbstractGenerator implements IGenerator{
	
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
	
	private String getPackageName(MetaModule mod){
		return context.getPackageName(mod)+".service";
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
		ret += writeImport("net.anotheria.util.Date");
	    ret += writeImport(context.getTopPackageName()+".BasicService");
	    ret += emptyline();
	    ret += writeImport(JDBCPersistenceServiceGenerator.getInterfaceImport(context, module));
	    ret += writeImport(JDBCPersistenceServiceGenerator.getFactoryImport(context, module));
	    ret += writeImport(JDBCPersistenceServiceGenerator.getExceptionImport(context, module));

	    List docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
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
	    
	    ret += writeString("public class "+getImplementationName(module)+" extends BasicService implements "+getInterfaceName(module)+" {");
	    increaseIdent();
	    ret += writeStatement("private static "+getImplementationName(module)+" instance");
	    ret += emptyline();
	    
	    ret += writeStatement("private "+JDBCPersistenceServiceGenerator.getInterfaceName(module)+" pService");
	    
	    ret += writeString("private "+getImplementationName(module)+"(){");
	    increaseIdent();
	    ret += writeStatement("pService = "+JDBCPersistenceServiceGenerator.getFactoryName(module)+".create"+JDBCPersistenceServiceGenerator.getServiceName(module)+"()");
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
	    

	    //TODO exceptions im interface einfuehren!
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        
	        ret += writeString("public "+listDecl+" get"+doc.getMultiple()+"(){");
	        increaseIdent();
	        ret += openTry();
	        ret += writeStatement("return pService.get"+doc.getMultiple()+"()");
	        decreaseIdent();
	        ret += writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        ret += writeIncreasedStatement("throw new RuntimeException(\"Persistence failed: \"+e.getMessage())");
	        ret += writeString("}");
	        ret += closeBlock();
	        ret += emptyline();
	        
			ret += writeString("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType){");
			increaseIdent();
			ret += writeStatement("return sorter.sort(get"+doc.getMultiple()+"(), sortType)");
			ret += closeBlock();
			ret += emptyline();

	        ret += writeString("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+"){");
	        increaseIdent();
	        ret += writeStatement("delete"+doc.getName()+"("+doc.getVariableName()+".getId())");
	        ret += closeBlock();
	        ret += emptyline();
	        
	        ret += writeString("public void delete"+doc.getName()+"(String id){");
	        increaseIdent();
	        ret += openTry();
	        ret += writeStatement("pService.delete"+doc.getName()+"(id)");
	        decreaseIdent();
	        ret += writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        ret += writeIncreasedStatement("throw new RuntimeException(\"Persistence failed: \"+e.getMessage())");
	        ret += writeString("}");
	        ret += closeBlock();
	        ret += emptyline();

	        ret += writeString("public "+doc.getName()+" get"+doc.getName()+"(String id){");
	        increaseIdent();
	        ret += openTry();
	        ret += writeStatement("return pService.get"+doc.getName()+"(id)");
	        decreaseIdent();
	        ret += writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        ret += writeIncreasedStatement("throw new RuntimeException(\"Persistence failed: \"+e.getMessage())");
	        ret += writeString("}");
	        ret += closeBlock();
	        ret += emptyline();
	        
	        ret += writeString("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+"){");
	        increaseIdent();
	        ret += openTry();
	        ret += writeStatement(doc.getVariableName() + " = pService.create"+doc.getName()+"("+doc.getVariableName()+")");
	        decreaseIdent();
	        ret += writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        ret += writeIncreasedStatement("throw new RuntimeException(\"Persistence failed: \"+e.getMessage())");
	        ret += writeString("}");
	        ret += writeString("if (hasServiceListeners()){");
	        increaseIdent();
	        ret += writeStatement("List myListeners = getServiceListeners()");
	        ret += writeString("for (int i=0; i<myListeners.size(); i++)");
	        ret += writeIncreasedStatement("((IServiceListener)myListeners.get(i)).documentCreated("+doc.getVariableName()+")");
	        ret += closeBlock();	
	        ret += writeStatement("return "+doc.getVariableName());
	        ret += closeBlock();
	        ret += emptyline();

	        ret += writeString("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+"){");
	        increaseIdent();
	        ret += writeStatement(doc.getName()+" oldVersion = null");
	        ret += openTry();
	        ret += writeString("if (hasServiceListeners())");
	        ret += writeIncreasedStatement("oldVersion = pService.get"+doc.getName()+"("+doc.getVariableName()+".getId())");
	        
	        ret += writeStatement(doc.getVariableName()+" = pService.update"+doc.getName()+"("+doc.getVariableName()+")");
	        decreaseIdent();
	        ret += writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
	        ret += writeIncreasedStatement("throw new RuntimeException(\"Persistence failed: \"+e.getMessage())");
	        ret += writeString("}");
	        
	        
	        ret += writeString("if (hasServiceListeners()){");
	        increaseIdent();
	        ret += writeStatement("List myListeners = getServiceListeners()");
	        ret += writeString("for (int i=0; i<myListeners.size(); i++)");
	        ret += writeIncreasedStatement("((IServiceListener)myListeners.get(i)).documentUpdated(oldVersion, "+doc.getVariableName()+")");
	        ret += closeBlock();
	        
	        ret += writeStatement("return "+doc.getVariableName());
	        ret += closeBlock();
	        ret += emptyline();
	        
	        ret += writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value){");
	        increaseIdent();
			ret += writeStatement("QueryProperty p = new QueryProperty(propertyName, value)");
			ret += writeString("try{");
			ret += writeIncreasedStatement("return pService.get"+doc.getMultiple()+"ByProperty(p)");
			ret += writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){");
			ret += writeIncreasedStatement("throw new RuntimeException(\"Persistence failed: \"+e.getMessage())");
			ret += writeString("}");
			
/*
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
	*/      ret += closeBlock();
	        ret += emptyline();
	        
			ret += writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType){");
			increaseIdent();
			ret += writeStatement("return sorter.sort(get"+doc.getMultiple()+"ByProperty(propertyName, value), sortType)");
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
			
	    }
	    
	    //generate export function
	    ret += emptyline();
	    ret += writeString("public Element exportToXML(){");
	    increaseIdent();
        ret += writeCommentLine("TODO not implemented");
        ret += writeStatement("throw new RuntimeException(\"+not yet implemented\")");
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
