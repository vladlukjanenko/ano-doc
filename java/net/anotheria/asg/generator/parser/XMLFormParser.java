package net.anotheria.asg.generator.parser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


import net.anotheria.asg.generator.forms.meta.MetaForm;
import net.anotheria.asg.generator.forms.meta.MetaFormField;
import net.anotheria.asg.generator.forms.meta.MetaFormSingleField;
import net.anotheria.asg.generator.forms.meta.MetaFormTableColumn;
import net.anotheria.asg.generator.forms.meta.MetaFormTableField;
import net.anotheria.asg.generator.forms.meta.MetaFormTableHeader;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class XMLFormParser {
	private String content;
	
	public XMLFormParser(String content){
		this.content = content;
	}

	@SuppressWarnings("unchecked")
	public List<MetaForm> parseForms(){
		SAXBuilder reader = new SAXBuilder();
		reader.setValidation(false);
		List ret = new ArrayList();

		try{
			Document doc = reader.build(new StringReader(content));
		
			Element root = doc.getRootElement();
			List<Element> forms = root.getChildren("form");
			for (int i=0; i<forms.size(); i++){
				Element form = forms.get(i);
				ret.add(parseForm(form));
			}
		
		}catch(JDOMException e){
			e.printStackTrace();
		}
		//System.out.println("Parsed forms: "+ret);
		return ret;
	}
	
	private MetaForm parseForm(Element formElement){
	    String id = formElement.getAttributeValue("id");
	    MetaForm form = new MetaForm(id); 
		Element fields = formElement.getChild("fields");
		List<MetaFormField> formFields = parseFields(fields);
		form.setElements(formFields);
		form.setTargets(parseTargets(formElement));
		form.setAction(formElement.getAttributeValue("action"));
		
		return form;
	}
	
	@SuppressWarnings("unchecked")
	private List<String> parseTargets(Element formElement){
	    List<Element> targets = formElement.getChildren("target");
	    List<String> ret = new ArrayList<String>(targets.size());
	    for (int i=0; i<targets.size(); i++){
	        ret.add( targets.get(i).getText() );
	    }
	    return ret;
	}
	
	@SuppressWarnings("unchecked")
	private List<MetaFormField> parseFields(Element fields){
		List<Element> fieldElements = fields.getChildren("field");
		List<MetaFormField> ret = new ArrayList<MetaFormField>(fieldElements.size());
		for (int i=0; i<fieldElements.size(); i++){
			Element field = fieldElements.get(i);
			MetaFormField formField = parseField(field,i+1);
			ret.add(formField);
			
		}
		return ret;
	}
	
	private MetaFormField parseField(Element field, int position){
		String type  = field.getAttributeValue("type");
		if (type.equals("table"))
			return parseTableField(field, position);
		return parseSingleField(field, position);
	}
	private MetaFormTableField parseTableField(Element field, int position){
		Element table = field.getChild("table");
		String name = table.getAttributeValue("name");
		if (name==null || name.length()==0)
			name = "element"+(position);

		int rows = Integer.parseInt(table.getAttributeValue("rows"));
		
		MetaFormTableField ret = new MetaFormTableField(name);
		ret.setRows(rows);
		
		List columns = table.getChildren("column");
		for (int i=0; i<columns.size(); i++){
			MetaFormTableColumn c = parseColumn((Element)columns.get(i));
			ret.addColumn(c);
		}

		return ret;
	}
	
	private MetaFormTableColumn parseColumn(Element e){
		MetaFormTableColumn column = new MetaFormTableColumn();
		String type  = e.getAttributeValue("type");
		int size = 80;
		try{
			size = Integer.parseInt(e.getAttributeValue("size"));
			
		}catch(Exception ignored){}
		MetaFormSingleField field = new MetaFormSingleField ("");
		field.setType(type);
		field.setSize(size);
		column.setField(field);
		
		
		Element headerElement = e.getChild("header");
		MetaFormTableHeader header = new MetaFormTableHeader();
		header.setWidth(headerElement.getAttributeValue("width"));
		header.setKey(headerElement.getText());
		column.setHeader(header);
		
		return column;
		
		
		
	} 
	
	private MetaFormSingleField parseSingleField(Element field, int position){
		String title = field.getChildText("title");
		String type  = field.getAttributeValue("type");
		String name = field.getAttributeValue("name");
		if (name==null || name.length()==0)
		    name = "element"+(position);

		int size = 80;
		try{
			size = Integer.parseInt(field.getAttributeValue("size"));
		}catch(Exception ignored){}
		
		
		MetaFormSingleField element = new MetaFormSingleField (name);
		element.setTitle(title);
		element.setType(type);
		element.setSize(size);
		return element;
	}
	
	
	
}
