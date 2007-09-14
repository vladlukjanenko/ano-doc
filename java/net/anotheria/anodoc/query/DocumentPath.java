package net.anotheria.anodoc.query;

/**
 * This class defines the path to a document.<br>
 * Use when you want to query over a document in your module or in any document list.
 */
public class DocumentPath extends Path {

	private Path parent;

	/**
	 * @see biz.beaglesoft.bgldoc.query.Path#addPathElement(biz.beaglesoft.bgldoc.query.Path)
	 */
	public Path addPathElement(Path p) {
		p.setParent(this);
		return p;
	}

	/**
	 * @see biz.beaglesoft.bgldoc.query.Path#toPathString()
	 */
	public String toPathString() {
		if(parent == null){
			return "docs.";
		}
		return parent.toPathString()+"docs.";
	}

	/**
	 * @see biz.beaglesoft.bgldoc.query.Path#setParent(biz.beaglesoft.bgldoc.query.Path)
	 */
	void setParent(Path p) {
		parent = p;
	}	

}
