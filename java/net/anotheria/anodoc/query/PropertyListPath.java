package net.anotheria.anodoc.query;

import java.io.Serializable;

/**
 * This class defines the path to a property list.<br>
 * Use when you want to query over a property list in a document.
 */
public class PropertyListPath extends Path implements Serializable{

	private Path parent;
	/**
	 * @see biz.beaglesoft.bgldoc.query.Path#addPathElement(biz.beaglesoft.bgldoc.query.Path)
	 */
	public Path addPathElement(Path p) {
		if(!(p instanceof PropertyPath)){
			throw new IllegalArgumentException("you can not add "+p.getClass()+" to PropertyListPath! Only PropertyPath' are supported");
		}
		p.setParent(this);
		return p;
	}

	/**
	 * @see biz.beaglesoft.bgldoc.query.Path#toPathString()
	 */
	public String toPathString() {
		if(parent == null){
			return "propLists.";
		}
		return parent.toPathString()+"propLists.";
	}

	/**
	 * @see biz.beaglesoft.bgldoc.query.Path#setParent(biz.beaglesoft.bgldoc.query.Path)
	 */
	void setParent(Path p) {
		parent = p;
	}

}
