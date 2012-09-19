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
     * True if autocomplete for this element in browser is off.
     */
    private boolean autocompleteOff;
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
	 * If true the element is datetime in long.
	 */
	private boolean datetime;	
	/**
	 * The decorator for the element.
	 */
	private MetaDecorator decorator;
	/**
	 * Validators that will validate sumbitted value.
	 */
	private List<MetaValidator> validators;
	
	/**
	 * The sorting type of the element.
	 */
	private SortingType sortingType = SortingType.ALPHABETHICAL;


    /**
	 * Creates a new meta view element.
	 * @param aName
	 */
	public MetaViewElement(String aName){
		this.name = aName;
	}
	
	/**
	 * @return True if the element is readonly
	 */
	public boolean isReadonly() {
		return readonly;
	}

	/**
	 * Sets if the element is readonly or not.
	 * @param b flag to set
	 */
	public void setReadonly(boolean b) {
		readonly = b;
	}

    /**
     * @return True if autocomplete for this element is off.
     */
    public boolean isAutocompleteOff() {
        return autocompleteOff;
    }

    /**
     * Sets if the element allows autocompletion or not.
     * @param b flag to set
     */
    public void setAutocompleteOff(boolean b) {
        this.autocompleteOff = b;
    }

    /**
	 * @return name of the element
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets name of the element.
	 * @param string name ot set
	 */
	public void setName(String string) {
		name = string;
	}

	
	/**
	 * @return true if the element is comparable, otherwise - false
	 */
	public boolean isComparable() {
		return comparable;
	}

	/**
	 * Sets if document ios comparable.
	 * @param b flag to set
	 */
	public void setComparable(boolean b) {
		comparable = b;
	}


	/**
	 * @return decorator for the element
	 */
	public MetaDecorator getDecorator() {
		return decorator;
	}

	/**
	 * Sets decorator for the element.
	 * @param decorator decorator to set
	 */
	public void setDecorator(MetaDecorator decorator) {
		this.decorator = decorator;
		//this is a hack to prevent need for altering all files, fow now.
		if (decorator!=null && name!=null && name.equals("id"))
			sortingType = SortingType.NUMERICAL;

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
	
	public boolean isDatetime() {
		return datetime;
	}

	public void setDatetime(boolean datetime) {
		this.datetime = datetime;
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
				", autocompleteOff=" + autocompleteOff +
                ", name='" + name + '\'' +
				", caption='" + caption + '\'' +
				", description='" + description + '\'' +
				", comparable=" + comparable +
				", rich=" + rich +
				", datetime=" + datetime +
				", decorator=" + decorator +
				", validators=" + validators +
				'}';
	}

	public SortingType getSortingType() {
		return sortingType;
	}

	public void setSortingType(SortingType sortingType) {
		this.sortingType = sortingType;
	}

	
}
