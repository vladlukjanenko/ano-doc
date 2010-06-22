/**
 * 
 */
package net.anotheria.anodoc.query2.string;

import net.anotheria.anodoc.data.Document;
import net.anotheria.asg.data.DataObject;
import net.anotheria.asg.data.ObjectInfo;

class DataObjectAdapter extends Document implements DataObject {

	private static final long serialVersionUID = -1050732326635139652L;

//	private List<Property> properties = new ArrayList<Property>();

	public DataObjectAdapter(String id) {
		super(id);
	}

//	public void addProperty(String name, String value) {
//		properties.add(new StringProperty(name, value));
//	}
//
//	@Override
//	public List<Property> getProperties() {
//		return properties;
//	}

	@Override
	public String getDefinedName() {
		return null;
	}

	@Override
	public String getDefinedParentName() {
		return null;
	}

	@Override
	public String getFootprint() {
		return null;
	}

	@Override
	public ObjectInfo getObjectInfo() {
		return null;
	}

}