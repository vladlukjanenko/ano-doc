package net.anotheria.anodoc.query;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Conjunction of two predicates. You get an instance of this class when you call 
 * <code>{@link biz.beaglesoft.bgldoc.query.Predicate#and(biz.beaglesoft.bgldoc.query.Predicate)}</code> 
 */
class AndPredicate extends Predicate implements Serializable{

	private Predicate left, right; 

	AndPredicate(Predicate aLeft, Predicate aRight){
		left = aLeft;
		right = aRight;
	}
	

	/**
	 * @see biz.beaglesoft.bgldoc.query.Predicate#toOQL()
	 */
	public String toOQL(String var) {
		return "("+left.toOQL(var)+") AND ("+right.toOQL(var)+")";
	}

	/**
	 * @see biz.beaglesoft.bgldoc.query.Predicate#toVarDecl(java.lang.String)
	 */
	public String toVarDecl(String var, HashSet<String> vars){
		String _left = left.toVarDecl(var,vars);
		String _right = right.toVarDecl(var,vars);
		if(_left.trim().length() == 0){
			return _right;
		}
		if(_right.trim().length() == 0){
			return _left;
		}
		return _left + ", " + _right;
	}

}
