package net.anotheria.asg.generator.view.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaViewElement {
	private boolean readonly;
	private String name;
	private boolean comparable;
	
	private MetaDecorator decorator;
	
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
	 * @return
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

	public boolean equals(Object o){
		return (o instanceof MetaViewElement) && ((MetaViewElement)o).getName().equals(getName());
	}
}
