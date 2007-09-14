package net.anotheria.anodoc.util;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import net.anotheria.anodoc.data.BooleanProperty;
import net.anotheria.anodoc.data.FloatProperty;
import net.anotheria.anodoc.data.IntProperty;
import net.anotheria.anodoc.data.LongProperty;
import net.anotheria.anodoc.data.Property;
import net.anotheria.anodoc.data.StringProperty;
import net.anotheria.anodoc.data.TextProperty;

/**
 * Database class for {@link biz.beaglesoft.bgldoc.data.BGLProperty}.<br>
 * Only {@link biz.beaglesoft.bgldoc.util.CommonJDOModuleStorage} uses this class
 * to be insusceptable against class changes.<br>
 * This class represents all different subclasses of {@link biz.beaglesoft.bgldoc.data.BGLProperty} 
 * except for {@link biz.beaglesoft.bgldoc.data.TextProperty}.<br>
 * This is achieved by remembering the type.
 */
class PropertyStorage implements PropertyConstants{
	
	private static Logger log;
	private static String _myAddress_;
	static {
		log = Logger.getLogger(PropertyStorage.class);
		try {
			_myAddress_ = InetAddress.getLocalHost().toString();
		} catch (Exception e) {
			_myAddress_ = "unknown: " + e.getMessage();
		}
	}

	/**
	 * the name/id of this property
	 */
	private String name;
	
	/**
	 * the value of this property
	 */
	private String value;
	
	/**
	 * the type of this property
	 */
	private int type;

	protected PropertyStorage(){}
	
	PropertyStorage(Property bglProperty) {
		initializeFromProperty(bglProperty);
	}

	/**
	 * This method initializes the instance of this class out of a {@link biz.beaglesoft.bgldoc.data.BGLProperty}
	 */
	private void initializeFromProperty(Property bglProperty) {
		name = bglProperty.getId();
		value = bglProperty.getValue()+"";
		type = getType(bglProperty);
	}

	/**
	 * This method maps the type of the {@link biz.beaglesoft.bgldoc.data.BGLProperty} to an int value.
	 */
	private int getType(Property bglProperty) {
		if (log.isDebugEnabled()) {
			NDC.push(_myAddress_);
			log.debug("handling object of "+bglProperty.getClass());
			NDC.pop();
		}
		if(bglProperty instanceof BooleanProperty){
			return PROPERTY_BOOLEAN;
		}else if(bglProperty instanceof FloatProperty){
			return PROPERTY_FLOAT;
		}else if(bglProperty instanceof IntProperty){
			return PROPERTY_INT;
		}else if(bglProperty instanceof LongProperty){
			return PROPERTY_LONG;
		}else if(bglProperty instanceof StringProperty){
			return PROPERTY_STRING;
		}else if(bglProperty instanceof TextProperty){
			return PROPERTY_TEXT;
		}
		throw new RuntimeException("unsupported type "+bglProperty.getClass().getName());
	}

	/**
	 * returns the appropriate subclass instance of {@link biz.beaglesoft.bgldoc.data.BGLProperty}
	 */
	public Property getProperty() {
		switch(getType()){
			case PROPERTY_BOOLEAN: 	return getBooleanProperty();
			case PROPERTY_FLOAT: 		return getFloatProperty();
			case PROPERTY_INT: 		return getIntProperty();
			case PROPERTY_LONG: 		return getLongProperty();
			case PROPERTY_TEXT: 		return getTextProperty();
			case PROPERTY_STRING: 		
			default:					return getStringProperty();
		}
	}

	private Property getTextProperty() {
		return new TextProperty(getName(),getValue());
	}

	private Property getStringProperty() {
		StringProperty string = new StringProperty(getName());
		string.setValue(getValue());
		return string;
	}

	private Property getLongProperty() {
		LongProperty myLong = new LongProperty(getName(), new Long(getValue()));
		return myLong;
	}

	private Property getIntProperty() {
		IntProperty myInt = new IntProperty(getName(),new Integer(getValue()));
		return myInt;
	}

	private Property getFloatProperty() {
		return new FloatProperty(getName(),new Float(getValue()));
	}

	private Property getBooleanProperty() {
		return new BooleanProperty(getName(),new Boolean(getValue()));
	}

	public String getName() {
		return name;
	}

	public int getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

}
