package net.anotheria.anodoc.query2.string;

import net.anotheria.anodoc.query2.MatchingInfo;
import net.anotheria.util.CharacterEntityCoder;

public class StringMatchingInfo implements MatchingInfo{
	private String pre;
	private String post;
	private String match;
	
	public StringMatchingInfo(){
		
	}
	
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



	public String toHtml(){
		String ret = "";
		ret += "<i>"+CharacterEntityCoder.htmlEncodeString(pre)+"</i>";
		ret += "<b>"+CharacterEntityCoder.htmlEncodeString(match)+"</b>";
		ret += "<i>"+CharacterEntityCoder.htmlEncodeString(post)+"</i>";
		return ret;
	}
	
	public String toString(){
		return toHtml();
	}
}
