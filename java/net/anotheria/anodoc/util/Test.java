package net.anotheria.anodoc.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;


public class Test {
	public static void main(String a[]) throws IOException{
		Document doc = new Document();
		Element root = new Element("xyz");
		doc.setRootElement(root);
		List<Element> childs = new ArrayList<Element>();
		for (int i=0; i<10; i++){
			Element e = new Element("element");
			e.setText(""+i);
			e.setAttribute(new Attribute("id",""+i));
			childs.add(e);
		}
		root.setChildren(childs);
		
		XMLOutputter outputer = new XMLOutputter();
		outputer.output(doc, System.out);
	}
}
