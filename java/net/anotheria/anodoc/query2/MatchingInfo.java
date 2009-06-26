package net.anotheria.anodoc.query2;

/**
 * Used to present matches in the search page.
 * @author another
 *
 */
public interface MatchingInfo {
	/**
	 * Returns the html presentation of the match.
	 * @return
	 */
	String toHtml();
}
