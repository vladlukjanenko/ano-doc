package net.anotheria.asg.generator.view.meta;

import net.anotheria.asg.generator.IGenerateable;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaSection implements IGenerateable{
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
	
	public String toString(){
		return "section "+title;
	}
	
	public boolean equals(Object o){
		return (o instanceof MetaSection) && ((MetaSection)o).title.equals(title);
	}

}
