package net.anotheria.anodoc.query;

import java.util.HashSet;


/**
 * Negation of a predicate. You get an instance of this class when you call 
 * <code>{@link biz.beaglesoft.bgldoc.query.Predicate#not()}</code> 
 */
public class NotPredicate extends Predicate implements java.io.Serializable{

	private Predicate p;

	NotPredicate(Predicate aPredicate){
		p = aPredicate;
	}
	
	/**
	 * @see biz.beaglesoft.bgldoc.query.Predicate#toOQL()
	 */
	public String toOQL(String var) {
		return "NOT ("+p.toOQL(var)+")";
	}

	/**
	 * @see biz.beaglesoft.bgldoc.query.Predicate#toVarDecl(java.lang.String)
	 */
	public String toVarDecl(String var, HashSet vars) {
		return p.toVarDecl(var,vars);
	}

}
