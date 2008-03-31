package net.anotheria.asg.generator.model.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaListProperty;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.ModuleParameter;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.util.ExecutionTimer;
import net.anotheria.util.StringUtils;

/**
 * This generator generates the DAO for a Document, the daoexceptions, and the rowmapper.
 * @author another
 *
 */
public class PersistenceServiceDAOGenerator extends AbstractGenerator implements IGenerator{
	
	private Context context;
	
	public List<FileEntry> generate(IGenerateable gmodule, Context context){
		
		MetaModule mod = (MetaModule)gmodule;
		
		this.context = context;
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ExecutionTimer timer = new ExecutionTimer(mod.getName()+"-DaoGen");
		
		List<MetaDocument> documents = mod.getDocuments();
		for (MetaDocument d: documents){
			
			timer.startExecution(d.getName());
			timer.startExecution(d.getName()+"Exc");
			ret.add(new FileEntry(FileEntry.package2path(getPackageName(mod)), getExceptionName(d), generateException(d)));
			timer.stopExecution(d.getName()+"Exc");

			timer.startExecution(d.getName()+"NoItemE");
			ret.add(new FileEntry(FileEntry.package2path(getPackageName(mod)), getNoItemExceptionName(d), generateNoItemException(d)));
			timer.stopExecution(d.getName()+"NoItemE");
			
			timer.startExecution(d.getName()+"DAO");
			ret.add(new FileEntry(FileEntry.package2path(getPackageName(mod)), getDAOName(d), generateDAO(d)));
			timer.stopExecution(d.getName()+"DAO");
			
			timer.startExecution(d.getName()+"RowMapper");
			ret.add(new FileEntry(FileEntry.package2path(getPackageName(mod)), getRowMapperName(d), generateRowMapper(d)));
			timer.stopExecution(d.getName()+"RowMapper");

			timer.stopExecution(d.getName());
		}
		
		//timer.printExecutionTimesOrderedByCreation();
		return ret;
	}
	
	private String getPackageName(MetaModule module){
		return context.getPackageName(module)+".service.persistence";
	}
	
	private String generateException(MetaDocument doc){
		StringBuilder ret = new StringBuilder(5000);
		
	    ret.append(CommentGenerator.generateJavaTypeComment(getExceptionName(doc)));
	    
	    ret.append(writeStatement("package "+getPackageName(doc.getParentModule())));
	    ret.append(emptyline());
	    ret.append(writeImport("net.anotheria.db.dao.DAOException"));
	    ret.append(emptyline());

	    ret.append(writeString("public class "+getExceptionName(doc)+ " extends DAOException{"));
		increaseIdent();
		ret.append(emptyline());
		ret.append(writeString("public "+getExceptionName(doc)+"(String message){"));
		ret.append(writeIncreasedStatement("super(message)"));
		ret.append(writeString("}"));
		
		ret.append(emptyline());
		ret.append(writeString("public "+getExceptionName(doc)+"(){"));
		ret.append(writeIncreasedStatement("super()"));
		ret.append(writeString("}"));

		ret.append(closeBlock());
		return ret.toString(); 
	}
	
	private String generateNoItemException(MetaDocument doc){
		StringBuilder ret = new StringBuilder(5000);
		
	    ret.append(CommentGenerator.generateJavaTypeComment(getNoItemExceptionName(doc)));
	    
	    ret.append(writeStatement("package "+getPackageName(doc.getParentModule())));
	    ret.append(emptyline());

	    ret.append(writeString("public class "+ getNoItemExceptionName(doc)+ " extends "+getExceptionName(doc)+"{"));
		increaseIdent();
		ret.append(emptyline());
		ret.append(writeString("public "+getNoItemExceptionName(doc)+"(String id){"));
		ret.append(writeIncreasedStatement("super("+quote("No item found for id: ")+"+id)"));
		ret.append(writeString("}"));
		
		ret.append(writeString("public "+getNoItemExceptionName(doc)+"(long id){"));
		ret.append(writeIncreasedStatement("this("+quote("")+"+id)"));
		ret.append(writeString("}"));

		ret.append(closeBlock());
		return ret.toString(); 
	}
	
	private String getAttributeConst(MetaProperty p){
		return "ATT_NAME_"+p.getName().toUpperCase();
	}

	private String getAttributeName(MetaProperty p){
		return p.getName().toLowerCase();
	}

	private String generateRowMapper(MetaDocument doc){
	    
		
		StringBuilder ret = new StringBuilder(5000);
	    
	    ret.append(CommentGenerator.generateJavaTypeComment(getRowMapperName(doc)));
	    
	    ret.append(writeStatement("package "+getPackageName(doc.getParentModule())));
	    ret.append(emptyline());
	    
	    ret.append(writeImport("java.sql.ResultSet"));
	    ret.append(writeImport("java.sql.SQLException"));
	    ret.append(emptyline());

	    ret.append(writeImport("net.anotheria.db.dao.RowMapper"));
	    ret.append(writeImport("net.anotheria.db.dao.RowMapperException"));
	    ret.append(emptyline());
	    ret.append(writeImport("org.apache.log4j.Logger"));
	    ret.append(emptyline());
	    
//	    List<MetaProperty> properties = new ArrayList<MetaProperty>();
//	    properties.addAll(doc.getProperties());
//	    properties.addAll(doc.getLinks());
//	    for (MetaProperty p : properties)
//	    	if(p instanceof MetaListProperty){
//	    		ret.append(writeImport("java.util.Arrays"));
//	    		ret.append(emptyline());
//	    		break;
//	    	}
	    
        ret.append(writeImport(DataFacadeGenerator.getDocumentImport(context, doc)));
        ret.append(writeImport(VOGenerator.getDocumentImport(context, doc)));
	    ret.append(emptyline());

	    ret.append(writeString("public class "+getRowMapperName(doc)+" extends RowMapper<"+doc.getName()+">{"));
	    increaseIdent();
	    ret.append(emptyline());
		ret.append(writeStatement("private static Logger log = Logger.getLogger("+getDAOName(doc)+".class)"));
	    ret.append(emptyline());

	    
	    ret.append(openFun("public "+doc.getName()+" map(ResultSet row) throws RowMapperException"));

	    ret.append(openTry());
	    
	    ret.append(writeStatement("long id = row.getLong(1)"));
	    ret.append(writeStatement(doc.getName()+" ret = new "+VOGenerator.getDocumentImplName(doc)+"(\"\"+id)"));
	    for (int i=0; i<doc.getProperties().size(); i++){
	    	ret.append(generateProperty2DBMapping(doc.getProperties().get(i), i+2));
	    }
	    int ioffset = doc.getProperties().size();
	    
	    for (int i=0; i<doc.getLinks().size(); i++){
	    	ret.append(generateProperty2DBMapping(doc.getLinks().get(i), i+ioffset+2));
	    }
	    
	    ioffset = doc.getProperties().size()+doc.getLinks().size();
	    ret.append(generateProperty2DBMappingPrivate(doc, new MetaProperty(VOGenerator.DAO_CREATED, "long"), ioffset+2));
	    ret.append(generateProperty2DBMappingPrivate(doc, new MetaProperty(VOGenerator.DAO_UPDATED, "long"), ioffset+3));
	    
	    ret.append(writeStatement("return ret"));
	    decreaseIdent();
	    ret.append(writeString("}catch(SQLException e){"));
	    ret.append(writeIncreasedStatement("log.error(\"map\", e)"));
	    ret.append(writeIncreasedStatement("throw new RowMapperException(e)"));
	    ret.append(writeString("}"));
	    
	    ret.append(closeBlock());

		ret.append(closeBlock());
	    
	    return ret.toString();
	}
	
	private String generateProperty2DBMapping(MetaProperty p, int position){
		if (p instanceof MetaListProperty)
			return _generateArrayProperty2DBMapping((MetaListProperty)p, position);
		else {
			return _generateProperty2DBMapping(p, position);
		}
	}

	private String _generateProperty2DBMapping(MetaProperty p, int position){
		String call = "";
		
		call += "ret.set";
		call += p.getAccesserName();
		call += "(";
		call += "row.";
		call += p.toPropertyGetter();
		call += "("+position+"))";
		
		return writeStatement(call);
	}
	
	private String _generateArrayProperty2DBMapping(MetaListProperty p, int position){
		String call = "";
		
		call += "ret.set";
		call += p.getAccesserName();
		call += "(";
		call += "convertToList(";
		call += "(" + p.getContainedProperty().toJavaType() + "[])";
		call += "row.getArray";
		call += "("+position+")";
		call += ".getArray";
		call += "()))";
		
		return writeStatement(call);
	}
	
	private String generateProperty2DBMappingPrivate(MetaDocument doc, MetaProperty p, int position){
		String call = "";
		
		call += "(("+VOGenerator.getDocumentImplName(doc)+")ret).set";
		call += p.getAccesserName();
		call += "(";
		call += "row.";
		call += p.toPropertyGetter();
		call += "("+position+"))";
		
		return writeStatement(call);
	}

	private String generateDB2PropertyMapping(String variableName, MetaProperty p, int position){
		if (p instanceof MetaListProperty)
			return _generateDB2ArrayPropertyMapping(variableName, (MetaListProperty)p, position);
		else {
			return _generateDB2PropertyMapping(variableName, p, position);
		}
	}
	
	private String _generateDB2PropertyMapping(String variableName, MetaProperty p, int position){
		String call = "";
		
		call += "ps.";
		call += p.toPropertySetter();
		call += "("+position+", ";
		call += variableName+".";
		call += p.toGetter();
		call += "())";
		
		
		return writeStatement(call);
	}
	
	private String _generateDB2ArrayPropertyMapping(String variableName, MetaListProperty p, int position){
		String call = "";
		
		call += "ps.setArray";
		call += "("+position+", ";
		call += "new " + p.getContainedProperty().toJavaObjectType() + "Array(";
		call += variableName+".";
		call += p.toGetter();
		call += "()))";
		
		return writeStatement(call);
	}
	
	private String generateDB2PropertyCallMapping(String variableName, MetaProperty p, String position){
		if (p instanceof MetaListProperty)
			return _generateDB2ArrayPropertyCallMapping(variableName, (MetaListProperty)p, position);
		else {
			return _generateDB2PropertyCallMapping(variableName, p, position);
		}
	}
	
	private String _generateDB2PropertyCallMapping(String variableName, MetaProperty p, String position){
		String call = "";
		
		call += "ps.";
		call += p.toPropertySetter();
		call += "("+position+", ";
		call += "("+p.toJavaObjectType()+")" + variableName;
		call += ")";
		
		
		return call;
	}
	
	private String _generateDB2ArrayPropertyCallMapping(String variableName, MetaListProperty p, String position){
		String call = "//Not implemented";
		
//		call += "ps.setArray";
//		call += "("+position+", ";
//		call += "new " + p.getContainedProperty().toJavaObjectType() + "Array(";
//		call += variableName+".";
//		call += p.toGetter();
//		call += "()))";
		
		return call;
	}

	private String generateDAO(MetaDocument doc){
		
	    List<MetaProperty> properties = new ArrayList<MetaProperty>();
	    properties.addAll(doc.getProperties());
	    properties.addAll(doc.getLinks());
		
		StringBuilder ret = new StringBuilder(5000);
	    
	    ret.append(CommentGenerator.generateJavaTypeComment(getDAOName(doc)));

	    boolean moduleDbContextSensitive = doc.getParentModule().isParameterEqual(ModuleParameter.MODULE_DB_CONTEXT_SENSITIVE, "true");
	    
	    ret.append(writeStatement("package "+getPackageName(doc.getParentModule())));
	    ret.append(emptyline());
	    ret.append(writeImport("java.util.List"));
	    ret.append(writeImport("java.util.ArrayList"));
	    ret.append(writeImport("java.util.concurrent.atomic.AtomicLong"));
	    if (moduleDbContextSensitive){
	    	ret.append(writeImport("java.util.Map"));
	    	ret.append(writeImport("java.util.HashMap"));
	    }
	    ret.append(emptyline());
	    
	    
        ret.append(writeImport(DataFacadeGenerator.getDocumentImport(context, doc)));
        ret.append(writeImport(VOGenerator.getDocumentImport(context, doc)));
        ret.append(writeImport("net.anotheria.db.dao.DAO"));
        ret.append(writeImport("net.anotheria.db.dao.DAOException"));
        ret.append(writeImport("net.anotheria.db.dao.DAOSQLException"));
        ret.append(writeImport("net.anotheria.db.dao.RowMapper"));
	    ret.append(emptyline()); 
	    ret.append(writeImport("net.anotheria.anodoc.query2.QueryProperty"));
	    ret.append(writeImport("net.anotheria.anodoc.util.context.DBContext"));
	    ret.append(writeImport("net.anotheria.anodoc.util.context.ContextManager"));
	    ret.append(emptyline());

	    ret.append(writeImport("java.sql.Connection"));
	    ret.append(writeImport("java.sql.PreparedStatement"));
	    ret.append(writeImport("java.sql.ResultSet"));
	    ret.append(writeImport("java.sql.SQLException"));
	    ret.append(writeImport("java.sql.Statement"));
	    ret.append(emptyline());
	    
	    ret.append(writeImport("org.apache.log4j.Logger"));
	    ret.append(emptyline());
	    
	    Set<String> arrayImports = new HashSet<String>();
	    for (MetaProperty p : properties){
	    	if(p instanceof MetaListProperty)
	    		arrayImports.add("net.anotheria.db.array." + ((MetaListProperty)p).getContainedProperty().toJavaObjectType() + "Array");
	    }
	    for(String imp: arrayImports)
	    	ret.append(writeImport(imp));
	    ret.append(emptyline());

	    ret.append(writeString("public class "+getDAOName(doc)+" implements DAO{"));
	    increaseIdent();
	    ret.append(emptyline());
		ret.append(writeStatement("private static Logger log = Logger.getLogger("+getDAOName(doc)+".class)"));
	    
	    //first define constants.
	    String constDecl = "public static final String ";
	    ret.append(writeStatement(constDecl+"TABNAME = "+quote(getSQLTableName(doc))));
	    ret.append(emptyline());
	    MetaProperty id = new MetaProperty("id", "string");
	    MetaProperty dao_created = new MetaProperty("dao_created", "long");
	    MetaProperty dao_updated = new MetaProperty("dao_updated", "long");

	    ret.append(writeStatement(constDecl+getAttributeConst(id)+" = "+quote(getAttributeName(id))));
	    for (MetaProperty p : properties){
		    ret.append(writeStatement(constDecl+getAttributeConst(p)+" \t = "+quote(getAttributeName(p))));
	    }
	    
	    ret.append(emptyline());
	    //create sql staments
	    //SQL-CREATE
	    String sqlCreate1 = quote("INSERT INTO ");
	    StringBuilder sqlCreate2 = new StringBuilder(quote(" ("));
	    sqlCreate2.append("+"+getAttributeConst(id));
	    for (MetaProperty p : properties){
	    	sqlCreate2.append("+"+quote(", ")+"+"+getAttributeConst(p));
	    }
	    sqlCreate2.append("+"+quote(", ")+"+"+getAttributeConst(dao_created));
	    StringBuilder sqlCreateEnd = new StringBuilder(") VALUES (");
	    //+2 because of created flag and id.
	    for (int i=0; i<properties.size()+2; i++){
	    	sqlCreateEnd.append("?");
	    	if (i<properties.size()+1)
	    		sqlCreateEnd.append(",");
	    }
	    sqlCreateEnd.append(")");
	    sqlCreate2.append("+").append(quote(sqlCreateEnd));
	    ret.append(writeStatement(constDecl+" SQL_CREATE_1 \t= "+sqlCreate1));
	    ret.append(writeStatement(constDecl+" SQL_CREATE_2 \t= "+sqlCreate2));
	    
	    //SQL-UPDATE
	    String sqlUpdate1 = quote("UPDATE ");
	    StringBuilder sqlUpdate2 = new StringBuilder(quote(" SET "));
	    for (MetaProperty p : properties){
	    	sqlUpdate2.append(" + ").append(getAttributeConst(p)).append(" + ").append(quote(" = ?, "));
	    }
    	sqlUpdate2.append(" + ").append(getAttributeConst(dao_updated)).append(" + ").append(quote(" = ?"));
	    sqlUpdate2.append(" + ").append(quote(" WHERE ")).append(" + ").append(getAttributeConst(id)).append(" + ").append(quote(" = ?"));

    	ret.append(writeStatement(constDecl+" SQL_UPDATE_1 \t= "+sqlUpdate1));
    	ret.append(writeStatement(constDecl+" SQL_UPDATE_2 \t= "+sqlUpdate2.toString()));
	    
	    //SQL-DELETE
	    String sqlDelete1 = quote("DELETE FROM ");
	    String sqlDelete2 = quote(" WHERE ")+" + TABNAME +"+quote(".")+" + "+getAttributeConst(id)+" + "+quote(" = ?");
	    ret.append(writeStatement(constDecl + " SQL_DELETE_1 \t= "+sqlDelete1));
	    ret.append(writeStatement(constDecl + " SQL_DELETE_2 \t= "+sqlDelete2));

	    //SQL-READ-ONE
	    
	    StringBuilder allAttrbutes = new StringBuilder("\"");
	    allAttrbutes.append("+").append(getAttributeConst(id));
	    for (MetaProperty p : properties){
	    	allAttrbutes.append("+").append(quote(", ")).append("+").append(getAttributeConst(p));
	    }
	    allAttrbutes.append("+"+quote(", ")+"+"+getAttributeConst(dao_created));
	    allAttrbutes.append("+"+quote(", ")+"+"+getAttributeConst(dao_updated));
	    allAttrbutes.append("+\"");
	    
	    String sqlReadOne1 = quote("SELECT "+allAttrbutes+" FROM ");
	    String sqlReadOne2 = quote(" WHERE ")+" + TABNAME +"+quote(".")+" + "+getAttributeConst(id)+" + "+quote(" = ?");
	    ret.append(writeStatement(constDecl + " SQL_READ_ONE_1 \t= "+sqlReadOne1));
	    ret.append(writeStatement(constDecl + " SQL_READ_ONE_2 \t= "+sqlReadOne2));

	    //SQL-READ-ALL
	    String sqlReadAll1 = quote("SELECT "+allAttrbutes+" FROM ");
	    String sqlReadAll2 = quote(" ORDER BY id");
	    ret.append(writeStatement(constDecl + " SQL_READ_ALL_1 \t= "+sqlReadAll1));
	    ret.append(writeStatement(constDecl + " SQL_READ_ALL_2 \t= "+sqlReadAll2));
	    
	    //SQL-READ-ALL
	    String sqlReadAllByProperty1 = quote("SELECT "+allAttrbutes+" FROM ");
	    String sqlReadAllByProperty2 = quote(" WHERE ");
	    ret.append(writeStatement(constDecl + " SQL_READ_ALL_BY_PROPERTY_1 \t= "+sqlReadAllByProperty1));
	    ret.append(writeStatement(constDecl + " SQL_READ_ALL_BY_PROPERTY_2 \t= "+sqlReadAllByProperty2));

	    ret.append(emptyline());
	    ret.append(writeStatement("private RowMapper<"+doc.getName()+"> rowMapper = new "+doc.getName()+"RowMapper()"));
	    
	    ret.append(emptyline());
	    //create impl


	    if (moduleDbContextSensitive){
	    	ret.append(writeStatement("private Map<String,AtomicLong> lastIds = new HashMap<String,AtomicLong>()"));
	    }else{
		    ret.append(writeStatement("private AtomicLong lastId = new AtomicLong()"));
	    }
	    ret.append(writeStatement("private static final long START_ID = 0"));
	    ret.append(emptyline());
	    
	    //get last id method
	    ret.append(writeString("private AtomicLong getLastId(Connection con) throws DAOException {"));
	    increaseIdent();
	    if (moduleDbContextSensitive){
		    ret.append(writeStatement("DBContext context = ContextManager.getCallContext().getDbContext()"));
		    ret.append(writeStatement("String tableName = context.getTableNameInContext(TABNAME)"));
	    	ret.append(writeStatement("AtomicLong lastId = lastIds.get(tableName)"));
	    	ret.append(writeString("if (lastId==null){"));
	    	increaseIdent();
	    	ret.append(writeCommentLine("double-checked-locking"));
	    	ret.append(writeString("synchronized(lastIds){"));
	    	increaseIdent();
	    	ret.append(writeStatement("lastId = lastIds.get(tableName)"));
	    	ret.append(writeString("if (lastId==null){"));
	    	increaseIdent();
        	ret.append(writeStatement("long maxId = getMaxId(con, tableName)"));
        	ret.append(writeStatement("lastId = new AtomicLong(maxId == 0 ? START_ID : maxId)"));
        	ret.append(writeStatement("lastIds.put(tableName, lastId)"));
        	ret.append(closeBlock());
        	ret.append(closeBlock());
        	ret.append(closeBlock());
        	ret.append(writeStatement("return lastId"));
	    	
	    }else{
	    	ret.append(writeStatement("return lastId"));
	    	
	    }
	    ret.append(closeBlock());
	    ret.append(emptyline());
	    
	    //createSQL Method
	    ret.append(writeString("private String createSQL(String sql1, String sql2){"));
	    increaseIdent();
	    if (moduleDbContextSensitive){
		    ret.append(writeStatement("DBContext context = ContextManager.getCallContext().getDbContext()"));
		    ret.append(writeStatement("StringBuilder sql = new StringBuilder()"));
		    ret.append(writeStatement("sql.append(sql1).append(context.getTableNameInContext(TABNAME)).append(sql2)"));
		    ret.append(writeStatement("return sql.toString()"));
	    }else{
		    ret.append(writeStatement("StringBuilder sql = new StringBuilder()"));
		    ret.append(writeStatement("sql.append(sql1).append(TABNAME).append(sql2)"));
		    ret.append(writeStatement("return sql.toString()"));
	    }
	    ret.append(closeBlock());
	    ret.append(emptyline());
	    
	    String throwsClause = " throws DAOException";
	    String callLog = "";
	    
        //get all XYZ method
        callLog = quote("get"+doc.getMultiple()+"(")+"+con+"+quote(")");
        ret.append(writeComment("Returns all "+doc.getMultiple()+" objects stored."));
        ret.append(openFun("public List<"+doc.getName()+">"+" get"+doc.getMultiple()+"(Connection con)"+throwsClause));
        ret.append(generateFunctionStart("SQL_READ_ALL", callLog, true));
        ret.append(writeStatement("ResultSet result = ps.executeQuery()"));
        ret.append(writeStatement("ArrayList<"+doc.getName()+"> ret = new ArrayList<"+doc.getName()+">()"));
        ret.append(writeString("while(result.next())"));
		ret.append(writeIncreasedStatement("ret.add(rowMapper.map(result))"));
		ret.append(writeStatement("return  ret"));
        ret.append(generateFunctionEnd(callLog, true));
        ret.append(closeBlock());
        ret.append(emptyline());
        
        ret.append(writeComment("Deletes a "+doc.getName()+" object by id."));
        callLog = quote("delete"+doc.getName()+"(")+"+con+"+quote(", ")+"+id+"+quote(")");
        
        ret.append(openFun("public void delete"+doc.getName()+"(Connection con, String id)"+throwsClause));
        ret.append(generateFunctionStart("SQL_DELETE", callLog, true));
        ret.append(writeStatement("ps.setLong(1, Long.parseLong(id))"));
        ret.append(writeStatement("int rows = ps.executeUpdate()"));
        ret.append(writeString("if (rows!=1 && rows!=0){"));
        increaseIdent();
        ret.append(writeStatement("log.warn(\"Deleted more than one row of "+doc.getName()+": \"+id)"));
		ret.append(closeBlock());
		ret.append(generateFunctionEnd(callLog, true));
        ret.append(closeBlock());
        ret.append(emptyline());
        
        //getXYZ method
        callLog = quote("get"+doc.getName()+"(")+"+con+"+quote(", ")+"+id+"+quote(")");
        ret.append(writeComment("Returns the "+doc.getName()+" object with the specified id."));
        ret.append(openFun("public "+doc.getName()+" get"+doc.getName()+"(Connection con, String id)"+throwsClause));
        ret.append(generateFunctionStart("SQL_READ_ONE", callLog, true));
        ret.append(writeStatement("ps.setLong(1, Long.parseLong(id))"));
        ret.append(writeStatement("ResultSet result = ps.executeQuery()"));
        ret.append(writeString("if (!result.next())"));
        ret.append(writeIncreasedStatement("throw new "+getNoItemExceptionName(doc)+"(id)"));
		ret.append(writeStatement("return rowMapper.map(result)"));
        ret.append(generateFunctionEnd(callLog, true));
        ret.append(closeBlock());
        ret.append(emptyline());
        
        int ii = 0;
        
        //createXYZ method
        callLog = quote("create"+doc.getName()+"(")+"+con+"+quote(", ")+"+"+doc.getVariableName()+"+"+quote(")");
        ret.append(writeComment("Creates a new "+doc.getName()+" object.\nReturns the created version."));
        ret.append(openFun("public "+doc.getName()+" create"+doc.getName()+"(Connection con, "+doc.getName()+" "+doc.getVariableName()+")"+throwsClause));
        ret.append(generateFunctionStart("SQL_CREATE", callLog, true));
        ret.append(writeStatement("long nextId = getLastId(con).incrementAndGet()"));
        ret.append(writeStatement("ps.setLong(1, nextId)"));
        for (int i=0; i<properties.size(); i++){
        	ret.append(generateDB2PropertyMapping(doc.getVariableName(), properties.get(i), i+2));
        	ii = i +2;
        }
        ret.append(writeCommentLine("set create timestamp"));
        ret.append(writeStatement("ps.setLong("+(ii+1)+", System.currentTimeMillis())"));

        ret.append(writeStatement("int rows = ps.executeUpdate()"));
        ret.append(writeString("if (rows!=1)"));
        ret.append(writeIncreasedStatement("throw new DAOException(\"Create failed, updated rows: \"+rows)"));
        
        String copyResVarName = "new"+StringUtils.capitalize(doc.getVariableName());
        String createCopyCall = VOGenerator.getDocumentImplName(doc)+" "+copyResVarName + " = new "+VOGenerator.getDocumentImplName(doc);
        createCopyCall += "(\"\"+nextId)";
        ret.append(writeStatement(createCopyCall));
        ret.append(writeStatement(copyResVarName+".copyAttributesFrom("+doc.getVariableName()+")"));
        
        ret.append(writeStatement("return "+copyResVarName));
        ret.append(generateFunctionEnd(callLog, true));
        
        ret.append(closeBlock());
        ret.append(emptyline());


        //createListXYZ method
        String listDecl = "List<"+doc.getName()+">";
        callLog = quote("create"+doc.getMultiple()+"(")+"+con+"+quote(", ")+"+list+"+quote(")");
        ret.append(writeComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions."));
        ret.append(openFun("public "+listDecl+" create"+doc.getMultiple()+"(Connection con, "+listDecl+" list)"+throwsClause));
        //ret.append(generateFunctionStart("SQL_CREATE", callLog, true));
        ret.append(writeStatement("PreparedStatement ps = null"));
        ret.append(writeString("try{"));
        increaseIdent();
        ret.append(writeStatement("con.setAutoCommit(false)"));
        ret.append(writeStatement("ps = con.prepareStatement(createSQL(SQL_CREATE_1, SQL_CREATE_2))"));
        ret.append(writeStatement(listDecl+" ret = new ArrayList<"+doc.getName()+">()"));
        ret.append(writeString("for ("+doc.getName()+" "+doc.getVariableName()+" : list){"));
        increaseIdent();
        ret.append(writeStatement("long nextId = getLastId(con).incrementAndGet()"));
        ret.append(writeStatement("ps.setLong(1, nextId)"));
        for (int i=0; i<properties.size(); i++){
        	ret.append(generateDB2PropertyMapping(doc.getVariableName(), properties.get(i), i+2));
        	ii = i +2;
        }
        ret.append(writeCommentLine("set create timestamp"));
        ret.append(writeStatement("ps.setLong("+(ii+1)+", System.currentTimeMillis())"));

        ret.append(writeStatement("int rows = ps.executeUpdate()"));
        ret.append(writeString("if (rows!=1)"));
        ret.append(writeIncreasedStatement("throw new DAOException(\"Create failed, updated rows: \"+rows)"));
        
        /*String*/ copyResVarName = "new"+StringUtils.capitalize(doc.getVariableName());
        /*String*/ createCopyCall = VOGenerator.getDocumentImplName(doc)+" "+copyResVarName + " = new "+VOGenerator.getDocumentImplName(doc);
        createCopyCall += "(\"\"+nextId)";
        ret.append(writeStatement(createCopyCall));
        ret.append(writeStatement(copyResVarName+".copyAttributesFrom("+doc.getVariableName()+")"));
        
        ret.append(writeStatement("ret.add("+copyResVarName+")"));
        ret.append(closeBlock());
        ret.append(writeStatement("con.commit()"));
        ret.append(writeStatement("return ret"));
        ret.append(generateFunctionEnd(callLog, true));
        
        ret.append(closeBlock());
        ret.append(emptyline());

        //updateXYZ method
        callLog = quote("update"+doc.getName()+"(")+"+con+"+quote(", ")+"+"+doc.getVariableName()+"+"+quote(")");
        ret.append(writeComment("Updates a "+doc.getName()+" object.\nReturns the updated version."));
        ret.append(openFun("public "+doc.getName()+" update"+doc.getName()+"(Connection con, "+doc.getName()+" "+doc.getVariableName()+")"+throwsClause));
        ret.append(generateFunctionStart("SQL_UPDATE", callLog, true));

        for (int i=0; i<properties.size(); i++){
        	ret.append(generateDB2PropertyMapping(doc.getVariableName(), properties.get(i), i+1));
        	ii = i+1;
        }
        ret.append(writeCommentLine("set update timestamp"));
        ret.append(writeStatement("ps.setLong("+(ii+1)+", System.currentTimeMillis())"));
        ret.append(writeCommentLine("set id for the where clause"));
        ret.append(writeStatement("ps.setLong("+(ii+2)+", Long.parseLong("+doc.getVariableName()+".getId()))"));

        ret.append(writeStatement("int rows = ps.executeUpdate()"));
        ret.append(writeString("if (rows!=1)"));
        ret.append(writeIncreasedStatement("throw new DAOException(\"Update failed, updated rows: \"+rows)"));

        ret.append(writeStatement("return "+doc.getVariableName()));
        ret.append(generateFunctionEnd(callLog, true));
        ret.append(closeBlock());
        ret.append(emptyline());
        
        
        //updateListXYZ method
        callLog = quote("update"+doc.getMultiple()+"(")+"+con+"+quote(", ")+"+list+"+quote(")");
        ret.append(writeComment("Updates multiple new "+doc.getName()+" objects.\nReturns the updated versions."));
        ret.append(openFun("public "+listDecl+" update"+doc.getMultiple()+"(Connection con, "+listDecl+" list)"+throwsClause));
        ret.append(writeStatement("PreparedStatement ps = null"));
        ret.append(writeString("try{"));
        increaseIdent();
        ret.append(writeStatement("con.setAutoCommit(false)"));
        ret.append(writeStatement("ps = con.prepareStatement(createSQL(SQL_UPDATE_1, SQL_UPDATE_2))"));
        ret.append(writeStatement(listDecl+" ret = new ArrayList<"+doc.getName()+">()"));
        ret.append(writeString("for ("+doc.getName()+" "+doc.getVariableName()+" : list){"));
        increaseIdent();
        for (int i=0; i<properties.size(); i++){
        	ret.append(generateDB2PropertyMapping(doc.getVariableName(), properties.get(i), i+1));
        	ii = i + 1;
        }
        ret.append(writeCommentLine("set update timestamp"));
        ret.append(writeStatement("ps.setLong("+(ii+1)+", System.currentTimeMillis())"));
        ret.append(writeCommentLine("set id for the where clause"));
        ret.append(writeStatement("ps.setLong("+(ii+2)+", Long.parseLong("+doc.getVariableName()+".getId()))"));

        ret.append(writeStatement("int rows = ps.executeUpdate()"));
        ret.append(writeString("if (rows!=1)"));
        ret.append(writeIncreasedStatement("throw new DAOException(\"Update failed, updated rows: \"+rows)"));
        
        ret.append(closeBlock());
        ret.append(writeStatement("con.commit()"));
        ret.append(writeStatement("return list"));
        ret.append(generateFunctionEnd(callLog, true));
        
        ret.append(closeBlock());
        ret.append(emptyline());
        //end updateListXYZ
        
        //get all XYZ byProperty method
        callLog = quote("get"+doc.getMultiple()+"ByProperty(")+"+con+"+quote(",")+"+ properties+"+quote(")");
        ret.append(writeComment("Returns all "+doc.getMultiple()+" objects stored which matches given properties."));
        ret.append(openFun("public List<"+doc.getName()+">"+" get"+doc.getMultiple()+"ByProperty(Connection con, List<QueryProperty> properties)"+throwsClause));
        //ret.append(generateFunctionStart("SQL_READ_ALL_BY_PROPERTY", callLog, true));
		ret.append(writeStatement("PreparedStatement ps = null"));
		ret.append(openTry());
		//TODO Caching fuer generierte SQL Statements
        ret.append(writeCommentLine("//enable caching of statements one day"));
        ret.append(writeStatement("String SQL = createSQL(SQL_READ_ALL_BY_PROPERTY_1, SQL_READ_ALL_BY_PROPERTY_2)"));
        ret.append(writeStatement("String whereClause = "+quote("")));
        ret.append(writeString("for (QueryProperty p : properties){"));
        increaseIdent();
        ret.append(writeString("if (whereClause.length()>0)"));
        ret.append(writeIncreasedStatement("whereClause += "+quote(" AND ")));
        ret.append(writeStatement("whereClause += p.getName()+p.getComparator()+"+quote("?")));
        ret.append(closeBlock());
        ret.append(writeStatement("SQL += whereClause"));
        //ret.append(writeStatement("System.out.println(SQL)"));
        ret.append(writeStatement("ps = con.prepareStatement(SQL)"));
        //set properties
        ret.append(writeString("for (int i=0; i<properties.size(); i++){"));
        increaseIdent();
        ret.append(writeStatement("setProperty(i+1, ps, properties.get(i))"));
        ret.append(closeBlock());        
        
        ret.append(writeStatement("ResultSet result = ps.executeQuery()"));
        ret.append(writeStatement("ArrayList<"+doc.getName()+"> ret = new ArrayList<"+doc.getName()+">()"));
        ret.append(writeString("while(result.next())"));
		ret.append(writeIncreasedStatement("ret.add(rowMapper.map(result))"));
		ret.append(writeStatement("return  ret"));
        ret.append(generateFunctionEnd(callLog, true));
        ret.append(closeBlock());
        ret.append(emptyline());
        
        //setProperty
        ret.append(openFun("private void setProperty(int position, PreparedStatement ps, QueryProperty property) throws SQLException"));
        for (MetaProperty p : properties){
        	ret.append(writeString("if ("+getAttributeConst(p)+".equals(property.getName())){"));
        	increaseIdent();
        	ret.append(writeStatement(generateDB2PropertyCallMapping("property.getValue()", p, "position")));
        	ret.append(writeStatement("return"));
        	ret.append(closeBlock());
        }
    	ret.append(writeString("if ("+getAttributeConst(id)+".equals(property.getName())){"));
    	increaseIdent();
    	ret.append(writeStatement(generateDB2PropertyCallMapping("property.getValue()", id, "position")));
    	ret.append(writeStatement("return"));
    	ret.append(closeBlock());
    	ret.append(writeString("if ("+quote(dao_created.getName())+".equals(property.getName())){"));
    	increaseIdent();
    	ret.append(writeStatement(generateDB2PropertyCallMapping("property.getValue()", dao_created, "position")));
    	ret.append(writeStatement("return"));
    	ret.append(closeBlock());
    	ret.append(writeString("if ("+quote(dao_updated.getName())+".equals(property.getName())){"));
    	increaseIdent();
    	ret.append(writeStatement(generateDB2PropertyCallMapping("property.getValue()", dao_updated, "position")));
    	ret.append(writeStatement("return"));
    	ret.append(closeBlock());

        ret.append(closeBlock()); //end setProperty
        
       //special functions
//	        ret.append(writeComment("Returns all "+doc.getName()+" objects, where property with given name equals object."));
//	        ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)"));
//	        ret.append(emptyline());
//			ret.append(writeComment("Returns all "+doc.getName()+" objects, where property with given name equals object, sorted"));
//			ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)"));
//			ret.append(emptyline());
//			ret.append(writeComment("Executes a query"));
//			ret.append(writeStatement("public QueryResult executeQueryOn"+doc.getMultiple()+"(DocumentQuery query)"));
//			ret.append(emptyline());
	    
	    
//		ret.append(writeComment("creates an xml element with all contained data."));
//		ret.append(writeStatement("public Element exportToXML()"));
//		ret.append(emptyline());
        
        ret.append(writeString("/* ---------- SQL --------- "));
        ret.append(generateSQLCreate(doc, dao_created, dao_updated));
        ret.append(writeString("   ---------- SQL --------- */"));
        
        ret.append(openFun("public void createStructure(Connection connection) "+throwsClause));
        ret.append(writeCommentLine("not implemented"));
        ret.append(closeBlock());
        ret.append(emptyline());
        		
        ret.append(writeString("/* ---------- SQL --------- "));
        ret.append(generateSQLDelete(doc));
        ret.append(writeString("   ---------- SQL --------- */"));
        ret.append(openFun("public void deleteStructure(Connection connection) "+throwsClause));
        ret.append(writeCommentLine("not implemented"));
        ret.append(closeBlock());
        ret.append(emptyline());
        
        ret.append(openFun("protected void finish(Statement st)"));
        ret.append(closeBlock());
        ret.append(emptyline());

        ret.append(openFun("private long getMaxId(Connection con, String tableName) "+throwsClause));
        ret.append(writeStatement("Statement st = null"));
        ret.append(openTry());
        ret.append(writeStatement("con.setAutoCommit(true)"));
        ret.append(writeStatement("st = con.createStatement()"));
    	ret.append(writeStatement("st.execute(\"SELECT MAX(\"+"+getAttributeConst(id)+"+\") FROM \"+tableName)"));
    	ret.append(writeStatement("ResultSet set = st.getResultSet()"));
    	ret.append(writeStatement("long maxId = 0"));
    	ret.append(writeString("if (set.next())"));
    	ret.append(writeIncreasedStatement("maxId = set.getLong(1)"));
    	ret.append(writeStatement("log.info(\"maxId in table \"+tableName+\" is \"+maxId)"));
    	ret.append(writeStatement("set.close()"));
    	ret.append(writeStatement("st.close()"));
    	ret.append(writeStatement("return maxId"));
    	
    	ret.append(generateFunctionEnd(quote("getMaxId(")+"+con+"+quote(", ")+"+tableName+"+quote(")"), false));
        ret.append(closeBlock());
        ret.append(emptyline());
        
        //init() method
        ret.append(openFun("public void init(Connection con) "+throwsClause));
        ret.append(writeStatement("log.debug(\"Called: init(\"+con+\")\")"));
        if (!moduleDbContextSensitive){
        	ret.append(writeStatement("long maxId = getMaxId(con, TABNAME)"));
        	ret.append(writeStatement("lastId = new AtomicLong(maxId == 0 ? START_ID : maxId)"));
        }
    	ret.append(closeBlock());
	    ret.append(closeBlock());
	    return ret.toString();
	}
	
	private String generateSQLDelete(MetaDocument doc){
		return writeString("DROP TABLE "+getSQLTableName(doc));
	}
	
	private String generateSQLCreate(MetaDocument doc, MetaProperty... additionalProps){
		String ret = "";
		ret += writeString("CREATE TABLE "+getSQLTableName(doc)+"(");
		ret += writeString("id int8 PRIMARY KEY,");
		for (int i=0; i<doc.getProperties().size(); i++){
			ret += writeString(getSQLPropertyDefinition(doc.getProperties().get(i))+",");
		}
		for (int i=0; i<doc.getLinks().size(); i++){
			ret += writeString(getSQLPropertyDefinition(doc.getLinks().get(i))+",");
		}
		for (int i=0; i<additionalProps.length-1; i++)
			ret += writeString(getSQLPropertyDefinition(additionalProps[i])+",");
		ret += writeString(getSQLPropertyDefinition(additionalProps[additionalProps.length-1]));
		
		ret += writeString(")");
		return ret;

	}
	
	private String getSQLPropertyDefinition(MetaProperty p){
		return getAttributeName(p)+" "+getSQLPropertyType(p);
	}
	
	/**
	 * This method maps MetaProperties Types to SQL DataTypes.
	 * @param p
	 * @return
	 */
	private String getSQLPropertyType(MetaProperty p){
		if (p.getType().equals("string"))
			return "varchar";
		if (p.getType().equals("text"))
			return "varchar";
		if (p.getType().equals("long"))
			return "int8";
		if (p.getType().equals("int"))
			return "int";
		if (p.getType().equals("double"))
			return "double precision";
		if (p.getType().equals("float"))
			return "float4";
		if (p.getType().equals("boolean"))
			return "boolean";
		return "UNKNOWN!";
	}

	private String getSQLTableName(MetaDocument doc){
		return doc.getName().toLowerCase();
	}
	
	private String generateFunctionStart(String SQL_STATEMENT, String callLog, boolean usePreparedSt){
		String ret = "";
		if (usePreparedSt){
			ret += writeStatement("PreparedStatement ps = null");
			ret += openTry();
			ret += writeStatement("con.setAutoCommit(true)");
			ret += writeStatement("ps = con.prepareStatement(createSQL("+SQL_STATEMENT+"_1, "+SQL_STATEMENT+"_2))");
		}else{
			ret += writeStatement("Statement st = null");
			ret += openTry();
			ret += writeStatement("con.setAutoCommit(true)");
			//ret += writeStatement("ps = con.prepareStatement("+SQL_STATEMENT+")");
		}
		return ret;
	}
	
	private String generateFunctionEnd(String callLog, boolean usePreparedSt){
		String ret = "";
		decreaseIdent();
		ret += writeString("}catch(SQLException e){");
		increaseIdent();
		ret += writeStatement("log.error("+callLog+", e)");
		ret += writeStatement("throw new DAOSQLException(e)");
		decreaseIdent();
		ret += writeString("}finally{");
		increaseIdent();
		ret += writeStatement("finish("+(usePreparedSt ? "ps": "st")+")");
		ret += closeBlock();
		return ret;
	}
	
	
	public static final String getExceptionName(MetaDocument doc){
		return getDAOName(doc)+"Exception";
	}
	
	public static final String getNoItemExceptionName(MetaDocument doc){
		return getDAOName(doc)+"NoItemForIdFoundException";
	}

	public static String getDAOName(MetaDocument doc){
	    return doc.getName()+"DAO";
	}

	public static String getRowMapperName(MetaDocument doc){
	    return doc.getName()+"RowMapper";
	}
}
