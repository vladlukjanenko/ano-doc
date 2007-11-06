package net.anotheria.anodoc.query;

import java.io.Serializable;
import java.util.HashSet;

/**
 * This predicate implementation is a "match" implementation.<br>
 * i.e. the value is only matched with leading and following wildcard(*), 
 * it is not checked for equality
 */
public class BasicPredicate extends Predicate implements Serializable{

	private Path p;
	private String field;
	private Object value;
	
	/**
	 * @param aPath the path to the object to check(can be null, then the current document will be taken)
	 * @param aField the name of the field to check(i.e. the member name in the object or the property name used in the document)
	 * @param aValue the value to compare the field value to
	 */
	public BasicPredicate(Path aPath, String aField, Object aValue){
		p = aPath;
		field = aField;
		value = aValue;
	}
	
	/**
	 * @see biz.beaglesoft.bgldoc.query.Predicate#toOQL()
	 */
	public String toOQL(String var) {
		return p == null ?
				var+"."+field +" LIKE \"*"+value+"*\"" :
				"v"+p.id+"."+field +" LIKE \"*"+value+"*\"";
	}

	/**
	 * @see biz.beaglesoft.bgldoc.query.Predicate#toVarDecl()
	 */
	public String toVarDecl(String var, HashSet<String> knownVars) {
		if(p == null){
			return "";
		}
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
