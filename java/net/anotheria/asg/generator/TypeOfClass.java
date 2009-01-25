package net.anotheria.asg.generator;

public enum TypeOfClass {
	CLASS,
	INTERFACE;
	
	public static TypeOfClass getDefault(){
		return CLASS;
	}
	
	public String toJava(){
		return toString().toLowerCase();
	}
}
