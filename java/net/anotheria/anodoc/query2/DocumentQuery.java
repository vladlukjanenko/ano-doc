package net.anotheria.anodoc.query2;

import java.util.List;

import net.anotheria.asg.data.DataObject;

public interface DocumentQuery {
	/**
	 * Returns a list of possible results for an object.
	 * @param doc
	 * @return
	 */
	List<QueryResultEntry> match(DataObject doc);
}
