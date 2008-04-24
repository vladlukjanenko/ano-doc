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
		StringBuilder ret = new StringBuilder(5000);
		
	    ret.append(CommentGenerator.generateJavaTypeComment(getInterfaceName(module)));
	    
	    ret.append(writeStatement("package "+getPackageName(module)));
	    ret.append(emptyline());
	    ret.append(writeImport("java.sql.SQLException"));
	    ret.append(writeImport("net.anotheria.db.dao.DAOException"));
	    ret.append(emptyline());

	    ret.append(writeString("public class "+getExceptionName(module)+ " extends Exception{"));
		increaseIdent();
		ret.append(emptyline());
		ret.append(writeString("public "+getExceptionName(module)+"(String message){"));
		ret.append(writeIncreasedStatement("super(message)"));
		ret.append(writeString("}"));
		
		ret.append(writeString("public "+getExceptionName(module)+"(SQLException e){"));
		ret.append(writeIncreasedStatement("super("+quote("Undelying DB Error: ")+"+e.getMessage())"));
		ret.append(writeString("}"));

		ret.append(writeString("public "+getExceptionName(module)+"(DAOException e){"));
		ret.append(writeIncreasedStatement("super("+quote("Undelying DAO Error: ")+"+e.getMessage())"));
		ret.append(writeString("}"));
		
		ret.append(closeBlock());
		return ret.toString(); 
	}
	
	private String generateInterface(MetaModule module){
		StringBuilder ret = new StringBuilder(5000);
	    
	    ret.append(CommentGenerator.generateJavaTypeComment(getInterfaceName(module)));
 
	    ret.append(writeStatement("package "+getPackageName(module)));
	    ret.append(emptyline());
	    ret.append(writeImport("java.util.List"));
	    ret.append(emptyline());
	    ret.append(writeImport("net.anotheria.anodoc.query2.QueryProperty"));
	    ret.append(emptyline());
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret.append(writeImport(DataFacadeGenerator.getDocumentImport(context, doc)));
	    }
	    ret.append(emptyline());

	    ret.append(writeString("public interface "+getInterfaceName(module)+"{"));
	    increaseIdent();
	    
	    String throwsClause = " throws "+getExceptionName(module);
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        ret.append(writeComment("Returns all "+doc.getMultiple()+" objects stored."));
	        ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause));
	        ret.append(emptyline());
	        ret.append(writeComment("Deletes a "+doc.getName()+" object by id."));
	        ret.append(writeStatement("public void delete"+doc.getName()+"(String id)"+throwsClause));
	        ret.append(emptyline());
	        ret.append(writeComment("Returns the "+doc.getName()+" object with the specified id."));
	        ret.append(writeStatement("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause));
	        ret.append(emptyline());
	        ret.append(writeComment("Imports a new "+doc.getName()+" object.\nReturns the imported version."));
	        ret.append(writeStatement("public "+doc.getName()+" import"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause));
	        ret.append(emptyline());
	        ret.append(writeComment("Creates a new "+doc.getName()+" object.\nReturns the created version."));
	        ret.append(writeStatement("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause));
	        ret.append(emptyline());
	        ret.append(writeComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions."));
	        ret.append(writeStatement("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause));
	        ret.append(emptyline());
	        ret.append(writeComment("Updates a "+doc.getName()+" object.\nReturns the updated version."));
	        ret.append(writeStatement("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause));
	        ret.append(emptyline());
	        ret.append(writeComment("Updates multiple "+doc.getName()+" object.\nReturns the updated versions."));
	        ret.append(writeStatement("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause));
	        ret.append(emptyline());
	        ret.append(writeComment("Returns all "+doc.getName()+" objects which match the given property or properties."));
	        ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty"+"(QueryProperty... properties)"+throwsClause));
	        ret.append(emptyline());
	    }
	    
//		ret.append(writeComment("creates an xml element with all contained data."));
//		ret.append(writeStatement("public Element exportToXML()"));
//		ret.append(emptyline());
	    
	    ret.append(closeBlock());
	    return ret.toString();
	}
	
	private String getDAOVariableName(MetaDocument doc){
		return doc.getVariableName()+"DAO";
	}
	
	private String generateImplementation(MetaModule module){
		StringBuilder ret = new StringBuilder(5000);

		ret.append(CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The implementation of the "+getInterfaceName(module)+"."));

	    ret.append(writeStatement("package "+getPackageName(module)));
	    ret.append(emptyline());
	    ret.append(writeImport("java.util.List"));
	    ret.append(writeImport("java.util.ArrayList"));
	    ret.append(writeImport("java.util.Arrays"));
		ret.append(writeImport("net.anotheria.util.sorter.SortType"));
		ret.append(writeImport("net.anotheria.util.Date"));
	    ret.append(writeImport("net.anotheria.anodoc.query2.QueryProperty"));
	    ret.append(emptyline());
	    List<MetaDocument> docs = module.getDocuments();
	    for (MetaDocument doc : docs){
	        ret.append(writeImport(DataFacadeGenerator.getDocumentImport(context, doc)));
	    }
	    ret.append(emptyline());
	    ret.append(writeImport("net.anotheria.db.service.BasePersistenceServiceJDBCImpl"));
	    ret.append(writeImport("net.anotheria.db.dao.DAOException"));
	    ret.append(writeImport("java.sql.Connection"));
	    ret.append(writeImport("java.sql.SQLException"));
	    ret.append(emptyline());
	    
	    ret.append(writeString("public class "+getImplementationName(module)+" extends BasePersistenceServiceJDBCImpl implements "+getInterfaceName(module)+" {"));
	    increaseIdent();
	    ret.append(writeStatement("private static "+getImplementationName(module)+" instance"));
	    ret.append(emptyline());
	    
	    ret.append(writeCommentLine("DAO Objects for data access."));
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret.append(writeStatement("private "+PersistenceServiceDAOGenerator.getDAOName(doc)+" "+getDAOVariableName(doc)));
	    }
	    ret.append(emptyline());
	    
	    ret.append(writeString("private "+getImplementationName(module)+"(){"));
	    increaseIdent();
	    ret.append(closeBlock());
	    ret.append(emptyline());
	    
	    ret.append(writeString("static final "+getImplementationName(module)+" getInstance(){"));
	    increaseIdent();
	    ret.append(writeString("if (instance==null){"));
	    increaseIdent();
	    ret.append(writeStatement("instance = new "+getImplementationName(module)+"()"));
	    ret.append(closeBlock());
	    ret.append(writeStatement("return instance"));
	    ret.append(closeBlock());
	    ret.append(emptyline());
	    
	    ret.append(writeString("public void init(){"));
	    increaseIdent();
	    ret.append(writeStatement("super.init()"));
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret.append(writeStatement(getDAOVariableName(doc) +" = new "+PersistenceServiceDAOGenerator.getDAOName(doc)+"()"));
	    }
	    ret.append(writeStatement("String currentDAO = null"));
	    ret.append(openTry());
	    ret.append(writeStatement("Connection c = getConnection()"));
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret.append(writeStatement("log.info(\"Initializing DAO for "+doc.getName()+"\" )"));
	        ret.append(writeStatement("currentDAO = "+quote(doc.getName())));
	        ret.append(writeStatement(getDAOVariableName(doc)+".init(c)"));
	    }
	
	    decreaseIdent();
	    ret.append(writeString("}catch(DAOException e){"));
		ret.append(writeIncreasedStatement("log.fatal(\"init failed (dao:\"+currentDAO+\") \",e )"));
		//ret.append(writeIncreasedStatement("throw new RuntimeException(\"init failed (dao:\"+currentDAO+\") cause: \"+e.getMessage())"));
	    ret.append(writeString("}catch(SQLException e){"));
		ret.append(writeIncreasedStatement("log.fatal(\"init failed (sql) \",e )"));
		//ret.append(writeIncreasedStatement("throw new RuntimeException(\"init failed (sql) cause: \"+e.getMessage())"));
	    ret.append(writeString("}catch(Exception e){"));
	    ret.append(writeIncreasedStatement("System.out.println(e.getMessage()+\" \"+e.getClass())"));
		ret.append(writeIncreasedStatement("log.fatal(\"init failed (e) \",e )"));
		//ret.append(writeIncreasedStatement("throw new RuntimeException(\"init failed (sql) cause: \"+e.getMessage())"));
		ret.append(writeString("}"));
	    ret.append(closeBlock());
	    ret.append(emptyline());
	    
	    
	    String throwsClause = " throws "+getExceptionName(module);
	    String callLog = null;
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";

	        callLog = "\"Call get"+doc.getMultiple()+"() \"";
	        ret.append(writeComment("Returns all "+doc.getMultiple()+" objects stored."));
	        ret.append(openFun("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause));
	        ret.append(generateMethodStart(callLog));
	        ret.append(writeStatement("return "+getDAOVariableName(doc)+".get"+doc.getMultiple()+"(c)"));
	        ret.append(generateMethodEnd(module, callLog));
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        callLog = "\"Call delete"+doc.getName()+"(\"+id+\") \"";
	        ret.append(writeComment("Deletes a "+doc.getName()+" object by id."));
	        ret.append(openFun("public void delete"+doc.getName()+"(String id)"+throwsClause));
	        ret.append(generateMethodStart(callLog));
	        ret.append(writeStatement(getDAOVariableName(doc)+".delete"+doc.getName()+"(c, id)"));
	        ret.append(generateMethodEnd(module, callLog));
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        callLog = "\"Call get"+doc.getName()+"(\"+id+\") \"";
	        ret.append(writeComment("Returns the "+doc.getName()+" object with the specified id."));
	        ret.append(openFun("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause));
	        ret.append(generateMethodStart(callLog));
	        ret.append(writeStatement("return "+getDAOVariableName(doc)+".get"+doc.getName()+"(c, id)"));
	        ret.append(generateMethodEnd(module, callLog));
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        callLog = "\"Call import"+doc.getName()+"(\"+"+doc.getVariableName()+"+\") \"";
	        ret.append(writeComment("Imports a new "+doc.getName()+" object.\nReturns the imported version."));
	        ret.append(openFun("public "+doc.getName()+" import"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause));
	        ret.append(generateMethodStart(callLog));
	        ret.append(writeStatement("return "+getDAOVariableName(doc)+".import"+doc.getName()+"(c, "+doc.getVariableName()+")"));
	        ret.append(generateMethodEnd(module, callLog));
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        
	        callLog = "\"Call create"+doc.getName()+"(\"+"+doc.getVariableName()+"+\") \"";
	        ret.append(writeComment("Creates a new "+doc.getName()+" object.\nReturns the created version."));
	        ret.append(openFun("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause));
	        ret.append(generateMethodStart(callLog));
	        ret.append(writeStatement("return "+getDAOVariableName(doc)+".create"+doc.getName()+"(c, "+doc.getVariableName()+")"));
	        ret.append(generateMethodEnd(module, callLog));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        callLog = "\"Call create"+doc.getMultiple()+"(\"+list+\") \"";
	        ret.append(writeComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions."));
	        ret.append(openFun("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause));
	        ret.append(generateMethodStart(callLog));
	        ret.append(writeStatement("return "+getDAOVariableName(doc)+".create"+doc.getMultiple()+"(c, list)"));
	        ret.append(generateMethodEnd(module, callLog));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        

	        callLog = "\"Call update"+doc.getName()+"(\"+"+doc.getVariableName()+"+\") \"";
	        ret.append(writeComment("Updates a "+doc.getName()+" object.\nReturns the updated version."));
	        ret.append(openFun("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause));
	        ret.append(generateMethodStart(callLog));
	        ret.append(writeStatement("return "+getDAOVariableName(doc)+".update"+doc.getName()+"(c, "+doc.getVariableName()+")"));
	        ret.append(generateMethodEnd(module, callLog));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        callLog = "\"Call update"+doc.getMultiple()+"(\"+list+\") \"";
	        ret.append(writeComment("Updates multiple  "+doc.getName()+" objects.\nReturns the updated versions."));
	        ret.append(openFun("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause));
	        ret.append(generateMethodStart(callLog));
	        ret.append(writeStatement("return "+getDAOVariableName(doc)+".update"+doc.getMultiple()+"(c, list)"));
	        ret.append(generateMethodEnd(module, callLog));
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        callLog = "\"Call get"+doc.getMultiple()+"ByProperty(\"+properties+\") \"";
	        ret.append(writeComment("Returns all "+doc.getName()+" objects which match the given property."));
	        ret.append(openFun("public "+listDecl+" get"+doc.getMultiple()+"ByProperty"+"(QueryProperty... properties)"+throwsClause));
	        ret.append(generateMethodStart(callLog));
	        ret.append(writeStatement("return "+getDAOVariableName(doc)+".get"+doc.getMultiple()+"ByProperty(c, Arrays.asList(properties))"));
	        ret.append(generateMethodEnd(module, callLog));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
/*	        
	        ret.append(writeComment("Returns all "+doc.getName()+" objects which match given properties."));
	        ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperties"+"(List<QueryProperty> properties)"+throwsClause));
	        ret.append(emptyline());
*/	        
	    }
	    

	    
	    //generate export function
	    ret.append(emptyline());
	    
	    
	    ret.append(closeBlock());
	    return ret.toString();
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
