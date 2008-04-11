package net.anotheria.asg.generator.meta;

import net.anotheria.util.StringUtils;


/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaProperty implements Cloneable{
	private String type;
	private String name;
	private IMetaType metaType;
	
	private boolean multilingual;
	private boolean readonly;
	
	public MetaProperty(String aName, String aType){
		this.name = aName;
		this.type = aType;
		metaType = TypeFactory.createType(aType);
		multilingual = false;
	}
	
	public MetaProperty(String aName, IMetaType aType){
		this.name = aName;
		metaType = aType;
		multilingual = false;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	public String getName(String language) {
		return language == null ? getName() : name+StringUtils.capitalize(language);
	}

	public String getName(String addOn, String language) {
		return language == null ? getName()+addOn : name+addOn+StringUtils.capitalize(language);
	}

	/**
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string
	 */
	public void setType(String string) {
		type = string;
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

	public String toString(){
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

	public IMetaType getMetaType(){
		return metaType;
	}
	
	
	
	public boolean isMultilingual() {
		return multilingual;
	}
	public void setMultilingual(boolean multilingual) {
		this.multilingual = multilingual;
	}
	public boolean isReadonly() {
		return readonly;
	}
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
	
	public Object clone(){
		try{
			return super.clone();
		}catch(CloneNotSupportedException e){
			//ignore
		}
		throw new Error("Can't happen");
	}

}
