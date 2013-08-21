package net.anotheria.asg.generator.parser;

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
import net.anotheria.asg.generator.view.meta.MetaValidator;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.asg.generator.view.meta.MetaViewElement;
import net.anotheria.util.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * XML Parser for MetaViews.
 * @author another
 */
public final class XMLViewParser {

	/**
	 * {@link Logger} instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(XMLViewParser.class);
	
	public static final List<MetaView> parseViews(String content){
		
		SAXBuilder reader = new SAXBuilder();
		reader.setValidation(false);
		List<MetaView> ret = new ArrayList<MetaView>();

		try{
			Document doc = reader.build(new StringReader(content));
			Element root = doc.getRootElement();
			@SuppressWarnings("unchecked")List<Element> views = root.getChildren("view");
			for (Element elem :views){
				MetaView view = parseView(elem);
				ret.add(view);
			}
		}catch(JDOMException e){
			LOGGER.error("parseViews", e);
			throw new RuntimeException("Can't parse view because: "+e.getMessage());
		}catch(IOException e){
			LOGGER.error("parseViews", e);
			throw new RuntimeException("Can't parse view because: "+e.getMessage());
		}
		return ret;
	}

	private static final MetaView parseView(Element m){
		
		String name = m.getAttributeValue("name");
		MetaView view = new MetaView(name);

		//System.out.println("parsing view: "+name);
		
		@SuppressWarnings("unchecked")List<Element> sections = m.getChild("sections").getChildren();
		for (int i=0; i<sections.size(); i++)
			view.addSection(parseSection(sections.get(i)));
		
		view.setTitle(m.getAttributeValue("title"));
		view.setCms20("2.0".equals(m.getAttributeValue("version")));
		
		String roles = m.getAttributeValue("requiredroles");
		if (roles != null && roles.trim().length()!=0)
			view.setRequiredRoles(Arrays.asList(StringUtils.tokenize(roles.trim(), ',')));
		
		return view;
	}
	
	private static final MetaSection parseSection(Element section){
		String type = section.getAttributeValue("type");
		if (type.equals("module"))
			return parseModuleSection(section);
		if (type.equals("custom"))
			return parseCustomSection(section);
		throw new RuntimeException("Unknown section type: "+type);
	}
	
	private static final MetaCustomSection parseCustomSection(Element section){
		String title = section.getAttributeValue("title");
		String path = section.getChildText("path");
		MetaCustomSection ret = new MetaCustomSection(title);
		ret.setPath(path);
		return ret;
	}
	
	private static final MetaModuleSection parseModuleSection(Element section){
		String title = section.getAttributeValue("title");
		//System.out.println("Parse section:; "+title);
		MetaModuleSection s = new MetaModuleSection(title);
		String moduleName = section.getChildText("module");
		String documentName = section.getChildText("document");
		MetaModule mod = GeneratorDataRegistry.getInstance().getModule(moduleName);
		if (mod==null)
			throw new RuntimeException("Module "+moduleName+" not found (parsing section: "+title+")");
		s.setModule(mod); 
		s.setDocument(mod.getDocumentByName(documentName));
		
		Element elementsRoot = section.getChild("elements");
		@SuppressWarnings("unchecked")List<Element> elements = elementsRoot.getChildren();
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = parseViewElement(s, elements.get(i));
			s.addElement(element);
		}
		
		@SuppressWarnings("unchecked")List<Element> dialogs = section.getChildren("dialog");
		//System.out.println("To parse "+dialogs.size()+" dialogs.");
		for (int i=0; i<dialogs.size(); i++){
			Element d = dialogs.get(i);
			MetaDialog dialog = new MetaDialog(d.getAttributeValue("name"));
			dialog.setTitle(d.getAttributeValue("title"));
			dialog.setJavascript(d.getAttributeValue("javascript"));
			@SuppressWarnings("unchecked")List<Element> dialogElements = d.getChild("elements").getChildren();
			for (int e=0; e<dialogElements.size(); e++){
				MetaViewElement element = parseViewElement(null, dialogElements.get(e));
				dialog.addElement(element);
			}
			s.addDialog(dialog);			
		}
		
		@SuppressWarnings("unchecked")List<Element> filters = section.getChildren("filter");
		for (Element f : filters){
			s.addMetaFilter(GeneratorDataRegistry.getInstance().createFilter(f.getAttributeValue("name"), f.getAttributeValue("field")));
		}
		
		return s;
	}
	
	private static final MetaViewElement parseViewElement(MetaModuleSection section, Element elem){
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
		
		String fieldCaption = elem.getAttributeValue("caption");
		if (!StringUtils.isEmpty(fieldCaption)) {
			element.setCaption(fieldCaption);
		}
		String description = elem.getChildText("description");
		if (!StringUtils.isEmpty(description)) {
			element.setDescription(description);
		}
		String validatorsName = elem.getAttributeValue("validator"); 
		if (validatorsName != null ) {
			String[] validators = validatorsName.split(",");
			List<MetaValidator> metaValidators = new ArrayList<MetaValidator>(validators.length);
			for (String validatorName : validators) {
				MetaValidator validator = GeneratorDataRegistry.getInstance().getValidator(validatorName);
				metaValidators.add(validator);
				if (validator == null) {
					throw new IllegalArgumentException("Uknown validator <" + validatorName + "> for view element def " + elem+
					". Check that you have validators-def.xml in classpath and validator definition is present there.");
				}
			}
			element.setValidators(metaValidators);
		}
		
		try{
			Element decElement = elem.getChild("decorator");
			if (decElement!=null){
				element.setDecorator(parseDecorator(decElement));
				//System.out.println("found decorator!");
			}
		}catch(Exception e){
			LOGGER.error("*********** Could not parse decorator cause: " + e.getMessage() + " ***********");
		}
		
		return element;
		
	}
	
	private static final MetaDecorator parseDecorator(Element e){
		String name = e.getAttributeValue("name");
		String rule = e.getChildText("rule");
		MetaDecorator dec = GeneratorDataRegistry.getInstance().createDecorator(name, rule);
		return dec;
	}
	
	private static final MetaFieldElement parseFieldElement(Element elem){
		String name = elem.getAttributeValue("name");
		String readonly = elem.getAttributeValue("readonly");
        String autocompleteOff = elem.getAttributeValue("autocompleteOff");
		String rich = elem.getAttributeValue("rich");
		String datetime = elem.getAttributeValue("datetime");
		MetaFieldElement field = new MetaFieldElement(name); 
		if (readonly!=null && readonly.equals("true"))
			field.setReadonly(true);
		if (rich!=null && rich.equals("true"))
            field.setRich(true);
        if (autocompleteOff!=null && autocompleteOff.equals("true"))
            field.setAutocompleteOff(true);
        if (datetime != null && datetime.equals("true"))
			field.setDatetime(true);
		
		return field;
	}
	
	private static final MetaEmptyElement parseEmptyElement(Element elem){
		return new MetaEmptyElement();
	}

	private static final MetaFunctionElement parseFunctionElement(Element elem){
		String name = elem.getAttributeValue("name");
		MetaFunctionElement  ret = new MetaFunctionElement(name);
		if (elem.getAttribute("caption")!=null)
			ret.setCaption(elem.getAttributeValue("caption")); 
		return ret;
	}
	
	private static final MetaCustomFunctionElement parseCustomFunctionElement(Element elem){
		String name = elem.getAttributeValue("name");
		MetaCustomFunctionElement  ret = new MetaCustomFunctionElement(name);
		ret.setCaption(elem.getChildText("caption"));
		ret.setLink(elem.getChildText("link"));
		return ret;
	}

	private static final MetaListElement parseListElement(Element e){
		MetaListElement ret = new MetaListElement();
		@SuppressWarnings("unchecked")List<Element> elements = e.getChildren("element");
		for (int i=0; i<elements.size(); i++)
			ret.addElement(parseViewElement(null, elements.get(i)));
		return ret; 	
	}
	
	private XMLViewParser(){
		
	}
	
}
