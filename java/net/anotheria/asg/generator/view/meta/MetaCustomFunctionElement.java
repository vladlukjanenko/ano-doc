package net.anotheria.asg.generator.view.meta;

/**
 * This element allows to specify a custom function element (link or button).
 * @author another
 *
 */
public class MetaCustomFunctionElement extends MetaViewElement{
	/**
	 * Link target of the element.
	 */
	private String link;
	/**
	 * Caption of the link.
	 */
	private String caption;
	
	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	/**
	 * Creates a new MetaCustomFunctionElement.
	 * @param name
	 */
	public MetaCustomFunctionElement(String name){
		super(name);
	}
}

