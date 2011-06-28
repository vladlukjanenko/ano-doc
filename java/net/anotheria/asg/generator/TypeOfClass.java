package net.anotheria.asg.generator;
/**
 * Type of generateable class.
 * @author lrosenberg
 */
public enum TypeOfClass {
	/**
	 * A generated java class.
	 */
	CLASS,
    /**
	 * A generated java enum.
	 */
	ENUM,
	/**
	 * A generated java interface.
	 */
	INTERFACE;
	
	public static TypeOfClass getDefault(){
		return CLASS;
	}

	/**
	 * @return java declaration
	 */
	public String toJava() {
		return toString().toLowerCase();
	}
}
