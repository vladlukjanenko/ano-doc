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


public class JDBCPersistenceServiceGenerator extends AbstractGenerator implements IGenerator{
	
	private Context context;
	
	public List<FileEntry> generate(IGenerateable gmodule, Context context){
		
		MetaModule mod = (MetaModule)gmodule;
		
		this.context = context;
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ret.add(new FileEntry(FileEntry.package2path(getPackageName(mod)), getExceptionName(mod), generateException(mod)));
		ret.add(new FileEntry(FileEntry.package2path(getPackageName(mod)), getInterfaceName(mod), generateInterface(mod)));
		ret.add(new FileEntry(FileEntry.package2path(getPackageName(mod)), getFactoryName(mod), generateFactory(mod)));
		ret.add(new FileEntry(FileEntry.package2path(getPackageName(mod)), getImplementationName(mod), generateImplementation(mod)));
		
		return ret;
	}
	
	private String getPackageName(MetaModule m){
		return getPackageName(context, m);
	}
	
	private String generateException(MetaModule module){
		String ret = "";
		
	    ret += CommentGenerator.generateJavaTypeComment(getInterfaceName(module));
	    
	    ret += writeStatement("package "+getPackageName(module));
	    ret += emptyline();
	    ret += writeImport("java.sql.SQLException");
	    ret += writeImport("net.anotheria.db.dao.DAOException");
	    ret += emptyline();

	    ret += writeString("public class "+getExceptionName(module)+ " extends Exception{");
		increaseIdent();
		ret += emptyline();
		ret += writeString("public "+getExceptionName(module)+"(String message){");
		ret += writeIncreasedStatement("super(message)");
		ret += writeString("}");
		
		ret += writeString("public "+getExceptionName(module)+"(SQLException e){");
		ret += writeIncreasedStatement("super("+quote("Undelying DB Error: ")+"+e.getMessage())");
		ret += writeString("}");

		ret += writeString("public "+getExceptionName(module)+"(DAOException e){");
		ret += writeIncreasedStatement("super("+quote("Undelying DAO Error: ")+"+e.getMessage())");
		ret += writeString("}");
		
		ret += closeBlock();
		return ret; 
	}
	
	private String generateInterface(MetaModule module){
	    String ret = "";
	    
	    ret += CommentGenerator.generateJavaTypeComment(getInterfaceName(module));
 
	    ret += writeStatement("package "+getPackageName(module));
	    ret += emptyline();
	    ret += writeImport("java.util.List");
	    ret += emptyline();
	    ret += writeImport("net.anotheria.anodoc.query2.QueryProperty");
	    ret += emptyline();
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    }
	    ret += emptyline();

	    ret += writeString("public interface "+getInterfaceName(module)+"{");
	    increaseIdent();
	    
	    String throwsClause = " throws "+getExceptionName(module);
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        ret += writeComment("Returns all "+doc.getMultiple()+" objects stored.");
	        ret += writeStatement("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause);
	        ret += emptyline();
	        ret += writeComment("Deletes a "+doc.getName()+" object by id.");
	        ret += writeStatement("public void delete"+doc.getName()+"(String id)"+throwsClause);
	        ret += emptyline();
	        ret += writeComment("Returns the "+doc.getName()+" object with the specified id.");
	        ret += writeStatement("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause);
	        ret += emptyline();
	        ret += writeComment("Creates a new "+doc.getName()+" object.\nReturns the created version.");
	        ret += writeStatement("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        ret += emptyline();
	        ret += writeComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.");
	        ret += writeStatement("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        ret += emptyline();
	        ret += writeComment("Updates a "+doc.getName()+" object.\nReturns the updated version.");
	        ret += writeStatement("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        ret += emptyline();
	        ret += writeComment("Returns all "+doc.getName()+" objects which match the given property or properties.");
	        ret += writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty"+"(QueryProperty... properties)"+throwsClause);
	        ret += emptyline();
	    }
	    
//		ret += writeComment("creates an xml element with all contained data.");
//		ret += writeStatement("public Element exportToXML()");
//		ret += emptyline();
	    
	    ret += closeBlock();
	    return ret;
	}
	
	private String getDAOVariableName(MetaDocument doc){
		return doc.getVariableName()+"DAO";
	}
	
	private String generateImplementation(MetaModule module){
	    String ret = "";

		ret += CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The implementation of the "+getInterfaceName(module)+".");

	    ret += writeStatement("package "+getPackageName(module));
	    ret += emptyline();
	    ret += writeImport("java.util.List");
	    ret += writeImport("java.util.ArrayList");
	    ret += writeImport("java.util.Arrays");
		ret += writeImport("net.anotheria.util.sorter.SortType");
		ret += writeImport("net.anotheria.util.Date");
	    ret += writeImport("net.anotheria.anodoc.query2.QueryProperty");
	    ret += emptyline();
	    List<MetaDocument> docs = module.getDocuments();
	    for (MetaDocument doc : docs){
	        ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    }
	    ret += emptyline();
	    ret += writeImport("net.anotheria.db.service.BasePersistenceServiceJDBCImpl");
	    ret += writeImport("net.anotheria.db.dao.DAOException");
	    ret += writeImport("java.sql.Connection");
	    ret += writeImport("java.sql.SQLException");
	    ret += emptyline();
	    
	    ret += writeString("public class "+getImplementationName(module)+" extends BasePersistenceServiceJDBCImpl implements "+getInterfaceName(module)+" {");
	    increaseIdent();
	    ret += writeStatement("private static "+getImplementationName(module)+" instance");
	    ret += emptyline();
	    
	    ret += writeCommentLine("DAO Objects for data access.");
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret += writeStatement("private "+PersistenceServiceDAOGenerator.getDAOName(doc)+" "+getDAOVariableName(doc));
	    }
	    ret += emptyline();
	    
	    ret += writeString("private "+getImplementationName(module)+"(){");
	    increaseIdent();
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
	    
	    ret += writeString("public void init(){");
	    increaseIdent();
	    ret += writeStatement("super.init()");
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret += writeStatement(getDAOVariableName(doc) +" = new "+PersistenceServiceDAOGenerator.getDAOName(doc)+"()");
	    }
	    ret += writeStatement("String currentDAO = null");
	    ret += openTry();
	    ret += writeStatement("Connection c = getConnection()");
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret += writeStatement("log.info(\"Initializing DAO for "+doc.getName()+"\" )");
	        ret += writeStatement("currentDAO = "+quote(doc.getName()));
	        ret += writeStatement(getDAOVariableName(doc)+".init(c)");
	    }
	
	    decreaseIdent();
	    ret += writeString("}catch(DAOException e){");
		ret += writeIncreasedStatement("log.fatal(\"init failed (dao:\"+currentDAO+\") \",e )");
		//ret += writeIncreasedStatement("throw new RuntimeException(\"init failed (dao:\"+currentDAO+\") cause: \"+e.getMessage())");
	    ret += writeString("}catch(SQLException e){");
		ret += writeIncreasedStatement("log.fatal(\"init failed (sql) \",e )");
		//ret += writeIncreasedStatement("throw new RuntimeException(\"init failed (sql) cause: \"+e.getMessage())");
	    ret += writeString("}catch(Exception e){");
	    ret += writeIncreasedStatement("System.out.println(e.getMessage()+\" \"+e.getClass())");
		ret += writeIncreasedStatement("log.fatal(\"init failed (e) \",e )");
		//ret += writeIncreasedStatement("throw new RuntimeException(\"init failed (sql) cause: \"+e.getMessage())");
		ret += writeString("}");
	    ret += closeBlock();
	    ret += emptyline();
	    
	    
	    String throwsClause = " throws "+getExceptionName(module);
	    String callLog = null;
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";

	        callLog = "\"Call get"+doc.getMultiple()+"() \"";
	        ret += writeComment("Returns all "+doc.getMultiple()+" objects stored.");
	        ret += openFun("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause);
	        ret += generateMethodStart(callLog);
	        ret += writeStatement("return "+getDAOVariableName(doc)+".get"+doc.getMultiple()+"(c)");
	        ret += generateMethodEnd(module, callLog);
	        ret += closeBlock();
	        ret += emptyline();

	        callLog = "\"Call delete"+doc.getName()+"(\"+id+\") \"";
	        ret += writeComment("Deletes a "+doc.getName()+" object by id.");
	        ret += openFun("public void delete"+doc.getName()+"(String id)"+throwsClause);
	        ret += generateMethodStart(callLog);
	        ret += writeStatement(getDAOVariableName(doc)+".delete"+doc.getName()+"(c, id)");
	        ret += generateMethodEnd(module, callLog);
	        ret += closeBlock();
	        ret += emptyline();

	        callLog = "\"Call get"+doc.getName()+"(\"+id+\") \"";
	        ret += writeComment("Returns the "+doc.getName()+" object with the specified id.");
	        ret += openFun("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause);
	        ret += generateMethodStart(callLog);
	        ret += writeStatement("return "+getDAOVariableName(doc)+".get"+doc.getName()+"(c, id)");
	        ret += generateMethodEnd(module, callLog);
	        ret += closeBlock();
	        ret += emptyline();

	        callLog = "\"Call create"+doc.getName()+"(\"+"+doc.getVariableName()+"+\") \"";
	        ret += writeComment("Creates a new "+doc.getName()+" object.\nReturns the created version.");
	        ret += openFun("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        ret += generateMethodStart(callLog);
	        ret += writeStatement("return "+getDAOVariableName(doc)+".create"+doc.getName()+"(c, "+doc.getVariableName()+")");
	        ret += generateMethodEnd(module, callLog);
	        ret += closeBlock();
	        ret += emptyline();
	        
	        callLog = "\"Call create"+doc.getMultiple()+"(\"+list+\") \"";
	        ret += writeComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.");
	        ret += openFun("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        ret += generateMethodStart(callLog);
	        ret += writeStatement("return "+getDAOVariableName(doc)+".create"+doc.getMultiple()+"(c, list)");
	        ret += generateMethodEnd(module, callLog);
	        ret += closeBlock();
	        ret += emptyline();
	        

	        callLog = "\"Call update"+doc.getName()+"(\"+"+doc.getVariableName()+"+\") \"";
	        ret += writeComment("Updates a "+doc.getName()+" object.\nReturns the updated version.");
	        ret += openFun("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        ret += generateMethodStart(callLog);
	        ret += writeStatement("return "+getDAOVariableName(doc)+".update"+doc.getName()+"(c, "+doc.getVariableName()+")");
	        ret += generateMethodEnd(module, callLog);
	        ret += closeBlock();
	        ret += emptyline();
	        
	        callLog = "\"Call get"+doc.getMultiple()+"ByProperty(\"+properties+\") \"";
	        ret += writeComment("Returns all "+doc.getName()+" objects which match the given property.");
	        ret += openFun("public "+listDecl+" get"+doc.getMultiple()+"ByProperty"+"(QueryProperty... properties)"+throwsClause);
	        ret += generateMethodStart(callLog);
	        ret += writeStatement("return "+getDAOVariableName(doc)+".get"+doc.getMultiple()+"ByProperty(c, Arrays.asList(properties))");
	        ret += generateMethodEnd(module, callLog);
	        ret += closeBlock();
	        ret += emptyline();
	        
/*	        
	        ret += writeComment("Returns all "+doc.getName()+" objects which match given properties.");
	        ret += writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperties"+"(List<QueryProperty> properties)"+throwsClause);
	        ret += emptyline();
*/	        
	    }
	    

	    
	    //generate export function
	    ret += emptyline();
	    
	    
	    ret += closeBlock();
	    return ret;
	}
	
	private String generateMethodStart(String callLog){
		String ret = "";
		ret += writeStatement("log.debug("+callLog+")");
		ret += writeStatement("Connection c = null");
		ret += openTry();
		ret += writeStatement("c = getConnection()");
		return ret;
	}
	
	private String generateMethodEnd(MetaModule mod, String callLog){
		decreaseIdent();
		String ret = "";
		ret += writeString("}catch(SQLException e){");
		ret += writeIncreasedStatement("log.error(\""+callLog.substring(callLog.indexOf(' ')+1)+",e)");
		ret += writeIncreasedStatement("throw new "+getExceptionName(mod)+"(e)");
		ret += writeString("}catch(DAOException e){");
		ret += writeIncreasedStatement("throw new "+getExceptionName(mod)+"(e)");
		ret += writeString("}finally{");
		ret += writeIncreasedStatement("release(c)");
		ret += writeString("}");
		return ret;
	}

	/**
	 * Generates a factory for the implementation.
	 * @param module
	 * @return
	 */
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

	
	public static final String getExceptionName(MetaModule m){
		return getServiceName(m)+"Exception";
	}
	
	public static String getInterfaceName(MetaModule m){
	    return "I"+getServiceName(m);
	}
	
	public static String getServiceName(MetaModule m){
	    return m.getName()+"PersistenceService";
	}

	public static String getFactoryName(MetaModule m){
	    return getServiceName(m)+"Factory";
	}
	
	public static String getImplementationName(MetaModule m){
	    return getServiceName(m)+"Impl";
	}
	
	public static String getPackageName(Context context, MetaModule module){
		return context.getPackageName(module)+".service.persistence";
	}
	
	public static final String getExceptionImport(Context c, MetaModule m){
		return getPackageName(c, m)+"."+getExceptionName(m);
	}

	public static final String getFactoryImport(Context c, MetaModule m){
		return getPackageName(c, m)+"."+getFactoryName(m);
	}

	public static final String getInterfaceImport(Context c, MetaModule m){
		return getPackageName(c, m)+"."+getInterfaceName(m);
	}
}
