package net.anotheria.anodoc.query2;

/**
 * Presents the query of abstract DataObjects that have Property with given name 
 * and its value less then or equal (if including) to the querying value.
 * 
 * @author denis
 *
 */
public class QueryLessThenProperty<P extends Comparable<P>> extends QueryProperty{
	
	private static final long serialVersionUID = -4870076734697577172L;

	private boolean including;
	
	/**
	 * Creates new less then query. 
	 * @param name of the Property for which query is created
	 * @param value maximal allowed value of queried Property
	 */
	public QueryLessThenProperty(String name, P value) {
		this(name, value, false);
	}
	
	/**
	 * Creates new less then or equal (if including) query. 
	 * @param name of the Property for which query is created
	 * @param value maximal allowed value of queried Property
	 * @param including the equal values 
	 */
	public QueryLessThenProperty(String name, P value, boolean including) {
		super(name, value);
		this.including = including;
	}

	@Override public String toString(){
		return getName() + getComparator() +getValue();
	}
	
	@Override public String getComparator(){
		return including? " <= " : " < ";
	}
	
	@SuppressWarnings("unchecked")
	@Override public boolean doesMatch(Object o){
		if(getValue() == null || o == null)
			return false;
		P value = (P)getValue();
		if(!(o instanceof Comparable))
			throw new RuntimeException("Matched object must implement interface Comprable!");
		P anotherValue = (P) o;

		int diff = value.compareTo(anotherValue);
		//Shifting to the left on one if not including.
		if(!including)
			//Now diff in case of equal values is -1 and excluded from matching
			diff--;
		return diff >= 0;
	}
	
}
