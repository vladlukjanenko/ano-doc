package net.anotheria.asg.generator.meta;

/**
 * Factory for the meta types.
 * 
 * @author another
 */
public class TypeFactory {

	private TypeFactory() {
	}

	/**
	 * Creates a type out of a string description.
	 * 
	 * @param type
	 * @return
	 */
	public static final IMetaType createType(MetaProperty.Type type) {

		switch (type) {
		case STRING:
			return new StringType();
        case PASSWORD:
            return new PasswordType();
		case INT:
			return new IntType();
		case LONG:
			return new LongType();
		case LIST:
			return new ListType();
		case TEXT:
			return new StringType();
		case BOOLEAN:
			return new BooleanType();
		case DOUBLE:
			return new DoubleType();
		case FLOAT:
			return new FloatType();
		case IMAGE:
			return new ImageType();
		default:
			throw new RuntimeException("Unsupported type: " + type);
		}

	}
}
