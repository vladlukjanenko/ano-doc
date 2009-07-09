package net.anotheria.anodoc.query2;

import java.util.List;

import net.anotheria.asg.data.DataObject;

public interface DocumentQuery {
	List<QueryResultEntry> match(DataObject doc);
}
