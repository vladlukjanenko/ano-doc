package net.anotheria.anodoc.query2.string;

import net.anotheria.anodoc.query2.MatchingInfo;
import net.anotheria.util.CharacterEntityCoder;

public class StringMatchingInfo implements MatchingInfo{
	/**
	 * Part of the string prior to the matched part.
	 */
	private String pre;
	/**
	 * Part of the string past matched part.
	 */
	private String post;
	/**
	 * Matched part.
	 */
	private String match;
	/**
	 * Creates a new StringMatchingInfo.
	 */
	public StringMatchingInfo(){
		
	}
	
	/**
	 * Creates a new StringMatchingInfo.
	 */
	public StringMatchingInfo(String aPre, String aMatch, String aPost){
		pre = aPre;
		match = aMatch;
		post = aPost;
	}
	
	public String getMatch() {
		return match;
	}



	public void setMatch(String match) {
		this.match = match;
	}



	public String getPost() {
		return post;
	}



	public void setPost(String post) {
		this.post = post;
	}



	public String getPre() {
		return pre;
	}



	public void setPre(String pre) {
		this.pre = pre;
	}



	@Override public String toHtml(){
		String ret = "";
		ret += "<i>"+CharacterEntityCoder.htmlEncodeString(pre)+"</i>";
		ret += "<b>"+CharacterEntityCoder.htmlEncodeString(match)+"</b>";
		ret += "<i>"+CharacterEntityCoder.htmlEncodeString(post)+"</i>";
		return ret;
	}
	
	@Override public String toString(){
		return toHtml();
	}
}
