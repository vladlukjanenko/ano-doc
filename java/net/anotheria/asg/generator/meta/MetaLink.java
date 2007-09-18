package net.anotheria.asg.generator.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaLink extends MetaProperty{
	
	private String linkType;
	private String linkTarget; 
	
	public MetaLink(String name){
		super(name, "string");
	}
	/**
	 * @return
	 */
	public String getLinkTarget() {
		return linkTarget;
	}

	/**
	 * @return
	 */
	public String getLinkType() {
		return linkType;
	}

	/**
	 * @param string
	 */
	public void setLinkTarget(String string) {
		linkTarget = string;
	}

	/**
	 * @param string
	 */
	public void setLinkType(String string) {
		linkType = string;
	}

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.meta.MetaProperty#toNameConstant()
	 */
	public String toNameConstant() {
		return "LINK_"+super.toNameConstant();
	}
	
	public boolean isLinked(){
		return true;
	}
	
	public String getTargetModuleName(){
		return getLinkTarget().substring(0, getLinkTarget().indexOf('.'));
	}

	public String getTargetDocumentName(){
		return getLinkTarget().substring(getLinkTarget().indexOf('.')+1);
	}

}