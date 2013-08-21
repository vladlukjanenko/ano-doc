package net.anotheria.asg.generator.model.db;

import net.anotheria.asg.generator.*;
import net.anotheria.asg.generator.meta.*;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.util.ExecutionTimer;
import net.anotheria.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This generator generates the DAO for a Document, the daoexceptions, and the rowmapper.
 *
 * @author another
 */
public class PersistenceServiceDAOGenerator extends AbstractGenerator implements IGenerator {

	public List<FileEntry> generate(IGenerateable gmodule) {

		MetaModule mod = (MetaModule) gmodule;

		List<FileEntry> ret = new ArrayList<FileEntry>();

		ExecutionTimer timer = new ExecutionTimer(mod.getName() + "-DaoGen");

		List<MetaDocument> documents = mod.getDocuments();
		for (MetaDocument d : documents) {

			timer.startExecution(d.getName());
			timer.startExecution(d.getName() + "Exc");
			ret.add(new FileEntry(generateException(d)));
			timer.stopExecution(d.getName() + "Exc");

			timer.startExecution(d.getName() + "NoItemE");
			ret.add(new FileEntry(generateNoItemException(d)));
			timer.stopExecution(d.getName() + "NoItemE");

			timer.startExecution(d.getName() + "DAO");
			ret.add(new FileEntry(generateDAO(d)));
			timer.stopExecution(d.getName() + "DAO");

			timer.startExecution(d.getName() + "RowMapper");
			ret.add(new FileEntry(generateRowMapper(d)));
			timer.stopExecution(d.getName() + "RowMapper");

			timer.stopExecution(d.getName());
		}

		//timer.printExecutionTimesOrderedByCreation();
		return ret;
	}

	private String getPackageName(MetaModule module) {
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(module) + ".service.persistence";
	}

	private GeneratedClass generateException(MetaDocument doc) {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getExceptionName(doc), this));

		clazz.setPackageName(getPackageName(doc.getParentModule()));

		clazz.addImport("net.anotheria.db.dao.DAOException");

		clazz.setName(getExceptionName(doc));
		clazz.setParent("DAOException");

		startClassBody();
		appendString("public " + getExceptionName(doc) + "(String message){");
		appendIncreasedStatement("super(message)");
		appendString("}");

		emptyline();
		appendString("public " + getExceptionName(doc) + "(){");
		appendIncreasedStatement("super()");
		appendString("}");

		return clazz;

	}

	private GeneratedClass generateNoItemException(MetaDocument doc) {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getNoItemExceptionName(doc), this));
		clazz.setPackageName(getPackageName(doc.getParentModule()));
		clazz.setName(getNoItemExceptionName(doc));
		clazz.setParent(getExceptionName(doc));

		startClassBody();
		appendString("public " + getNoItemExceptionName(doc) + "(String id){");
		appendIncreasedStatement("super(" + quote("No item found for id: ") + "+id)");
		appendString("}");

		appendString("public " + getNoItemExceptionName(doc) + "(long id){");
		appendIncreasedStatement("this(" + quote("") + "+id)");
		appendString("}");

		return clazz;
	}

	private String getAttributeConst(MetaProperty p) {
		return "ATT_NAME_" + p.getName().toUpperCase();
	}

	private String getAttributeName(MetaProperty p) {
		return p.getName().toLowerCase();
	}

	private GeneratedClass generateRowMapper(MetaDocument doc) {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		Context context = GeneratorDataRegistry.getInstance().getContext();

		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getRowMapperName(doc), this));
		clazz.setPackageName(getPackageName(doc.getParentModule()));

		clazz.addImport("java.sql.ResultSet");
		clazz.addImport("java.sql.SQLException");
		clazz.addImport("net.anotheria.db.dao.RowMapper");
		clazz.addImport("net.anotheria.db.dao.RowMapperException");
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.addImport(VOGenerator.getDocumentImport(context, doc));

		clazz.setName(getRowMapperName(doc));
		clazz.setParent("RowMapper<" + doc.getName() + ">");
		clazz.setGenerateLogger(true);

		startClassBody();

		openFun("public " + doc.getName() + " map(ResultSet row) throws RowMapperException");

		openTry();

		appendStatement("long id = row.getLong(1)");
		appendStatement(doc.getName() + " ret = new " + VOGenerator.getDocumentImplName(doc) + "(\"\"+id)");
		for (int i = 0; i < doc.getProperties().size(); i++) {
			generateProperty2DBMapping(doc.getProperties().get(i), i + 2);
		}
		int ioffset = doc.getProperties().size();

		for (int i = 0; i < doc.getLinks().size(); i++) {
			generateProperty2DBMapping(doc.getLinks().get(i), i + ioffset + 2);
		}

		ioffset = doc.getProperties().size() + doc.getLinks().size();
		generateProperty2DBMappingPrivate(doc, new MetaProperty(VOGenerator.DAO_CREATED, MetaProperty.Type.LONG), ioffset + 2);
		generateProperty2DBMappingPrivate(doc, new MetaProperty(VOGenerator.DAO_UPDATED, MetaProperty.Type.LONG), ioffset + 3);

		appendStatement("return ret");
		decreaseIdent();
		appendString("}catch(SQLException e){");
		appendIncreasedStatement("log.error(\"map\", e)");
		appendIncreasedStatement("throw new RowMapperException(e)");
		appendString("}");

		closeBlockNEW();

		return clazz;
	}

	private void generateProperty2DBMapping(MetaProperty p, int position) {
		if (p instanceof MetaListProperty)
			_generateArrayProperty2DBMapping((MetaListProperty) p, position);
		else {
			_generateProperty2DBMapping(p, position);
		}
	}

	private void _generateProperty2DBMapping(MetaProperty p, int position) {
		String call = "";

		call += "ret.set";
		call += p.getAccesserName();
		call += "(";
		call += "row.";
		call += p.toPropertyGetter();
		call += "(" + position + "))";

		appendStatement(call);
	}

	private void _generateArrayProperty2DBMapping(MetaListProperty p, int position) {
		String call = "";

		call += "ret.set";
		call += p.getAccesserName();
		call += "(";
		call += "convertToList(";
		call += "(" + p.getContainedProperty().toJavaType() + "[])";
		call += "row.getArray";
		call += "(" + position + ")";
		call += ".getArray";
		call += "()))";

		appendStatement(call);
	}

	private void generateProperty2DBMappingPrivate(MetaDocument doc, MetaProperty p, int position) {
		String call = "";

		call += "((" + VOGenerator.getDocumentImplName(doc) + ")ret).set";
		call += p.getAccesserName();
		call += "(";
		call += "row.";
		call += p.toPropertyGetter();
		call += "(" + position + "))";

		appendStatement(call);
	}

	private void generateDB2PropertyMapping(String variableName, MetaProperty p, int position) {
		if (p instanceof MetaListProperty)
			_generateDB2ArrayPropertyMapping(variableName, (MetaListProperty) p, position);
		else {
			_generateDB2PropertyMapping(variableName, p, position);
		}
	}

	private void _generateDB2PropertyMapping(String variableName, MetaProperty p, int position) {
		String call = "";

		call += "ps.";
		call += p.toPropertySetter();
		call += "(" + position + ", ";
		call += variableName + ".";
		call += p.toGetter();
		call += "())";


		appendStatement(call);
	}

	private void _generateDB2ArrayPropertyMapping(String variableName, MetaListProperty p, int position) {
		String call = "";

		call += "ps.setArray";
		call += "(" + position + ", ";
		call += "new " + p.getContainedProperty().toJavaObjectType() + "Array(";
		call += variableName + ".";
		call += p.toGetter();
		call += "()))";

		appendStatement(call);
	}

	private String getDB2PropertyCallMapping(String variableName, MetaProperty p, String position) {
		if (p instanceof MetaListProperty)
			return _getDB2ArrayPropertyCallMapping(variableName, (MetaListProperty) p, position);
		else {
			return _getDB2PropertyCallMapping(variableName, p, position);
		}
	}

	private String _getDB2PropertyCallMapping(String variableName, MetaProperty p, String position) {
		String call = "";

		call += "ps.";
		call += p.toPropertySetter();
		call += "(" + position + ", ";
		call += "(" + p.toJavaObjectType() + ")" + variableName;
		call += ")";


		return call;
	}

	private String _getDB2ArrayPropertyCallMapping(String variableName, MetaListProperty p, String position) {
		String call = "//Not implemented";

//		call += "ps.setArray";
//		call += "("+position+", ";
//		call += "new " + p.getContainedProperty().toJavaObjectType() + "Array(";
//		call += variableName+".";
//		call += p.toGetter();
//		call += "()))";

		return call;
	}

	private GeneratedClass generateDAO(MetaDocument doc) {

		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		List<MetaProperty> properties = new ArrayList<MetaProperty>();
		properties.addAll(doc.getProperties());
		properties.addAll(doc.getLinks());

		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getDAOName(doc), this));
		clazz.setPackageName(getPackageName(doc.getParentModule()));

		boolean moduleDbContextSensitive = doc.getParentModule().isParameterEqual(ModuleParameter.MODULE_DB_CONTEXT_SENSITIVE, "true");

		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		clazz.addImport("java.util.concurrent.atomic.AtomicLong");
		if (moduleDbContextSensitive) {
			clazz.addImport("java.util.Map");
			clazz.addImport("java.util.HashMap");
		}

		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.addImport(VOGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), doc));
		clazz.addImport("net.anotheria.db.dao.DAO");
		clazz.addImport("net.anotheria.db.dao.DAOException");
		clazz.addImport("net.anotheria.db.dao.DAOSQLException");
		clazz.addImport("net.anotheria.db.dao.RowMapper");
		clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
		clazz.addImport("net.anotheria.anodoc.util.context.DBContext");
		clazz.addImport("net.anotheria.anodoc.util.context.ContextManager");
		clazz.addImport("net.anotheria.util.slicer.Segment");

		clazz.addImport("java.sql.Connection");
		clazz.addImport("java.sql.PreparedStatement");
		clazz.addImport("java.sql.ResultSet");
		clazz.addImport("java.sql.SQLException");
		clazz.addImport("java.sql.Statement");
		clazz.addImport("org.slf4j.Logger");
		clazz.addImport("org.slf4j.LoggerFactory");
		clazz.addImport("net.anotheria.db.config.JDBCConfigFactory");
		clazz.addImport("net.anotheria.db.config.JDBCConfig");

		for (MetaProperty p : properties) {
			if (p instanceof MetaListProperty)
				clazz.addImport("net.anotheria.db.array." + ((MetaListProperty) p).getContainedProperty().toJavaObjectType() + "Array");
		}

		clazz.setName(getDAOName(doc));
		clazz.addInterface("DAO");

		startClassBody();
		appendStatement("private static Logger log = LoggerFactory.getLogger(" + getDAOName(doc) + ".class)");

		//first define constants.
		String constDecl = "public static final String ";
		appendStatement(constDecl + "TABNAME = " + quote(getSQLTableName(doc)));
		emptyline();
		MetaProperty id = new MetaProperty("id", MetaProperty.Type.STRING);
		MetaProperty dao_created = new MetaProperty("dao_created",MetaProperty.Type.LONG);
		MetaProperty dao_updated = new MetaProperty("dao_updated", MetaProperty.Type.LONG);

		appendStatement(constDecl + getAttributeConst(id) + " = " + quote(getAttributeName(id)));
		for (MetaProperty p : properties) {
			appendStatement(constDecl + getAttributeConst(p) + " \t = " + quote(getAttributeName(p)));
		}

		emptyline();
		//create sql staments
		//SQL-CREATE
		String sqlCreate1 = quote("INSERT INTO ");
		StringBuilder sqlCreate2 = new StringBuilder(quote(" ("));
		sqlCreate2.append("+" + getAttributeConst(id));
		for (MetaProperty p : properties) {
			sqlCreate2.append("+" + quote(", ") + "+" + getAttributeConst(p));
		}
		sqlCreate2.append("+" + quote(", ") + "+" + getAttributeConst(dao_created));
		StringBuilder sqlCreateEnd = new StringBuilder(") VALUES (");
		//+2 because of created flag and id.
		for (int i = 0; i < properties.size() + 2; i++) {
			sqlCreateEnd.append("?");
			if (i < properties.size() + 1)
				sqlCreateEnd.append(",");
		}
		sqlCreateEnd.append(")");
		sqlCreate2.append("+").append(quote(sqlCreateEnd));
		appendStatement(constDecl + " SQL_CREATE_1 \t= " + sqlCreate1);
		appendStatement(constDecl + " SQL_CREATE_2 \t= " + sqlCreate2);

		//SQL-UPDATE
		String sqlUpdate1 = quote("UPDATE ");
		StringBuilder sqlUpdate2 = new StringBuilder(quote(" SET "));
		for (MetaProperty p : properties) {
			sqlUpdate2.append(" + ").append(getAttributeConst(p)).append(" + ").append(quote(" = ?, "));
		}
		sqlUpdate2.append(" + ").append(getAttributeConst(dao_updated)).append(" + ").append(quote(" = ?"));
		sqlUpdate2.append(" + ").append(quote(" WHERE ")).append(" + ").append(getAttributeConst(id)).append(" + ").append(quote(" = ?"));

		appendStatement(constDecl + " SQL_UPDATE_1 \t= " + sqlUpdate1);
		appendStatement(constDecl + " SQL_UPDATE_2 \t= " + sqlUpdate2.toString());

		//SQL-DELETE
		String sqlDelete1 = quote("DELETE FROM ");
		String sqlDelete2 = quote(" WHERE ") + " + TABNAME +" + quote(".") + " + " + getAttributeConst(id) + " + " + quote(" = ?");
		appendStatement(constDecl + " SQL_DELETE_1 \t= " + sqlDelete1);
		appendStatement(constDecl + " SQL_DELETE_2 \t= " + sqlDelete2);

		//SQL-READ-ONE

		StringBuilder allAttrbutes = new StringBuilder("\"");
		allAttrbutes.append("+").append(getAttributeConst(id));
		for (MetaProperty p : properties) {
			allAttrbutes.append("+").append(quote(", ")).append("+").append(getAttributeConst(p));
		}
		allAttrbutes.append("+" + quote(", ") + "+" + getAttributeConst(dao_created));
		allAttrbutes.append("+" + quote(", ") + "+" + getAttributeConst(dao_updated));
		allAttrbutes.append("+\"");

		String sqlReadOne1 = quote("SELECT " + allAttrbutes + " FROM ");
		String sqlReadOne2 = quote(" WHERE ") + " + TABNAME +" + quote(".") + " + " + getAttributeConst(id) + " + " + quote(" = ?");
		appendStatement(constDecl + " SQL_READ_ONE_1 \t= " + sqlReadOne1);
		appendStatement(constDecl + " SQL_READ_ONE_2 \t= " + sqlReadOne2);

		//SQL-READ-ALL
		String sqlReadAll1 = quote("SELECT " + allAttrbutes + " FROM ");
		String sqlReadAll2 = quote(" ORDER BY id");
		appendStatement(constDecl + " SQL_READ_ALL_1 \t= " + sqlReadAll1);
		appendStatement(constDecl + " SQL_READ_ALL_2 \t= " + sqlReadAll2);

		//SQL-READ-ALL
		String sqlReadAllByProperty1 = quote("SELECT " + allAttrbutes + " FROM ");
		String sqlReadAllByProperty2 = quote(" WHERE ");
		appendStatement(constDecl + " SQL_READ_ALL_BY_PROPERTY_1 \t= " + sqlReadAllByProperty1);
		appendStatement(constDecl + " SQL_READ_ALL_BY_PROPERTY_2 \t= " + sqlReadAllByProperty2);


		// SQL_COUNT_1
		String sqlReadCount1 = quote("SELECT COUNT(id) FROM ");
		appendStatement(constDecl + " SQL_COUNT_1 \t= " + sqlReadCount1);

		// SQL_LIMIT_OFFSET_1
		String sqlReadLimit1 = quote(" LIMIT ?");
		String sqlReadOffcet1 = quote(" OFFSET ?");
		appendStatement(constDecl + " SQL_LIMIT_1 \t= " + sqlReadLimit1);
		appendStatement(constDecl + " SQL_OFFSET_1 \t= " + sqlReadOffcet1);

		emptyline();
		appendStatement("private RowMapper<" + doc.getName() + "> rowMapper = new " + doc.getName() + "RowMapper()");

		emptyline();
		//create impl


		if (moduleDbContextSensitive) {
			appendStatement("private Map<String,AtomicLong> lastIds = new HashMap<String,AtomicLong>()");
		} else {
			appendStatement("private AtomicLong lastId = new AtomicLong()");
		}
		//appendStatement("private static final long START_ID = 0");
		
		emptyline();
		appendStatement("private final JDBCConfig dbConfig");
		emptyline();
		//write out default constructor
		appendString("public " + getDAOName(doc) + "() {");
		increaseIdent();
		appendStatement("dbConfig = JDBCConfigFactory.getJDBCConfig()");
		closeBlockNEW();
		emptyline();
		//write out constructor with specified JDBC config
		appendString("public " + getDAOName(doc) + "(String jdbcConfig) {");
		increaseIdent();
		appendStatement("dbConfig = JDBCConfigFactory.getNamedJDBCConfig(jdbcConfig)");
		closeBlockNEW();
		emptyline();
		//get last id method
		appendString("private AtomicLong getLastId(Connection con) throws DAOException {");
		increaseIdent();
		if (moduleDbContextSensitive) {
			appendStatement("DBContext context = ContextManager.getCallContext().getDbContext()");
			appendStatement("String tableName = context.getTableNameInContext(TABNAME)");
			appendStatement("AtomicLong lastId = lastIds.get(tableName)");
			appendString("if (lastId==null){");
			increaseIdent();
			appendCommentLine("double-checked-locking");
			appendString("synchronized(lastIds){");
			increaseIdent();
			appendStatement("lastId = lastIds.get(tableName)");
			appendString("if (lastId==null){");
			increaseIdent();
			appendStatement("long maxId = getMaxId(con, tableName)");
			appendStatement("maxId = maxId >= dbConfig.getStartId() ? maxId : dbConfig.getStartId()");
			appendStatement("lastId = new AtomicLong(maxId)");
			appendStatement("lastIds.put(tableName, lastId)");
			closeBlockNEW();
			closeBlockNEW();
			closeBlockNEW();
			appendStatement("return lastId");

		} else {
			appendStatement("return lastId");

		}
		closeBlockNEW();
		emptyline();

		//get last id method
		appendString("private void adjustLastId(Connection con, long lastIdValue) throws DAOException {");
		increaseIdent();
		if (moduleDbContextSensitive) {
			appendStatement("throw new RuntimeException(\"Not yet implemented\")");
		} else {
			appendString("if (lastId.get()<lastIdValue)");
			appendIncreasedStatement("lastId.set(lastIdValue)");
		}
		closeBlockNEW();
		emptyline();


		//createSQL Method
		appendString("private String createSQL(String sql1, String sql2){");
		increaseIdent();
		if (moduleDbContextSensitive) {
			appendStatement("DBContext context = ContextManager.getCallContext().getDbContext()");
			appendStatement("StringBuilder sql = new StringBuilder()");
			appendStatement("sql.append(sql1).append(context.getTableNameInContext(TABNAME)).append(sql2)");
			appendStatement("return sql.toString()");
		} else {
			appendStatement("StringBuilder sql = new StringBuilder()");
			appendStatement("sql.append(sql1).append(TABNAME).append(sql2)");
			appendStatement("return sql.toString()");
		}
		closeBlockNEW();
		emptyline();

		String throwsClause = " throws DAOException";
		String callLog = "";

		//get all XYZ method
		callLog = quote("get" + doc.getMultiple() + "(") + "+con+" + quote(")");
		appendComment("Returns all " + doc.getMultiple() + " objects stored.");
		openFun("public List<" + doc.getName() + ">" + " get" + doc.getMultiple() + "(Connection con)" + throwsClause);
		generateFunctionStart("SQL_READ_ALL", callLog, true, true);
		appendStatement("result = ps.executeQuery()");
		appendStatement("ArrayList<" + doc.getName() + "> ret = new ArrayList<" + doc.getName() + ">()");
		appendString("while(result.next())");
		appendIncreasedStatement("ret.add(rowMapper.map(result))");
		appendStatement("return  ret");
		generateFunctionEnd(callLog, true, true);
		closeBlockNEW();
		emptyline();

		appendComment("Deletes a " + doc.getName() + " object by id.");
		callLog = quote("delete" + doc.getName() + "(") + "+con+" + quote(", ") + "+id+" + quote(")");

		openFun("public void delete" + doc.getName() + "(Connection con, String id)" + throwsClause);
		generateFunctionStart("SQL_DELETE", callLog, true, false);
		appendStatement("ps.setLong(1, Long.parseLong(id))");
		appendStatement("int rows = ps.executeUpdate()");
		appendString("if (rows!=1 && rows!=0){");
		increaseIdent();
		appendStatement("log.warn(\"Deleted more than one row of " + doc.getName() + ": \"+id)");
		closeBlockNEW();
		generateFunctionEnd(callLog, true, false);
		closeBlockNEW();
		emptyline();

		//deleteListXYZ method
		String listDecl = "List<" + doc.getName() + ">";
		appendComment("Deletes multiple " + doc.getName() + " objects.");
		callLog = quote("delete" + doc.getMultiple() + "(") + "+con+" + quote(", ") + "+list+" + quote(")");

		openFun("public void delete" + doc.getMultiple() + "(Connection con, " + listDecl + " list)" + throwsClause);
		appendStatement("PreparedStatement ps = null");
		appendString("try{");
		increaseIdent();
		appendStatement("con.setAutoCommit(false)");
		appendStatement("ps = con.prepareStatement(createSQL(SQL_DELETE_1, SQL_DELETE_2))");
		appendString("for (" + doc.getName() + " " + doc.getVariableName() + " : list){");
		increaseIdent();
		appendStatement("ps.setLong(1, Long.parseLong(" + doc.getVariableName() + ".getId()))");
		appendStatement("int rows = ps.executeUpdate()");
		appendString("if (rows!=1 && rows!=0){");
		increaseIdent();
		appendStatement("log.warn(\"Deleted more than one row of " + doc.getName() + ": \"+" + doc.getVariableName() + ".getId())");
		closeBlockNEW();
		closeBlockNEW();
		appendStatement("con.commit()");
		generateFunctionEnd(callLog, true, false);
		closeBlockNEW();
		emptyline();

		//getXYZ method
		callLog = quote("get" + doc.getName() + "(") + "+con+" + quote(", ") + "+id+" + quote(")");
		appendComment("Returns the " + doc.getName() + " object with the specified id.");
		openFun("public " + doc.getName() + " get" + doc.getName() + "(Connection con, String id)" + throwsClause);
		appendNullCheck("con", "Null arg: con");
		appendNullCheck("id", "Null arg: id");
		generateFunctionStart("SQL_READ_ONE", callLog, true, true);
		appendStatement("ps.setLong(1, Long.parseLong(id))");
		appendStatement("result = ps.executeQuery()");
		appendString("if (!result.next())");
		appendIncreasedStatement("throw new " + getNoItemExceptionName(doc) + "(id)");
		appendStatement("return rowMapper.map(result)");
		generateFunctionEnd(callLog, true, true);
		closeBlockNEW();
		emptyline();

		int ii = 0;

		//ImportXYZ method
		callLog = quote("import" + doc.getName() + "(") + "+con+" + quote(", ") + "+" + doc.getVariableName() + "+" + quote(")");
		appendComment("Imports a new " + doc.getName() + " object.\nReturns the imported version.");
		openFun("public " + doc.getName() + " import" + doc.getName() + "(Connection con, " + doc.getName() + " " + doc.getVariableName() + ")" + throwsClause);
		generateFunctionStart("SQL_CREATE", callLog, true, false);
		//appendStatement("long nextId = getLastId(con).incrementAndGet()"));
		appendStatement("ps.setLong(1, Long.parseLong(" + doc.getVariableName() + ".getId()))");
		for (int i = 0; i < properties.size(); i++) {
			generateDB2PropertyMapping(doc.getVariableName(), properties.get(i), i + 2);
			ii = i + 2;
		}
		appendCommentLine("set create timestamp");
		appendStatement("ps.setLong(" + (ii + 1) + ", System.currentTimeMillis())");

		appendStatement("int rows = ps.executeUpdate()");
		appendString("if (rows!=1)");
		appendIncreasedStatement("throw new DAOException(\"Create failed, updated rows: \"+rows)");

		String copyResVarName = "new" + StringUtils.capitalize(doc.getVariableName());
		String createCopyCall = VOGenerator.getDocumentImplName(doc) + " " + copyResVarName + " = new " + VOGenerator.getDocumentImplName(doc);
		createCopyCall += "(" + doc.getVariableName() + ".getId())";
		appendStatement(createCopyCall);
		appendStatement(copyResVarName + ".copyAttributesFrom(" + doc.getVariableName() + ")");
		appendStatement("adjustLastId(con, Long.parseLong(" + doc.getVariableName() + ".getId()))");

		appendStatement("return " + copyResVarName);
		generateFunctionEnd(callLog, true, false);

		closeBlockNEW();
		emptyline();

		ii = 0;

		//ImportXYZ method List<>
		callLog = quote("import " + doc.getMultiple() + "(") + "+con+" + quote(", ") + "+list+" + quote(")");
		appendComment("Imports multiple new " + doc.getName() + " objects.\nReturns the imported versions.");
		openFun("public " + listDecl + " import" + doc.getMultiple() + "(Connection con," + listDecl + " list)" + throwsClause);
		appendStatement("PreparedStatement ps = null");
		appendString("try{");
		increaseIdent();
		appendStatement("con.setAutoCommit(false)");
		appendStatement("ps = con.prepareStatement(createSQL(SQL_CREATE_1, SQL_CREATE_2))");
		appendStatement(listDecl + " ret = new ArrayList<" + doc.getName() + ">()");
		appendString("for (" + doc.getName() + " " + doc.getVariableName() + " : list){");
		increaseIdent();
		appendStatement("ps.setLong(1, Long.parseLong(" + doc.getVariableName() + ".getId()))");
		for (int i = 0; i < properties.size(); i++) {
			generateDB2PropertyMapping(doc.getVariableName(), properties.get(i), i + 2);
			ii = i + 2;
		}
		appendCommentLine("set create timestamp");

		appendStatement("ps.setLong(" + (ii + 1) + ", System.currentTimeMillis())");
		appendStatement("int rows = ps.executeUpdate()");
		appendString("if (rows!=1)");
		appendIncreasedStatement("throw new DAOException(\"Create failed, updated rows: \"+rows)");

		copyResVarName = "new" + StringUtils.capitalize(doc.getVariableName());
		createCopyCall = VOGenerator.getDocumentImplName(doc) + " " + copyResVarName + " = new " + VOGenerator.getDocumentImplName(doc);
		createCopyCall += "(" + doc.getVariableName() + ".getId())";
		appendStatement(createCopyCall);
		appendStatement(copyResVarName + ".copyAttributesFrom(" + doc.getVariableName() + ")");
		appendStatement("adjustLastId(con, Long.parseLong(" + doc.getVariableName() + ".getId()))");
		appendStatement("ret.add(" + copyResVarName + ")");
		closeBlockNEW();
		appendStatement("con.commit()");
		appendStatement("return ret");
		generateFunctionEnd(callLog, true, false);
		closeBlockNEW();
		emptyline();


		ii = 0;

		//createXYZ method
		callLog = quote("create" + doc.getName() + "(") + "+con+" + quote(", ") + "+" + doc.getVariableName() + "+" + quote(")");
		appendComment("Creates a new " + doc.getName() + " object.\nReturns the created version.");
		openFun("public " + doc.getName() + " create" + doc.getName() + "(Connection con, " + doc.getName() + " " + doc.getVariableName() + ")" + throwsClause);
		appendStatement("java.sql.SQLException throwable = null");
		appendString("for (int recoveryAttempt = 1; recoveryAttempt <= dbConfig.getIdRecoveryAttempts(); recoveryAttempt++) {");
		increaseIdent();
		appendStatement("PreparedStatement ps = null");
		openTry();
		appendStatement("con.setAutoCommit(false)");
		appendStatement("ps = con.prepareStatement(createSQL(SQL_CREATE_1, SQL_CREATE_2))");
		appendStatement("long nextId = getLastId(con).incrementAndGet()");
		appendStatement("ps.setLong(1, nextId)");
		for (int i = 0; i < properties.size(); i++) {
			generateDB2PropertyMapping(doc.getVariableName(), properties.get(i), i + 2);
			ii = i + 2;
		}
		appendCommentLine("set create timestamp");
		appendStatement("ps.setLong(" + (ii + 1) + ", System.currentTimeMillis())");

		appendStatement("int rows = ps.executeUpdate()");
		appendString("if (rows!=1)");
		appendIncreasedStatement("throw new DAOException(\"Create failed, updated rows: \"+rows)");
		copyResVarName = "new" + StringUtils.capitalize(doc.getVariableName());
		createCopyCall = VOGenerator.getDocumentImplName(doc) + " " + copyResVarName + " = new " + VOGenerator.getDocumentImplName(doc);
		createCopyCall += "(\"\"+nextId)";
		appendStatement(createCopyCall);
		appendStatement(copyResVarName + ".copyAttributesFrom(" + doc.getVariableName() + ")");
		appendStatement("con.commit()");
		appendStatement("return " + copyResVarName);
		decreaseIdent();
		appendString("} catch (SQLException e) {");
		increaseIdent();
		appendStatement("getLastId(con).set(getMaxId(con,TABNAME))");
		appendStatement("log.warn(\"Failed attempt\" +recoveryAttempt+ \" from \" +dbConfig.getIdRecoveryAttempts()+ \" to create new entry in \"+TABNAME+\" table\", e)");
		appendStatement("throwable = e");
		appendStatement("continue");
		decreaseIdent();
		appendString("} finally {");
		increaseIdent();		
		appendStatement("net.anotheria.db.util.JDBCUtil.release(ps)");
		closeBlockNEW();
		closeBlockNEW();
		appendStatement("log.error(\"All \"+ dbConfig.getIdRecoveryAttempts()+\" attempt of id rereading - Failed. \"+" + callLog + ", throwable)");
		appendStatement("throw new DAOSQLException(throwable)");
		closeBlockNEW();
		emptyline();


		//createListXYZ method
//        String listDecl = "List<"+doc.getName()+">";
		callLog = quote("create" + doc.getMultiple() + "(") + "+con+" + quote(", ") + "+list+" + quote(")");
		appendComment("Creates multiple new " + doc.getName() + " objects.\nReturns the created versions.");
		openFun("public " + listDecl + " create" + doc.getMultiple() + "(Connection con, " + listDecl + " list)" + throwsClause);
		appendStatement("java.sql.SQLException throwable = null");
		appendString("for (int recoveryAttempt = 1; recoveryAttempt <= dbConfig.getIdRecoveryAttempts(); recoveryAttempt++) {");
		increaseIdent();
		appendStatement("PreparedStatement ps = null");
		appendString("try{");
		increaseIdent();
		appendStatement("con.setAutoCommit(false)");
		appendStatement("ps = con.prepareStatement(createSQL(SQL_CREATE_1, SQL_CREATE_2))");
		appendStatement(listDecl + " ret = new ArrayList<" + doc.getName() + ">()");
		appendString("for (" + doc.getName() + " " + doc.getVariableName() + " : list){");
		increaseIdent();
		appendStatement("long nextId = getLastId(con).incrementAndGet()");
		appendStatement("ps.setLong(1, nextId)");
		for (int i = 0; i < properties.size(); i++) {
			generateDB2PropertyMapping(doc.getVariableName(), properties.get(i), i + 2);
			ii = i + 2;
		}
		appendCommentLine("set create timestamp");
		appendStatement("ps.setLong(" + (ii + 1) + ", System.currentTimeMillis())");

		appendStatement("int rows = ps.executeUpdate()");
		appendString("if (rows!=1)");
		appendIncreasedStatement("throw new DAOException(\"Create failed, updated rows: \"+rows)");

		/*String*/
		copyResVarName = "new" + StringUtils.capitalize(doc.getVariableName());
		/*String*/
		createCopyCall = VOGenerator.getDocumentImplName(doc) + " " + copyResVarName + " = new " + VOGenerator.getDocumentImplName(doc);
		createCopyCall += "(\"\"+nextId)";
		appendStatement(createCopyCall);
		appendStatement(copyResVarName + ".copyAttributesFrom(" + doc.getVariableName() + ")");

		appendStatement("ret.add(" + copyResVarName + ")");
		closeBlockNEW();
		appendStatement("con.commit()");
		appendStatement("return ret");
		decreaseIdent();
		appendString("} catch (SQLException e) {");
		increaseIdent();
		appendStatement("getLastId(con).set(getMaxId(con,TABNAME))");
		appendStatement("log.warn(\"Failed attempt\" +recoveryAttempt+ \" from \" +dbConfig.getIdRecoveryAttempts()+ \" to create new entries (list) in \"+TABNAME+\" table\", e)");
		appendStatement("throwable = e");
		appendStatement("continue");
		decreaseIdent();
		appendString("} finally {");
		increaseIdent();
		appendStatement("net.anotheria.db.util.JDBCUtil.release(ps)");
		closeBlockNEW();
		closeBlockNEW();
		appendStatement("log.error(\"All \"+ dbConfig.getIdRecoveryAttempts()+\" attempt of id rereading - Failed. \"+" + callLog + ", throwable)");
		appendStatement("throw new DAOSQLException(throwable)");
		closeBlockNEW();
		emptyline();

		//updateXYZ method
		callLog = quote("update" + doc.getName() + "(") + "+con+" + quote(", ") + "+" + doc.getVariableName() + "+" + quote(")");
		appendComment("Updates a " + doc.getName() + " object.\nReturns the updated version.");
		openFun("public " + doc.getName() + " update" + doc.getName() + "(Connection con, " + doc.getName() + " " + doc.getVariableName() + ")" + throwsClause);
		generateFunctionStart("SQL_UPDATE", callLog, true, false);

		for (int i = 0; i < properties.size(); i++) {
			generateDB2PropertyMapping(doc.getVariableName(), properties.get(i), i + 1);
			ii = i + 1;
		}
		appendCommentLine("set update timestamp");
		appendStatement("ps.setLong(" + (ii + 1) + ", System.currentTimeMillis())");
		appendCommentLine("set id for the where clause");
		appendStatement("ps.setLong(" + (ii + 2) + ", Long.parseLong(" + doc.getVariableName() + ".getId()))");

		appendStatement("int rows = ps.executeUpdate()");
		appendString("if (rows!=1)");
		appendIncreasedStatement("throw new DAOException(\"Update failed, updated rows: \"+rows)");

		appendStatement("return " + doc.getVariableName());
		generateFunctionEnd(callLog, true, false);
		closeBlockNEW();
		emptyline();


		//updateListXYZ method
		callLog = quote("update" + doc.getMultiple() + "(") + "+con+" + quote(", ") + "+list+" + quote(")");
		appendComment("Updates multiple new " + doc.getName() + " objects.\nReturns the updated versions.");
		openFun("public " + listDecl + " update" + doc.getMultiple() + "(Connection con, " + listDecl + " list)" + throwsClause);
		appendStatement("PreparedStatement ps = null");
		appendString("try{");
		increaseIdent();
		appendStatement("con.setAutoCommit(false)");
		appendStatement("ps = con.prepareStatement(createSQL(SQL_UPDATE_1, SQL_UPDATE_2))");
		appendStatement(listDecl + " ret = new ArrayList<" + doc.getName() + ">()");
		appendString("for (" + doc.getName() + " " + doc.getVariableName() + " : list){");
		increaseIdent();
		for (int i = 0; i < properties.size(); i++) {
			generateDB2PropertyMapping(doc.getVariableName(), properties.get(i), i + 1);
			ii = i + 1;
		}
		appendCommentLine("set update timestamp");
		appendStatement("ps.setLong(" + (ii + 1) + ", System.currentTimeMillis())");
		appendCommentLine("set id for the where clause");
		appendStatement("ps.setLong(" + (ii + 2) + ", Long.parseLong(" + doc.getVariableName() + ".getId()))");

		appendStatement("int rows = ps.executeUpdate()");
		appendString("if (rows!=1)");
		appendIncreasedStatement("throw new DAOException(\"Update failed, updated rows: \"+rows)");

		closeBlockNEW();
		appendStatement("con.commit()");
		appendStatement("return list");
		generateFunctionEnd(callLog, true, false);

		closeBlockNEW();
		emptyline();
		//end updateListXYZ

		//get all XYZ byProperty method
		callLog = quote("get" + doc.getMultiple() + "ByProperty(") + "+con+" + quote(",") + "+ properties+" + quote(")");
		appendComment("Returns all " + doc.getMultiple() + " objects stored which matches given properties.");
		openFun("public List<" + doc.getName() + ">" + " get" + doc.getMultiple() + "ByProperty(Connection con, List<QueryProperty> properties)" + throwsClause);
		//append(generateFunctionStart("SQL_READ_ALL_BY_PROPERTY", callLog, true));
		appendStatement("PreparedStatement ps = null");
		appendStatement("ResultSet result = null");
		openTry();
		//TODO Caching fuer generierte SQL Statements
		appendCommentLine("//enable caching of statements one day");
		appendStatement("String SQL = createSQL(SQL_READ_ALL_BY_PROPERTY_1, SQL_READ_ALL_BY_PROPERTY_2)");
		appendStatement("String whereClause = " + quote(""));
		appendString("for (QueryProperty p : properties){");
		increaseIdent();
		appendString("if (whereClause.length()>0)");
		appendIncreasedStatement("whereClause += " + quote(" AND "));
		appendStatement("String statement = p.unprepaireable()? (String) p.getValue(): " + quote("?"));
		//http://infra.anotheria.net:9080/jira/browse/ANODOC-8
		appendStatement("whereClause += p.getName().toLowerCase()+p.getComparator()+statement");
		closeBlockNEW();
		appendStatement("SQL += whereClause");
		//appendStatement("System.out.println(SQL)"));
		appendStatement("ps = con.prepareStatement(SQL)");
		//set properties
		appendStatement("int propertyPosition = 0");
		appendString("for (QueryProperty property: properties){");
		increaseIdent();
		appendString("if(property.unprepaireable())");
		appendIncreasedStatement("continue");
		appendStatement("setProperty(++propertyPosition, ps, property)");
		closeBlockNEW();

		appendStatement("result = ps.executeQuery()");
		appendStatement("ArrayList<" + doc.getName() + "> ret = new ArrayList<" + doc.getName() + ">()");
		appendString("while(result.next())");
		appendIncreasedStatement("ret.add(rowMapper.map(result))");
		appendStatement("return  ret");
		generateFunctionEnd(callLog, true, true);
		closeBlockNEW();
		emptyline();

		// get elements COUNT
		callLog = quote("get" + doc.getMultiple() + "Count(") + " + con + " + quote(")");
		appendComment("Returns " + doc.getMultiple() + " objects count.");
		openFun("public int get" + doc.getMultiple() + "Count(Connection con)" + throwsClause);
		appendStatement("PreparedStatement ps = null");
		appendStatement("ResultSet result = null");
		openTry();
		appendStatement("ps = con.prepareStatement(SQL_COUNT_1 + TABNAME)");
		appendStatement("result = ps.executeQuery()");
		appendStatement("int pCount = 0");
		appendString("if (result.next())");
		appendIncreasedStatement("pCount = result.getInt(1)");
		appendStatement("return pCount");
		generateFunctionEnd(callLog, true, true);
		closeBlockNEW();
		emptyline();
		// end get elements COUNT

		// get elements Segment
		callLog = quote("get" + doc.getMultiple() + "(") + " + con + " + quote(",") + "+ aSegment +" + quote(")");
		appendComment("Returns " + doc.getMultiple() + " objects segment.");
		openFun("public List<" + doc.getName() + ">" + " get" + doc.getMultiple() + "(Connection con, Segment aSegment)" + throwsClause);
		generateFunctionStartWithLimitAndOffset("SQL_READ_ALL");
		appendStatement("int pLimit = aSegment.getElementsPerSlice()");
		appendStatement("int pOffset = aSegment.getSliceNumber() * aSegment.getElementsPerSlice() - aSegment.getElementsPerSlice()");
		appendStatement("ps.setInt(1, pLimit)");
		appendStatement("ps.setInt(2, pOffset)");
		appendStatement("result = ps.executeQuery()");
		appendStatement("ArrayList<" + doc.getName() + "> ret = new ArrayList<" + doc.getName() + ">()");
		appendString("while(result.next())");
		appendIncreasedStatement("ret.add(rowMapper.map(result))");
		appendStatement("return  ret");
		generateFunctionEnd(callLog, true, true);
		closeBlockNEW();
		emptyline();
		// end get elements Segment

		// get elements Segment with FILTER
		callLog = quote("get" + doc.getMultiple() + "ByProperty(") + " + con + " + quote(",") + " + aSegment + " + quote(",")
				+ " + properties + " + quote(")");
		appendComment("Returns " + doc.getMultiple() + " objects segment which matches given properties.");
		openFun("public List<" + doc.getName() + ">" + " get" + doc.getMultiple()
				+ "ByProperty(Connection con, Segment aSegment, List<QueryProperty> properties)" + throwsClause);
		appendStatement("PreparedStatement ps = null");
		appendStatement("ResultSet result = null");
		openTry();
		//TODO Caching fuer generierte SQL Statements
		appendCommentLine("//enable caching of statements one day");
		appendStatement("String SQL = createSQL(SQL_READ_ALL_BY_PROPERTY_1, SQL_READ_ALL_BY_PROPERTY_2)");
		appendStatement("String whereClause = " + quote(""));
		appendString("for (QueryProperty p : properties){");
		increaseIdent();
		appendString("if (whereClause.length()>0)");
		appendIncreasedStatement("whereClause += " + quote(" AND "));
		appendStatement("String statement = p.unprepaireable()? (String) p.getValue(): " + quote("?"));
		appendStatement("whereClause += p.getName()+p.getComparator()+statement");
		closeBlockNEW();
		appendStatement("SQL += whereClause");
		appendStatement("SQL += SQL_READ_ALL_2 + SQL_LIMIT_1 + SQL_OFFSET_1");
		appendStatement("ps = con.prepareStatement(SQL)");
		appendStatement("int propertyPosition = 0");
		appendString("for (QueryProperty property: properties){");
		increaseIdent();
		appendString("if(property.unprepaireable())");
		appendIncreasedStatement("continue");
		appendStatement("setProperty(++propertyPosition, ps, property)");
		closeBlockNEW();
		appendStatement("int pLimit = aSegment.getElementsPerSlice()");
		appendStatement("int pOffset = aSegment.getSliceNumber() * aSegment.getElementsPerSlice() - aSegment.getElementsPerSlice()");
		appendStatement("ps.setInt(++propertyPosition, pLimit)");
		appendStatement("ps.setInt(++propertyPosition, pOffset)");
		appendStatement("result = ps.executeQuery()");
		appendStatement("ArrayList<" + doc.getName() + "> ret = new ArrayList<" + doc.getName() + ">()");
		appendString("while(result.next())");
		appendIncreasedStatement("ret.add(rowMapper.map(result))");
		appendStatement("return  ret");
		generateFunctionEnd(callLog, true, true);
		closeBlockNEW();
		emptyline();
		// end get elements Segment with FILTER

		//setProperty
		openFun("private void setProperty(int position, PreparedStatement ps, QueryProperty property) throws SQLException");
		appendString("if(property.unprepaireable()){");
		increaseIdent();
		appendStatement("return");
		closeBlockNEW();
		for (MetaProperty p : properties) {
			appendString("if (" + getAttributeConst(p) + ".equals(property.getName().toLowerCase())){");
			increaseIdent();
			appendStatement(getDB2PropertyCallMapping("property.getValue()", p, "position"));
			appendStatement("return");
			closeBlockNEW();
		}
		MetaProperty rawId = new MetaProperty("id", MetaProperty.Type.LONG);
		appendString("if (" + getAttributeConst(id) + ".equals(property.getName())){");
		increaseIdent();
		appendStatement(getDB2PropertyCallMapping("property.getValue()", rawId, "position"));
		appendStatement("return");
		closeBlockNEW();
		appendString("if (" + quote(dao_created.getName()) + ".equals(property.getName())){");
		increaseIdent();
		appendStatement(getDB2PropertyCallMapping("property.getValue()", dao_created, "position"));
		appendStatement("return");
		closeBlockNEW();
		appendString("if (" + quote(dao_updated.getName()) + ".equals(property.getName())){");
		increaseIdent();
		appendStatement(getDB2PropertyCallMapping("property.getValue()", dao_updated, "position"));
		appendStatement("return");
		closeBlockNEW();

		closeBlockNEW(); //end setProperty

		appendString("/* ---------- SQL --------- ");
		generateSQLCreate(doc, dao_created, dao_updated);
		appendString("   ---------- SQL --------- */");

		openFun("public void createStructure(Connection connection) " + throwsClause);
		appendCommentLine("not implemented");
		closeBlockNEW();
		emptyline();

		appendString("/* ---------- SQL --------- ");
		generateSQLDelete(doc);
		appendString("   ---------- SQL --------- */");
		openFun("public void deleteStructure(Connection connection) " + throwsClause);
		appendCommentLine("not implemented");
		closeBlockNEW();
		emptyline();

		openFun("private long getMaxId(Connection con, String tableName) " + throwsClause);
		appendStatement("Statement st = null");
		appendStatement("ResultSet result = null");
		openTry();
		appendStatement("con.setAutoCommit(true)");
		appendStatement("st = con.createStatement()");
		appendStatement("st.execute(\"SELECT MAX(\"+" + getAttributeConst(id) + "+\") FROM \"+tableName)");
		appendStatement("result = st.getResultSet()");
		appendStatement("long maxId = 0");
		appendString("if (result.next())");
		appendIncreasedStatement("maxId = result.getLong(1)");
		appendStatement("log.info(\"maxId in table \"+tableName+\" is \"+maxId)");
		appendStatement("return maxId");

		generateFunctionEnd(quote("getMaxId(") + "+con+" + quote(", ") + "+tableName+" + quote(")"), false, true);
		closeBlockNEW();
		emptyline();

		//init() method
		openFun("public void init(Connection con) " + throwsClause);
		appendStatement("log.debug(\"Called: init(\"+con+\")\")");
		if (!moduleDbContextSensitive) {
			appendStatement("long maxId = getMaxId(con, TABNAME)");
			appendStatement("maxId = maxId >= dbConfig.getStartId() ? maxId : dbConfig.getStartId()");
			appendStatement("lastId = new AtomicLong(maxId)");
		}
		closeBlockNEW();

		return clazz;
	}

	private void generateSQLDelete(MetaDocument doc) {
		appendString("DROP TABLE " + getSQLTableName(doc));
	}

	private void generateSQLCreate(MetaDocument doc, MetaProperty... additionalProps) {
		appendString("CREATE TABLE " + getSQLTableName(doc) + "(");
		appendString("id int8 PRIMARY KEY,");
		for (int i = 0; i < doc.getProperties().size(); i++) {
			appendString(getSQLPropertyDefinition(doc.getProperties().get(i)) + ",");
		}
		for (int i = 0; i < doc.getLinks().size(); i++) {
			appendString(getSQLPropertyDefinition(doc.getLinks().get(i)) + ",");
		}
		for (int i = 0; i < additionalProps.length - 1; i++)
			appendString(getSQLPropertyDefinition(additionalProps[i]) + ",");
		appendString(getSQLPropertyDefinition(additionalProps[additionalProps.length - 1]));

		appendString(")");
	}

	private String getSQLPropertyDefinition(MetaProperty p) {
		return getAttributeName(p) + " " + getSQLPropertyType(p);
	}

	/**
	 * This method maps MetaProperties Types to SQL DataTypes.
	 *
	 * @param p
	 * @return
	 */
	private String getSQLPropertyType(MetaProperty p) {
		
		switch (p.getType()) {
		case STRING:
			return "varchar";
		case TEXT:
			return "varchar";
		case LONG:
			return "int8";
		case INT:
			return "int";
		case DOUBLE:
			return "double precision";
		case FLOAT:
			return "float4";
		case BOOLEAN:
			return "boolean";
		default:
			return "UNKNOWN!";
		}
		
	}

	private String getSQLTableName(MetaDocument doc) {
		return doc.getName().toLowerCase();
	}

	private void generateFunctionStart(String SQL_STATEMENT, String callLog, boolean usePreparedSt, boolean isNeedResultSet) {
		if (usePreparedSt) {
			appendStatement("PreparedStatement ps = null");
			if (isNeedResultSet)
				appendStatement("ResultSet result = null");
			openTry();
			appendStatement("con.setAutoCommit(true)");
			appendStatement("ps = con.prepareStatement(createSQL(" + SQL_STATEMENT + "_1, " + SQL_STATEMENT + "_2))");
		} else {
			appendStatement("Statement st = null");
			if (isNeedResultSet)
				appendStatement("ResultSet result = null");
			openTry();
			appendStatement("con.setAutoCommit(true)");
		}
	}

	private void generateFunctionStartWithLimitAndOffset(String SQL_STATEMENT) {
		appendStatement("PreparedStatement ps = null");
		appendStatement("ResultSet result = null");
		openTry();
		appendStatement("con.setAutoCommit(true)");
		appendStatement("ps = con.prepareStatement(createSQL(" + SQL_STATEMENT + "_1, " + SQL_STATEMENT + "_2)" + " + SQL_LIMIT_1"
				+ " + SQL_OFFSET_1" + ")");
	}

	private void generateFunctionEnd(String callLog, boolean usePreparedSt, boolean isCloseResultSet) {
		decreaseIdent();
		appendString("} catch (SQLException e) {");
		increaseIdent();
		appendStatement("log.error(" + callLog + ", e)");
		appendStatement("throw new DAOSQLException(e)");
		decreaseIdent();
		appendString("} finally {");
		increaseIdent();
		if (isCloseResultSet)
			appendStatement("net.anotheria.db.util.JDBCUtil.release(result)");
		appendStatement("net.anotheria.db.util.JDBCUtil.release(" + (usePreparedSt ? "ps" : "st") + ")");
		closeBlockNEW();
	}


	public static final String getExceptionName(MetaDocument doc) {
		return getDAOName(doc) + "Exception";
	}

	public static final String getNoItemExceptionName(MetaDocument doc) {
		return getDAOName(doc) + "NoItemForIdFoundException";
	}

	public static String getDAOName(MetaDocument doc) {
		return doc.getName() + "DAO";
	}

	public static String getRowMapperName(MetaDocument doc) {
		return doc.getName() + "RowMapper";
	}
}
