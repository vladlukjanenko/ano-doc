package net.anotheria.anodoc.query.test;

import net.anotheria.anodoc.query.DocumentListPath;
import net.anotheria.anodoc.query.DocumentPath;
import net.anotheria.anodoc.query.Path;
import net.anotheria.anodoc.query.PropertyPath;

/**
 * @author skyball
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class PathTest {


	public static void main(String[] args) {
		System.out.println(Path.getRootPath().addPathElement(new DocumentListPath()).addPathElement(new DocumentPath()).addPathElement(new PropertyPath()));
	}
}
