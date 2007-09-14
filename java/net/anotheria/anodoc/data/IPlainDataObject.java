package net.anotheria.anodoc.data;

import java.io.Serializable;

/**
 * This interface represents a single data container 
 * which doesn't have undelying objects.
 * @see biz.beaglesoft.bgldoc.data.ICompositeDataObject
 * @see biz.beaglesoft.bgldoc.data.IBasicStoreableObject
 * @since 1.0
 */
public interface IPlainDataObject extends Serializable, IBasicStoreableObject{
}
