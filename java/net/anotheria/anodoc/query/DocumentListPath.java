package net.anotheria.anodoc.query;

/**
 * This class defines the path to a document list.<br>
 * Use when you want to query over a documentlist in your module or in any document.
 */
public class DocumentListPath extends Path {

	private Path parent;

	/**
	 * @see biz.beaglesoft.bgldoc.query.Path#addPathElement(biz.beaglesoft.bgldoc.query.Path)
	 */
	public Path addPathElement(Path p) throws IllegalArgumentException{
		if(!(p instanceof DocumentPath)){
			throw new IllegalArgumentException("you can not add "+p.getClass()+" to DocumentListPath! Only DocumentPath' are supported");
		}
		p.setParent(this);
		return p;
	}

	/**
	 * @see biz.beaglesoft.bgldoc.query.Path#toPathString()
	 */
	public String toPathString() {
		if(parent == null){
			return "lists.";
		}
		return parent.toPathString()+"lists.";
	}
	
	/**
	 * @see biz.beaglesoft.bgldoc.query.Path#setParent(biz.beaglesoft.bgldoc.query.Path)
	 */
	void setParent(Path p) {
		parent = p;
	}

}
