package net.anotheria.anodoc.query;

import java.io.Serializable;

/**
 * This class defines the path to a property.<br>
 * Use when you want to query over a property in a document or in any property list.
 */
public class PropertyPath extends Path implements Serializable{

	private Path parent;

	/**
	 * @see biz.beaglesoft.bgldoc.query.Path#addPathElement(biz.beaglesoft.bgldoc.query.Path)
	 */
	public Path addPathElement(Path p) throws IllegalArgumentException{
		throw new IllegalArgumentException("Property should be the end of the path");
	}

	/**
	 * @see biz.beaglesoft.bgldoc.query.Path#setParent(biz.beaglesoft.bgldoc.query.Path)
	 */
	void setParent(Path p) {
		parent = p;
	}

	/**
	 * @see biz.beaglesoft.bgldoc.query.Path#toPathString()
	 */
	public String toPathString() {
		if(parent == null){
			return "props.";
		}
		return parent.toPathString()+"props.";
	}

}
