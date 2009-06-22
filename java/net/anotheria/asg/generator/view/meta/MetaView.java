package net.anotheria.asg.generator.view.meta;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.IGenerateable;

/**
 * Meta description of the view.
 * @author another
 */
public class MetaView implements IGenerateable{
	/**
	 * Name of the view.
	 */
	private String name;
	//not used?
	//private boolean passwordProtected;
	/**
	 * Sections of the view.
	 */
	private List<MetaSection> sections;
	private String title;
	private List<String> requiredRoles;
	/**
	 * Creates a new MetaView.
	 * @param aName
	 */
	public MetaView(String aName){
		name = aName;
		sections = new ArrayList<MetaSection>();
		requiredRoles = new ArrayList<String>();
	}
	
// --- SOFAR NOT USED, HENCE OUTCOMMENTED 	
	/**
	 * @return
	 */
//	public boolean isPasswordProtected() {
	//	return passwordProtected;
	//}

	/**
	 * @param b
	 */
//	public void setPasswordProtected(boolean b) {
	//	passwordProtected = b;
	//}
//--- // END OUTCOMMENTED	
	
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

	@Override public String toString(){
		return "view "+name+", Roles: "+requiredRoles+", Sections: "+sections+" T: "+title;
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


	public List<String> getRequiredRoles() {
		return requiredRoles;
	}


	public void setRequiredRoles(List<String> requiredRoles) {
		this.requiredRoles = requiredRoles;
	}

}
