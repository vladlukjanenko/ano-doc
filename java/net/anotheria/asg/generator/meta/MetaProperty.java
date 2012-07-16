package net.anotheria.asg.generator.meta;

import net.anotheria.util.StringUtils;


/**
 * Represents a single property of a document. A property may be basic, like int, boolean, long, or complex, like list or table.
 * This class defines single one typed property mainly.
 * @author another
 */
public class MetaProperty implements Cloneable{
	
	public static enum Type{
		STRING("string"),
        PASSWORD("password"),
		TEXT("text"),
		BOOLEAN("boolean"),
		INT("int"),
		LONG("long"),
		DOUBLE("double"),
		FLOAT("float"),
		LIST("list"),
		IMAGE("image");
		

		String name;
		Type(String aName){
			name = aName;
		}
		
		public String getName(){
			return name;
		}
		
		public static Type findTypeByName(String name){
			for(Type t: values())
				if(t.getName().equals(name))
					return t;
			return null;
		}

		@Override
		public String toString() {
			return "Type{" +
					"name='" + name + '\'' +
					'}';
		}
	}
	
	/**
	 * The type of the property as string.
	 */
	private Type type;
	/**
	 * Name of the property.
	 */
	private String name;
	/**
	 * Resolved property type.
	 */
	private IMetaType metaType;
	/**
	 * True if the property is multilingual.
	 */
	private boolean multilingual;
	/**
	 * True if the property is readonly. For example id is a readonly property. Basically this is only used by the view, 
	 * the application itself still free to change a readonly property.
	 */
	private boolean readonly;
	/**
	 * Creates a new MetaProperty with given name and type description. 
	 * @param aName
	 * @param aType
	 */
	public MetaProperty(String aName, Type aType){
		this(aName, aType, false);
	}
	
	public MetaProperty(String aName, Type aType, boolean aMultilingual){
		name = aName;
		type = aType;
		metaType = TypeFactory.createType(aType);
		multilingual = aMultilingual;
		
		if (name==null)
			throw new IllegalArgumentException("name is null");
	}
	
	public MetaProperty(String aName, IMetaType aType){
		name = aName;
		metaType = aType;
		multilingual = false;

		if (name==null)
			throw new IllegalArgumentException("name is null");
	}


	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the internal name of the property for language variant.
	 * @param language
	 * @return
	 */
	public String getName(String language) {
		return language == null || !isMultilingual()? getName() : name+StringUtils.capitalize(language);
	}

	public String getName(String addOn, String language) {
		return language == null ? getName()+addOn : name+addOn+StringUtils.capitalize(language);
	}

	/**
	 * @return
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	public String toNameConstant(){
		return "PROP_"+getNameConstantBase();
	}
	
	public String toNameConstant(String language){
		return "PROP_"+getNameConstantBase()+"_"+language.toUpperCase();
	}
	
	private String getNameConstantBase(){
		String ret = "";
		for (int i=0; i<name.length(); i++){
			char c = name.charAt(i);
			if (Character.isLowerCase(c))
				ret += Character.toUpperCase(c);
			if (Character.isUpperCase(c)){
				ret += "_"+c;
			}
			if (!Character.isLetter(c))
				ret += c;
		}
		return ret;
	}

	public String getAccesserName(){
		return Character.toUpperCase(name.charAt(0))+name.substring(1);
	}
	
	public String getAccesserName(String language){
		return language == null ? getAccesserName() : Character.toUpperCase(name.charAt(0))+name.substring(1)+Character.toUpperCase(language.charAt(0))+language.substring(1);
	}

	public String toJavaType(){
		return metaType.toJava();
	}
	
	public String toJavaErasedType(){
		return metaType.toJava();
	}

	public String toJavaObjectType(){
		return metaType.toJavaObject();
	}
	
	public String toPropertyGetter(){
		return metaType.toPropertyGetter();
	}
	
	public String toPropertySetter(){
		return metaType.toPropertySetter();
	}
	
	public String toBeanGetter(){
		return metaType.toBeanGetter(name);
	}
	
	public String toBeanGetter(String language){
		return language == null ? toBeanGetter() : metaType.toBeanGetter(name)+StringUtils.capitalize(language);
	}

	public String toBeanSetter(){
		return metaType.toBeanSetter(name);
	}

	public String toBeanSetter(String language){
		return language == null ? toBeanSetter() : metaType.toBeanSetter(name)+StringUtils.capitalize(language);
	}

	@Override public String toString(){
		return type+" "+name;
	}
	
	public boolean isLinked(){
		return false;
	}

	public String toSetter(String language){
		return "set"+getAccesserName(language);
	}
	
	public String toSetter(){
		return "set"+getAccesserName();
	}
	
	public String toGetter(){
		return "get"+getAccesserName();
	}
	
	public String toGetter(String language){
		return "get"+getAccesserName(language);
	}

	/**
	 * Returns the metatype of this property.
	 * @return
	 */
	public IMetaType getMetaType(){
		return metaType;
	}
	
	
	/**
	 * Returns true if the property is multilingual.
	 * @return
	 */
	public boolean isMultilingual() {
		return multilingual;
	}
	/**
	 * Sets the multilingual support of the property.
	 * @param multilingual
	 */
	public void setMultilingual(boolean multilingual) {
		this.multilingual = multilingual;
	}
	/**
	 * Returns true if the property is read only.
	 * @return
	 */
	public boolean isReadonly() {
		return readonly;
	}
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
	
	@Override public Object clone(){
		try{
			return super.clone();
		}catch(CloneNotSupportedException e){
			//ignore
		}
		throw new AssertionError("Can't happen");
	}

}
