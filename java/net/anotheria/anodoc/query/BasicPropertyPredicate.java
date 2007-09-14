package net.anotheria.anodoc.query;

import java.io.Serializable;
import java.util.HashSet;

/**
 * This predicate implementation is a "match" implementation.<br>
 * i.e. the value is only matched with leading and following wildcard(*), 
 * it is not checked for equality<br>
 * Since this predicate is supposed to define a query over a property the path 
 * must not be null in the constructor.
 */
public class BasicPropertyPredicate extends Predicate implements Serializable{

	private Path p;
	private String name;
	private Object value;
	
	/**
	 * @param aPath the path to the property to check
	 * @param aField the name of the field to check(i.e. the property name in the document)
	 * @param aValue the value to compare the field value to
	 */
	public BasicPropertyPredicate(Path aPath, String aName, Object aValue) throws IllegalArgumentException{
		if(aPath == null){
			throw new IllegalArgumentException("path must not be null");
		}
		p = aPath;
		name = aName;
		value = aValue;
	}
	
	/**
	 * @see biz.beaglesoft.bgldoc.query.Predicate#toOQL()
	 */
	public String toOQL(String var) {
		return "v"+p.id+".name = \""+name+"\" AND "+"v"+p.id+".value LIKE \"*"+value+"*\""; 
	}

	/**
	 * @see biz.beaglesoft.bgldoc.query.Predicate#toVarDecl()
	 */
	public String toVarDecl(String var, HashSet knownVars) {
		if(knownVars.contains("v"+p.id)){
			return "";
		}
		knownVars.add("v"+p.id);
		StringBuffer result = new StringBuffer();
		String path = p.toString();
		path = path.substring(0,path.length()-1);
		result.append(var).append('.').append(path).append(" as ");
		result.append("v").append(p.id);
		return result.toString();
	}

}
