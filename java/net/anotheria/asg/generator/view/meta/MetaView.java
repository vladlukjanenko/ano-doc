package net.anotheria.asg.generator.view.meta;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.IGenerateable;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaView implements IGenerateable{
	private String name;
	private boolean passwordProtected;
	private List<MetaSection> sections;
	private String title;
	
	public MetaView(String aName){
		name = aName;
		sections = new ArrayList<MetaSection>();
	}
	/**
	 * @return
	 */
	public boolean isPasswordProtected() {
		return passwordProtected;
	}

	/**
	 * @param b
	 */
	public void setPasswordProtected(boolean b) {
		passwordProtected = b;
	}
	
	public void addSection(MetaSection section){
		sections.add(section);	
	}

	/**
	 * @return
	 */
	public List<MetaSection> getSections() {
		return sections;
	}

	/**
	 * @param list
	 */
	public void setSections(List<MetaSection> list) {
		sections = list;
	}

	public String toString(){
		return "view "+name+" "+sections+" T: "+title;
	}
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param string
	 */
	public void setTitle(String string) {
		title = string;
	}

}
