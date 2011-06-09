package net.anotheria.asg.generator.view.meta;

import java.util.List;

import net.anotheria.util.StringUtils;


/**
 * Represents an element of the view.
 * @author another
 */
public class MetaViewElement {
	/**
	 * True if the element is readonly.
	 */
	private boolean readonly;
	/**
	 * The name of the element.
	 */
	private String name;
	/**
	 * The caption of the element. 
	 * Will be displayed in CMS instead of name.
	 */
	private String caption;
	/**
	 * The description of the element. 
	 */
	private String description;
	/**
	 * If true the element is comparable.
	 */
	private boolean comparable;
	/**
	 * If true the element is rich element.
	 */
	private boolean rich;
	
	/**
	 * The decorator for the element.
	 */
	private MetaDecorator decorator;
	/**
	 * Validators that will validate sumbitted value.
	 */
	private List<MetaValidator> validators;
	
	/**
	 * Creates a new meta view element.
	 * @param aName
	 */
	public MetaViewElement(String aName){
		this.name = aName;
	}
	
	/**
	 * @return
	 */
	public boolean isReadonly() {
		return readonly;
	}

	/**
	 * @param b
	 */
	public void setReadonly(boolean b) {
		readonly = b;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	
	/**
	 * Returns true if the element is comparable.
	 */
	public boolean isComparable() {
		return comparable;
	}

	/**
	 * @param b
	 */
	public void setComparable(boolean b) {
		comparable = b;
	}


	/**
	 * @return
	 */
	public MetaDecorator getDecorator() {
		return decorator;
	}

	/**
	 * @param decorator
	 */
	public void setDecorator(MetaDecorator decorator) {
		this.decorator = decorator;
	}

	@Override public boolean equals(Object o){
		return (o instanceof MetaViewElement) && ((MetaViewElement)o).getName().equals(getName());
	}
	
	@Override public int hashCode() {
		assert false : "hashCode not designed";
		return 42; // any arbitrary constant will do 
	}

	public boolean isRich() {
		return rich;
	}

	public void setRich(boolean rich) {
		this.rich = rich;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getCaption() {
		return caption;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setValidators(List<MetaValidator> validator) {
		this.validators = validator;
	}

	public List<MetaValidator> getValidators() {
		return validators;
	}
	
	public boolean isValidated() {
		return validators != null && !validators.isEmpty();
	}
	
	public boolean isJSValidated() {
		if (isValidated()) {
			for (MetaValidator validator : validators){
				if (!StringUtils.isEmpty(validator.getJsValidation())){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "MetaViewElement{" +
				"readonly=" + readonly +
				", name='" + name + '\'' +
				", caption='" + caption + '\'' +
				", description='" + description + '\'' +
				", comparable=" + comparable +
				", rich=" + rich +
				", decorator=" + decorator +
				", validators=" + validators +
				'}';
	}
}
