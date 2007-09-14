package net.anotheria.asg.generator.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class TypeFactory {
	public static final IMetaType createType(String type){
		if (type.equals("string"))
			return new StringType();
		if (type.equals("int"))
			return new IntType();
		if (type.equals("long"))
			return new LongType();
		if (type.equals("list"))
			return new ListType();
		if (type.equals("text"))
			return new StringType();
		if (type.equals("boolean"))
			return new BooleanType();
		if (type.equals("double"))
			return new DoubleType();
		if (type.equals("float"))
			return new FloatType();
		if (type.equals("image"))
			return new ImageType();
		throw new RuntimeException("Unsupported type: "+type);
	}
}
