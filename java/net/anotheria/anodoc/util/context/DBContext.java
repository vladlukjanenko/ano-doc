package net.anotheria.anodoc.util.context;

import java.io.Serializable;

/**
 * A part of CallContext used for DB controlling. Its useful if you have multiple tables for same purposes and want to perform selection outside of the dao.
 * @author another
 *
 */
public class DBContext implements Serializable{
	/**
	 * Prefix of the table name.
	 */
	private String tableNamePrefix;
	/**
	 * Postfix of the table name.
	 */
	private String tableNamePostfix;
	
	public static final String DELIMITER = "_";
	/**
	 * Creates a new DBContext.
	 */
	public DBContext(){
		tableNamePostfix = tableNamePrefix = "";
	}
	
	public String getTableNamePostfix() {
		return tableNamePostfix;
	}
	public void setTableNamePostfix(String tableNamePostfix) {
		this.tableNamePostfix = tableNamePostfix;
	}
	public String getTableNamePrefix() {
		return tableNamePrefix;
	}
	public void setTableNamePrefix(String tableNamePrefix) {
		this.tableNamePrefix = tableNamePrefix;
	}
	
	public String getTableNameInContext(String tableName){
		StringBuilder ret = new StringBuilder();
		if (tableNamePrefix!=null && tableNamePrefix.length()>0)
			ret.append(tableNamePrefix).append(DELIMITER);
		ret.append(tableName);
		if (tableNamePostfix!=null && tableNamePostfix.length()>0)
			ret.append(tableNamePostfix).append(DELIMITER);
		
		return ret.toString();
	}
}
