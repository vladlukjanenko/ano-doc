package net.anotheria.anodoc.query;

import java.io.Serializable;
import java.util.HashSet;

/**
 * This class defines the basic functionality of a predicate you can use to define queries.<br>
 */
public abstract class Predicate implements Serializable{

	/**
	 * this method returns the conjunction of the current instance and the passed predicate
	 */
	public final Predicate and(Predicate p) {
		return new AndPredicate(this,p);
	}

	/**
	 * this method returns the disjunction of the current instance and the passed predicate
	 */
	public final Predicate or(Predicate p) {
		return new OrPredicate(this,p);
	}

	/**
	 * this method returns the negation of the current instance
	 */
	public final Predicate not() {
		return new NotPredicate(this);
	}

	/**
	 * converts the predicate to an OQL query string(only the part after WHERE) and returns it
	 */
	public abstract String toOQL(String var);
	
	/**
	 * checks the predicate for needed variables and returns the string that defines them for an OQL query
	 */
	public abstract String toVarDecl(String var, HashSet knownVars);	
}
