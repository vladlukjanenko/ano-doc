package net.anotheria.asg.generator.parser;


import java.io.IOException;
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
 * XMLParser for the data definition files.
 * @author another
 */
public final class XMLDataParser {
	
	@SuppressWarnings("unchecked")
	public static final List<MetaModule> parseModules(String content){
		
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
		}catch(IOException e){
			e.printStackTrace();
		}
		return ret;
	}
	
	private static final MetaModule parseModule(Element m){
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
		@SuppressWarnings("unchecked")List<Element> childs = m.getChildren("document");
		for (int i=0; i<childs.size(); i++)
			mod.addDocument(parseDocument(childs.get(i)));
		
		@SuppressWarnings("unchecked")List<Element> listeners = m.getChildren("listener");
		for (int i=0; i<listeners.size(); i++){
			String listenerClass = listeners.get(i).getAttributeValue("class");
			mod.addListener(listenerClass);
		}
		
		if (mod instanceof MetaFederationModule){
			@SuppressWarnings("unchecked")List<Element> federated = m.getChildren("federatedmodule");
			for (Element e: federated){
				((MetaFederationModule)mod).addFederatedModule(e.getAttributeValue("key"), e.getAttributeValue("name"));
			}
			
			@SuppressWarnings("unchecked")List<Element> mappings = m.getChildren("mapping");
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
		@SuppressWarnings("unchecked")List<Element> parameters = m.getChildren("parameter");
		for (Element p : parameters){
			ModuleParameter param = new ModuleParameter(p.getAttributeValue("name"), p.getAttributeValue("value"));
			System.out.println("Parsed module parameter "+param+" for module "+mod.getName());
			mod.addModuleParameter(param);
		}
		
		Element optionsEleemnt = m.getChild("options");
		if (optionsEleemnt!=null)
			mod.setModuleOptions(OptionsParser.parseOptions(optionsEleemnt));

		return mod; 
		
	}
	
	/**
	 * Parses a document from a document tag.
	 * @param d
	 * @return
	 */
	private static final MetaDocument parseDocument(Element d){
		MetaDocument doc = new MetaDocument(d.getAttributeValue("name"));
		@SuppressWarnings("unchecked")List<Element> properties = d.getChildren("property");
		@SuppressWarnings("unchecked")List<Element> links = d.getChildren("link");

		for (int i=0; i<properties.size(); i++){
			Element p = properties.get(i);
			doc.addProperty(parseAttribute(p));		
		}
		
		for (int i=0; i<links.size(); i++){
			Element p = links.get(i);
			doc.addLink((MetaLink)parseAttribute(p));		
		}

		return doc; 		
	}
	
	/**
	 * Parses an attribute of a document. That may be a link or a property.
	 * @param e
	 * @return
	 */
	private static final MetaProperty parseAttribute(Element e){
		//System.out.println("Parsing MetaProperty: "+e+", ->>"+e.getAttributeValue("name")+"<<-");
		if (e.getName().equals("property"))
			return parseProperty(e);
		if (e.getName().equals("link"))
			return parseLink(e);
		throw new RuntimeException("Unknown attribute type:"+e.getName());
	}
		
	/**
	 * Parses a property. May call subsequent methods depending on property type.
	 * @param p
	 * @return
	 */
	private static final MetaProperty parseProperty(Element p){
		String name = p.getAttributeValue("name");
		String typeStr = p.getAttributeValue("type");
			
		if (typeStr.equals("table"))
			return parseTable(p);
		if (typeStr.equals("list"))
			return parseList(p);
		if (typeStr.equals("enumeration"))
			return parseEnumeration(p);

		MetaProperty.Type type = MetaProperty.Type.findTypeByName(typeStr);
		if(type == null)
			throw new IllegalArgumentException("Uknown type <" + typeStr + "> for property def " + p);
		
		MetaProperty ret = new MetaProperty(name, type); 
		String multilingual = p.getAttributeValue("multilingual");
		if (multilingual!=null && multilingual.length()>0 && multilingual.equalsIgnoreCase("true"))
			ret.setMultilingual(true);
//		String validatorName = p.getAttributeValue("validator"); 
//		if (validatorName != null ) {
//			MetaValidator validator = GeneratorDataRegistry.getInstance().getValidator(validatorName);
//			if (validator == null) {
//				throw new IllegalArgumentException("Uknown validator <" + validatorName + "> for property def " + p+
//						". Check that you have validators-def.xml in classpath and validator is present there.");
//			}
//			ret.setValidator(validator);
//		}
		return ret;
	}
	
	/**
	 * Parser for enumeration properties.
	 * @param p
	 * @return
	 */
	private static final MetaEnumerationProperty parseEnumeration(Element p){
		String name = p.getAttributeValue("name");
		String enumeration = p.getAttributeValue("enumeration");
		MetaEnumerationProperty ret = new MetaEnumerationProperty(name, MetaProperty.Type.INT);
		ret.setEnumeration(enumeration);
		return ret;
	}
	
	/**
	 * Parses table properties.
	 * @param p
	 * @return
	 */
	private static final MetaProperty parseTable(Element p){
		String name = p.getAttributeValue("name");
		MetaTableProperty ret = new MetaTableProperty(name);
		
		@SuppressWarnings("unchecked")List<Element> columns = p.getChildren("column");
		for (int i=0; i<columns.size(); i++){
			Element e = columns.get(i);
				ret.addColumn(e.getAttributeValue("name"));
		}
		
		return ret;
		
	}
	
	/**
	 * Parses list properties.
	 * @param p
	 * @return
	 */
	private static final MetaProperty parseList(Element p){
		String name = p.getAttributeValue("name");
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

	/**
	 * Parses a link.
	 * @param p
	 * @return
	 */
	private static final MetaLink parseLink(Element p){
		String name = p.getAttributeValue("name");
		String linkType = p.getAttributeValue("type");
		String target = p.getAttributeValue("target");
		String decorationStr = p.getAttributeValue("decoration");
		List<String> decoration;
		if(StringUtils.isEmpty(decorationStr)){
			decoration = new ArrayList<String>();
			decoration.add("name");
		}else{
			decoration = StringUtils.tokenize2list(decorationStr, ',');
		}
		
		MetaLink l = new MetaLink(name);
		l.setLinkTarget(target);
		l.setLinkType(linkType);
		l.setLinkDecoration(decoration);
		String multilingual = p.getAttributeValue("multilingual");
		if (multilingual!=null && multilingual.length()>0 && multilingual.equalsIgnoreCase("true"))
			l.setMultilingual(true);

		return l;
	}
}