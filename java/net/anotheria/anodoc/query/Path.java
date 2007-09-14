package net.anotheria.anodoc.query;

/**
 * Basic class for defining pathes in queries. Since OQL supports queries like:<br>
 * obj.list1.list2.field=value<br>
 * we need a possibility to make that feature available to the user without<br>
 * demanding from him to know OQL.
 */
public abstract class Path {

	/**
	 * helper field for variable declaration in OQL
	 */
	private static int lastId = 0;
	
	/**
	 * this path' id
	 */
	protected int id;

	/**
	 * returns an empty path object
	 */
	public static final Path getRootPath(){
		return new EmptyPath();
	}

	protected Path(){
		id = lastId++;
	}

	/**
	 * this method is supposed to work like:<br>
	 * <code>Folder.addSubfolder(subfolder);<br>
	 * return subfolder;<code>
	 */
	public abstract Path addPathElement(Path p);
	
	/**
	 * returns the string we can use in a OQL query for defining the path to a field
	 */
	public abstract String toPathString();
	
	/**
	 * sets the parent to a path
	 */
	abstract void setParent(Path p);
	
	public String toString(){
		return toPathString();
	}
	
	final static class EmptyPath extends Path{
		
		public String toPathString(){
			return "";
		}

		/**
		 * @see biz.beaglesoft.bgldoc.query.Path#addPathElement(biz.beaglesoft.bgldoc.query.Path)
		 */
		public Path addPathElement(Path p) {
			p.setParent(this);
			return p;
		}

		/**
		 * @see biz.beaglesoft.bgldoc.query.Path#setParent(biz.beaglesoft.bgldoc.query.Path)
		 */
		void setParent(Path p) {
		}

	}
	
	
}
