package net.anotheria.asg.data;

import net.anotheria.util.xml.XMLAttribute;
import net.anotheria.util.xml.XMLNode;

import java.util.List;

/**
 * A helper class for creation of xml trees.
 * @author lrosenberg
 *
 */
public class XMLHelper {
//	public static <T> XMLNode createXMLNodeForListValue(String name, String[] language, List<T>... value){
//		return createXMLNodeWithContent(name, language, "list", "not_yet_implemented");
//	}
	
	public static <T> XMLNode createXMLNodeForListValue(String name, String contentType, List<T> content){
		XMLNode ret = new XMLNode(name);
		
		XMLNode val = new XMLNode("value");
		ret.addChildNode(val);
		
		val.addAttribute(new XMLAttribute("type", "list"));
		val.addAttribute(new XMLAttribute("contentType", contentType));
		for(Object c:content){
			XMLNode el = new XMLNode("element");
			val.addChildNode(el);
			el.setContent(""+c);
		}
		return ret;
	}
	
	public static XMLNode createXMLNodeForImageValue(String name, String[] language, String... value){
		return createXMLNodeForStringValue(name, language, value);
	}

	public static XMLNode createXMLNodeForTextValue(String name, String[] language, String... value){
		return createXMLNodeForStringValue(name, language, value);
	}

    public static XMLNode createXMLNodeForPasswordValue(String name, String[] language, String... value){
        return createXMLNodeForStringValue(name, language, value);
    }

	public static XMLNode createXMLNodeForStringValue(String name, String[] language, String... value){
		return createXMLNodeWithContent(name, language, "string",(Object[])value);
	}
	
	public static XMLNode createXMLNodeForBooleanValue(String name, String[] languages, boolean... value){
		return createXMLNodeWithAttribute(name, languages, "boolean", (Object[])makeArray(value));
	}
	
	public static XMLNode createXMLNodeForIntValue(String name, String[] languages, int... value){
		return createXMLNodeWithAttribute(name, languages, "int", (Object[])makeArray(value));
	}

	public static XMLNode createXMLNodeForLongValue(String name, String[] languages, long... value){
		return createXMLNodeWithAttribute(name, languages, "long", (Object[])makeArray(value));
	}
	
	public static XMLNode createXMLNodeForDoubleValue(String name, String[] languages, double... value){
		return createXMLNodeWithAttribute(name, languages, "double", (Object[])makeArray(value));
	}

	public static XMLNode createXMLNodeForFloatValue(String name, String[] languages, float... value){
		return createXMLNodeWithAttribute(name, languages, "float", (Object[])makeArray(value));
	}
	



	private static XMLNode createXMLNodeWithContent(String name, String[] languages, String type, Object... value){
		XMLNode ret = new XMLNode(name);
		
		if (languages==null){
			XMLNode val = new XMLNode("value");
			if (type!=null)
				val.addAttribute(new XMLAttribute("type", type));
			ret.addChildNode(val);
			if (value!=null)
				val.setContent(""+value[0]);
		}else{
			for (int i=0; i<languages.length; i++){
				String l = languages[i];
				XMLNode val = new XMLNode("value");
				if (type!=null)
					val.addAttribute(new XMLAttribute("type", type));
				
				val.addAttribute(new XMLAttribute("language", l));

				if (value!=null){
                    Object valueToSet = value[i]!=null?value[i]:"";
					val.setContent("" + valueToSet);
                }
				ret.addChildNode(val);
				
			}
		}
		return ret;
	}

	private static XMLNode createXMLNodeWithAttribute(String name, String[] languages, String type, Object... value){
		XMLNode ret = new XMLNode(name);
		
		if (languages==null){
			XMLNode val = new XMLNode("value");
			if (type!=null)
				val.addAttribute(new XMLAttribute("type", type));
			ret.addChildNode(val);
			if (value!=null)
				val.addAttribute(new XMLAttribute("value", ""+value[0]));
		}else{
			for (int i=0; i<languages.length; i++){
				String l = languages[i];
				System.out.println(l+" -> "+value[i]);
				XMLNode val = new XMLNode("value");
				if (type!=null)
					val.addAttribute(new XMLAttribute("type", type));
				
				val.addAttribute(new XMLAttribute("language", l));

				if (value!=null)
					val.addAttribute(new XMLAttribute("value", ""+value[i]));
				
				ret.addChildNode(val);
				
			}
		}
		return ret;
	}
	
	private static Integer[] makeArray(int[] in){
		Integer[] out = new Integer[in.length];
		for (int i=0; i<in.length; i++)
			out[i]=in[i];
		return out;
	}

	private static Long[] makeArray(long[] in){
		Long[] out = new Long[in.length];
		for (int i=0; i<in.length; i++)
			out[i]=in[i];
		return out;
	}

	private static Boolean[] makeArray(boolean[] in){
		Boolean[] out = new Boolean[in.length];
		for (int i=0; i<in.length; i++)
			out[i]=in[i];
		return out;
	}

	private static Double[] makeArray(double[] in){
		Double[] out = new Double[in.length];
		for (int i=0; i<in.length; i++)
			out[i]=in[i];
		return out;
	}

	private static Float[] makeArray(float[] in){
		Float[] out = new Float[in.length];
		for (int i=0; i<in.length; i++)
			out[i]=in[i];
		return out;
	}
	
	/**
	 * Prevent instantiation.
	 */
	private XMLHelper(){
		
	}
}
