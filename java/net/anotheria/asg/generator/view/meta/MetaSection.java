package net.anotheria.asg.generator.view.meta;

import net.anotheria.asg.generator.IGenerateable;

/**
 * A section in the overview.
 * @author another
 */
public class MetaSection implements IGenerateable{
	/**
	 * The title of the section.
	 */
	private String title;
	
	public MetaSection(String aTitle){
		this.title = aTitle;
	}
	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}
	
	@Override public String toString(){
		return "section "+title;
	}
	
	@Override public boolean equals(Object o){
		return (o instanceof MetaSection) && ((MetaSection)o).title.equals(title);
	}
	
	public int hashCode() {
		  assert false : "hashCode not designed";
		  return 42; // any arbitrary constant will do 
	}
}
