package net.anotheria.asg.generator.meta;

import java.util.List;

/**
 * Representation of an internal link to another document.
 * @author another
 */
public class MetaLink extends MetaProperty{
	
	/**
	 * Type of the link. Currently the only supported link type is single.
	 */
	private String linkType;
	/**
	 * Link target as relative or absolute document name.
	 */
	private String linkTarget;
	
	/**
	 * Properties of target document are used to decorate link in additional to ID
	 */
	private List<String> linkDecoration;
	
	public MetaLink(String name){
		super(name, MetaProperty.Type.STRING);
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
	@Override public String toNameConstant() {
		return "LINK_"+super.toNameConstant();
	}
	
	@Override public boolean isLinked(){
		return true;
	}
	
	public String getTargetModuleName(){
		int index = getLinkTarget().indexOf('.');
		return index == -1 ? null : getLinkTarget().substring(0, index);
	}

	public String getTargetDocumentName(){
		return getLinkTarget().substring(getLinkTarget().indexOf('.')+1);
	}
	
	/**
	 * 
	 * @param document
	 * @return
	 */
	public boolean doesTargetMatch(MetaDocument document){
		return doesTargetMath(document.getParentModule(), document);
	}
	
	public boolean doesTargetMath(MetaModule module, MetaDocument document){
		return linkTarget != null && linkTarget.equals(module.getName()+"."+document.getName());
	}
	
	public boolean isRelative(){
		return getLinkTarget().indexOf('.') == -1;
	}
	public List<String> getLinkDecoration() {
		return linkDecoration;
	}
	public void setLinkDecoration(List<String> linkDecoration) {
		this.linkDecoration = linkDecoration;
	}

}
