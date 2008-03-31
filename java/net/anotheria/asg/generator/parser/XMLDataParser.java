package net.anotheria.asg.generator.parser;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.meta.FederatedDocumentMapping;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaEnumerationProperty;
import net.anotheria.asg.generator.meta.MetaFederationModule;
import net.anotheria.asg.generator.meta.MetaLink;
import net.anotheria.asg.generator.meta.MetaListProperty;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.MetaTableProperty;
import net.anotheria.asg.generator.meta.ModuleParameter;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.util.StringUtils;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class XMLDataParser implements IDataParser {
	
	private String content;
	
	public XMLDataParser(String content){
		this.content = content;
	}
	
	
	public List<MetaModule> parseModules(){
		
		SAXBuilder reader = new SAXBuilder();
		reader.setValidation(false);
		List<MetaModule> ret = new ArrayList<MetaModule>();

		try{
			Document doc = reader.build(new StringReader(content));
			Element root = doc.getRootElement();
			List<Element> modules = root.getChildren("module");
			for (int i=0; i<modules.size(); i++){
				Element elem = (Element)modules.get(i);
				ret.add(parseModule(elem));
			}
		}catch(JDOMException e){
			e.printStackTrace();
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private MetaModule parseModule(Element m){
		//System.out.println("Parsing "+m.getName());
		String name = m.getAttributeValue("name");
		MetaModule mod = new MetaModule();
		mod.setName(name);
		try{
			String storageType = m.getAttributeValue("storageType");
			if (storageType!=null){
				if (storageType.equalsIgnoreCase("db"))
					mod.setStorageType(StorageType.DB);
				if (storageType.equalsIgnoreCase("federation")){
					//ugly, but ok.
					mod = new MetaFederationModule(name);
				}
					
			}
			String storageKey = m.getAttributeValue("storageKey");
			if (storageKey!=null){
				mod.setStorageKey(storageKey);
			}
		}catch(Exception ignored){
			ignored.printStackTrace();
		}
		List childs = m.getChildren("document");
		for (int i=0; i<childs.size(); i++)
			mod.addDocument(parseDocument((Element)childs.get(i)));
		
		List listeners = m.getChildren("listener");
		for (int i=0; i<listeners.size(); i++){
			String listenerClass = ((Element)listeners.get(i)).getAttributeValue("class");
			mod.addListener(listenerClass);
		}
		
		if (mod instanceof MetaFederationModule){
			List<Element> federated = m.getChildren("federatedmodule");
			for (Element e: federated){
				((MetaFederationModule)mod).addFederatedModule(e.getAttributeValue("key"), e.getAttributeValue("name"));
			}
			
			List<Element> mappings = m.getChildren("mapping");
			for (Element e : mappings){
				String sourceDocumentName = e.getAttributeValue("sourceDocument");
				String targetDocument = e.getAttributeValue("targetDocument");
				String t[] = StringUtils.tokenize(targetDocument, '.');
				FederatedDocumentMapping mapping = new FederatedDocumentMapping();
				mapping.setSourceDocument(sourceDocumentName);
				mapping.setTargetKey(t[0]);
				mapping.setTargetDocument(t[1]);
				((MetaFederationModule)mod).addMapping(mapping);
			}
		}
		
		//parse parameters
		List<Element> parameters = m.getChildren("parameter");
		for (Element p : parameters){
			ModuleParameter param = new ModuleParameter(p.getAttributeValue("name"), p.getAttributeValue("value"));
			System.out.println("Parsed module parameter "+param+" for module "+mod.getName());
			mod.addModuleParameter(param);
		}
		
		
		return mod; 
		
	}
	
	private MetaDocument parseDocument(Element d){
		MetaDocument doc = new MetaDocument(d.getAttributeValue("name"));
		List<Element> properties = d.getChildren("property");
		List<Element> links = d.getChildren("link");

		for (int i=0; i<properties.size(); i++){
			Element p = (Element) properties.get(i);
			doc.addProperty(parseAttribute(p));		
		}
		
		for (int i=0; i<links.size(); i++){
			Element p = (Element) links.get(i);
			doc.addLink((MetaLink)parseAttribute(p));		
		}

		return doc; 		
	}
	
	private MetaProperty parseAttribute(Element e){
		if (e.getName().equals("property"))
			return parseProperty(e);
		if (e.getName().equals("link"))
			return parseLink(e);
		throw new RuntimeException("Unknown attribute type:"+e.getName());
	}
		
	private MetaProperty parseProperty(Element p){
		String name = p.getAttributeValue("name");
		String type = p.getAttributeValue("type");
			
		if (type.equals("table"))
			return parseTable(p);
		if (type.equals("list"))
			return parseList(p);
		if (type.equals("enumeration"))
			return parseEnumeration(p);

		MetaProperty ret = new MetaProperty(name, type);; 
		String multilingual = p.getAttributeValue("multilingual");
		if (multilingual!=null && multilingual.length()>0 && multilingual.equalsIgnoreCase("true"))
			ret.setMultilingual(true);
		return ret;
	}
	
	private MetaEnumerationProperty parseEnumeration(Element p){
		String name = p.getAttributeValue("name");
		String enumeration = p.getAttributeValue("enumeration");
		MetaEnumerationProperty ret = new MetaEnumerationProperty(name, "int");
		ret.setEnumeration(enumeration);
		return ret;
	}
	
	private MetaProperty parseTable(Element p){
		String name = p.getAttributeValue("name");
		MetaTableProperty ret = new MetaTableProperty(name);
		
		List<Element> columns = p.getChildren("column");
		for (int i=0; i<columns.size(); i++){
			Element e = columns.get(i);
				ret.addColumn(e.getAttributeValue("name"));
		}
		
		return ret;
		
	}
	
	private MetaProperty parseList(Element p){
		String name = p.getAttributeValue("name");
		System.out.println("PARSE LIST: " + name);
		MetaListProperty ret = new MetaListProperty(name);
		
		Element content = p.getChild("content");
		Element property = (Element)content.getChildren().get(0);
		MetaProperty containedProperty = parseAttribute(property);
		ret.setContainedProperty(containedProperty);
		
		String multilingual = p.getAttributeValue("multilingual");
		if (multilingual!=null && multilingual.length()>0 && multilingual.equalsIgnoreCase("true"))
			ret.setMultilingual(true);
		return ret;
	}

	private MetaLink parseLink(Element p){
		String name = p.getAttributeValue("name");
		String linkType = p.getAttributeValue("type");
		String target = p.getAttributeValue("target");
		MetaLink l = new MetaLink(name);
		l.setLinkTarget(target);
		l.setLinkType(linkType);
		String multilingual = p.getAttributeValue("multilingual");
		if (multilingual!=null && multilingual.length()>0 && multilingual.equalsIgnoreCase("true"))
			l.setMultilingual(true);

		return l;
	}
}