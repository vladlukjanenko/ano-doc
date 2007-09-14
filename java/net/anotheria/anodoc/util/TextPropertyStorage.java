package net.anotheria.anodoc.util;

import net.anotheria.anodoc.data.TextProperty;

/**
 * Database class for {@link biz.beaglesoft.bgldoc.data.TextProperty}.<br>
 * Only {@link biz.beaglesoft.bgldoc.util.CommonJDOModuleStorage} uses this class
 * to be insusceptable against class changes.<br>
 * The value of this class has a full text index.
 */
class TextPropertyStorage extends PropertyStorage{

	private TextPropertyStorage(){}

	public TextPropertyStorage(TextProperty bglTextProperty) {
		super(bglTextProperty);
	}

}
