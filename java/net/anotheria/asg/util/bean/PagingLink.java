package net.anotheria.asg.util.bean;

/**
 * A link in the presentation of paging.
 * @author another
 *
 */
public class PagingLink {
	/**
	 * Link to a page.
	 */
	private String link;
	/**
	 * Caption of a page.
	 */
	private String caption;
	
	public PagingLink(String aLink, String aCaption){
		link = aLink;
		caption = aCaption;
	}
	
	public PagingLink(String aCaption){
		this(null, aCaption);
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	public boolean isLinked(){
		return link != null && link.length()>0;
	}
	

	
}
