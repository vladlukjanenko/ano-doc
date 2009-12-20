package net.anotheria.asg.generator.view.meta;
/**
 * Allows the developer to integrate a custom section into generated frontend.
 * @author another
 *
 */
public class MetaCustomSection extends MetaSection{
	/**
	 * Path to be called whenever the section is selected.
	 */
	private String path;
	
	public MetaCustomSection(String title){
		super(title);
	}
	
	@Override public String toString(){
		return getTitle()+": "+path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
