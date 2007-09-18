package net.anotheria.asg.generator.parser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.view.meta.MetaCustomFunctionElement;
import net.anotheria.asg.generator.view.meta.MetaCustomSection;
import net.anotheria.asg.generator.view.meta.MetaDecorator;
import net.anotheria.asg.generator.view.meta.MetaDialog;
import net.anotheria.asg.generator.view.meta.MetaEmptyElement;
import net.anotheria.asg.generator.view.meta.MetaFieldElement;
import net.anotheria.asg.generator.view.meta.MetaFunctionElement;
import net.anotheria.asg.generator.view.meta.MetaListElement;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaViewElement;
import net.anotheria.asg.generator.view.meta.MetaView;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class XMLViewParser {
	
	String content;
	
	public XMLViewParser(String content){
		this.content = content;
	}
	
	@SuppressWarnings("unchecked")
	public List<MetaView> parseViews(){
		
		SAXBuilder reader = new SAXBuilder();
		reader.setValidation(false);
		List<MetaView> ret = new ArrayList<MetaView>();

		try{
			Document doc = reader.build(new StringReader(content));
			Element root = doc.getRootElement();
			List<Element> views = root.getChildren("view");
			for (Element elem :views)
				ret.add(parseView(elem));
			
		}catch(JDOMException e){
			e.printStackTrace();
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private MetaView parseView(Element m){
		
		String name = m.getAttributeValue("name");
		MetaView view = new MetaView(name);

		//System.out.println("parsing view: "+name);
		
		List<Element> sections = m.getChild("sections").getChildren();
		for (int i=0; i<sections.size(); i++)
			view.addSection(parseSection(sections.get(i)));
		
		view.setTitle(m.getAttributeValue("title"));
		
		return view;
	}
	
	private MetaSection parseSection(Element section){
		String type = section.getAttributeValue("type");
		if (type.equals("module"))
			return parseModuleSection(section);
		if (type.equals("custom"))
			return parseCustomSection(section);
		throw new RuntimeException("Unknown section type: "+type);
	}
	
	private MetaCustomSection parseCustomSection(Element section){
		String title = section.getAttributeValue("title");
		String path = section.getChildText("path");
		MetaCustomSection ret = new MetaCustomSection(title);
		ret.setPath(path);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private MetaModuleSection parseModuleSection(Element section){
		String title = section.getAttributeValue("title");
		//System.out.println("Parse section:; "+title);
		MetaModuleSection s = new MetaModuleSection(title);
		String moduleName = section.getChildText("module");
		String documentName = section.getChildText("document");
		MetaModule mod = GeneratorDataRegistry.getInstance().getModule(moduleName);
		if (mod==null)
			throw new RuntimeException("Module "+moduleName+" not found!");
		s.setModule(mod); 
		s.setDocument(mod.getDocumentByName(documentName));
		
		Element elementsRoot = section.getChild("elements");
		List<Element> elements = elementsRoot.getChildren();
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = parseViewElement(s, elements.get(i));
			s.addElement(element);
		}
		
		List<Element> dialogs = section.getChildren("dialog");
		//System.out.println("To parse "+dialogs.size()+" dialogs.");
		for (int i=0; i<dialogs.size(); i++){
			Element d = dialogs.get(i);
			MetaDialog dialog = new MetaDialog(d.getAttributeValue("name"));
			dialog.setTitle(d.getAttributeValue("title"));
			List dialogElements = d.getChild("elements").getChildren();
			for (int e=0; e<dialogElements.size(); e++){
				MetaViewElement element = parseViewElement(null, (Element)dialogElements.get(e));
				dialog.addElement(element);
			}
			s.addDialog(dialog);			
		}
		
		List<Element> filters = section.getChildren("filter");
		for (Element f : filters){
			s.addMetaFilter(GeneratorDataRegistry.getInstance().createFilter(f.getAttributeValue("name"), f.getAttributeValue("field")));
		}
		
		return s;
	}
	
	private MetaViewElement parseViewElement(MetaModuleSection section, Element elem){
		String type = elem.getAttributeValue("type");
		
		MetaViewElement element = null;

		if (type.equals("field")){
			element = parseFieldElement(elem);
		}
		
		if (type.equals("function")){
			element = parseFunctionElement(elem);
		}
		
		if (type.equals("list")){
			element = parseListElement(elem);
		}
		
		if (type.equals("empty")){
			element = parseEmptyElement(elem);
		}
		
		if (type.equals("customfunction")){
			element = parseCustomFunctionElement(elem);
		}

		if (element==null)
			throw new RuntimeException("unknown element type "+type);
		
		String comparable = elem.getAttributeValue("comparable");
		if (comparable!=null && comparable.equals("true") )
			element.setComparable(true);
		if (section!=null && comparable!=null && comparable.equals("default") ){
			element.setComparable(true);
			section.setDefaultSortable(element);
		}
		
		Element decElement = elem.getChild("decorator");
		if (decElement!=null){
			element.setDecorator(parseDecorator(decElement));
			//System.out.println("found decorator!");
		}
		
		return element;
		
	}
	
	private MetaDecorator parseDecorator(Element e){
		String name = e.getAttributeValue("name");
		String rule = e.getChildText("rule");
		MetaDecorator dec = GeneratorDataRegistry.getInstance().createDecorator(name, rule);
		return dec;
	}
	
	private MetaFieldElement parseFieldElement(Element elem){
		String name = elem.getAttributeValue("name");
		String readonly = elem.getAttributeValue("readonly");
		MetaFieldElement field = new MetaFieldElement(name); 
		if (readonly!=null && readonly.equals("true"))
			field.setReadonly(true);
		return field;
	}
	
	private MetaEmptyElement parseEmptyElement(Element elem){
		return new MetaEmptyElement();
	}

	private MetaFunctionElement parseFunctionElement(Element elem){
		String name = elem.getAttributeValue("name");
		MetaFunctionElement  ret = new MetaFunctionElement(name);
		if (elem.getAttribute("caption")!=null)
			ret.setCaption(elem.getAttributeValue("caption")); 
		return ret;
	}
	
	private MetaCustomFunctionElement parseCustomFunctionElement(Element elem){
		String name = elem.getAttributeValue("name");
		MetaCustomFunctionElement  ret = new MetaCustomFunctionElement(name);
		ret.setCaption(elem.getChildText("caption"));
		ret.setLink(elem.getChildText("link"));
		return ret;
	}

	@SuppressWarnings("unchecked")
	private MetaListElement parseListElement(Element e){
		MetaListElement ret = new MetaListElement();
		List<Element> elements = e.getChildren("element");
		for (int i=0; i<elements.size(); i++)
			ret.addElement(parseViewElement(null, elements.get(i)));
		return ret; 	
	}
	

}
