package net.anotheria.anodoc.util;

import java.net.InetAddress;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import net.anotheria.anodoc.data.ListProperty;
import net.anotheria.anodoc.data.Property;

/**
 * Database class for {@link biz.beaglesoft.bgldoc.data.BGLListProperty}.<br>
 * Only {@link biz.beaglesoft.bgldoc.util.CommonJDOModuleStorage} uses this class
 * to be insusceptable against class changes.
 */
class PropertyListStorage {

	private static Logger log;
	private static String _myAddress_;
	static {
		log = Logger.getLogger(PropertyListStorage.class);
		try {
			_myAddress_ = InetAddress.getLocalHost().toString();
		} catch (Exception e) {
			_myAddress_ = "unknown: " + e.getMessage();
		}
	}

	/**
	 * the name/id of the list
	 */
	private String name;
	
	/**
	 * the list of {@link biz.beaglesoft.bgldoc.data.BGLListProperty}s this list contains
	 */
	private PropertyStorage[] props;
	
	private PropertyListStorage(){}
	
	PropertyListStorage(ListProperty bglListProperty) {
		initializeFromListProperty(bglListProperty);
	}

	/**
	 * This method initializes the instance of this class out of a {@link biz.beaglesoft.bgldoc.data.BGLListProperty}
	 */
	private void initializeFromListProperty(ListProperty bglListProperty) {
		name = bglListProperty.getId();
		List originals = bglListProperty.getList();
		if (log.isDebugEnabled()) {
			NDC.push(_myAddress_);
			log.debug("handling "+originals.size()+" elements");
			NDC.pop();
		}
		props = new PropertyStorage[originals.size()];
		for(int i = 0; i < originals.size(); i++){
			props[i] = new PropertyStorage(bglListProperty.get(i));
		} 
	}

	/**
	 * returns the {@link biz.beaglesoft.bgldoc.data.BGLListProperty} the current instance represents.<br> 
	 * The instance is created by using the passed factory.<br>
	 * Therefore it is possible to recreate even subclasses out of this common storage class by just 
	 * passing the right factory.
	 */
	Property getProperty() {
		ListProperty list = new ListProperty(getName());
		for(int i = 0; i < props.length; i++){
			list.add(props[i].getProperty());
		}
		return list;
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

}
