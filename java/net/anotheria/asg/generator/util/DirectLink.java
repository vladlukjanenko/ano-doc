package net.anotheria.asg.generator.util;

import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaLink;
import net.anotheria.asg.generator.meta.MetaModule;

/**
 * A link to a target document.
 * @author lrosenberg
 *
 */
public class DirectLink {
	/**
	 * Source module.
	 */
	private MetaModule module;
	/**
	 * Source document.
	 */
	private MetaDocument document;
	/**
	 * Link in the source document.
	 */
	private MetaLink property;
	
	public DirectLink(){
		
	}
	
	public DirectLink(MetaModule aModule, MetaDocument aDocument, MetaLink aProperty){
		module = aModule;
		document = aDocument;
		property = aProperty;
	}
	
	public MetaModule getModule() {
		return module;
	}
	public void setModule(MetaModule module) {
		this.module = module;
	}
	public MetaDocument getDocument() {
		return document;
	}
	public void setDocument(MetaDocument document) {
		this.document = document;
	}
	public MetaLink getProperty() {
		return property;
	}
	public void setProperty(MetaLink property) {
		this.property = property;
	}
}
