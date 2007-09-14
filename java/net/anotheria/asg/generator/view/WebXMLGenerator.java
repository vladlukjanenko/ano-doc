package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.Generator;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.IOUtils;
import net.anotheria.util.StringUtils;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class WebXMLGenerator extends AbstractGenerator {
	public static final String SERVLETS_PLACEHOLDER = "<insert_servlets/>";
	public static final String TEMPLATE = "etc/templates/web-template.xml";
	
	public static final String SERVLET_NAME_EDITOR = "editor";
	
	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
	 */
	public List<FileEntry> generate(List<MetaView> views, Context context) {
		List<FileEntry> files = new ArrayList<FileEntry>();
	
		String servletsContent = generateServlets(views, context);
		String file = "";
		try{
			file = IOUtils.readFileAtOnceAsString(Generator.getBaseDir()+TEMPLATE);
		}catch(Exception e){
			e.printStackTrace();
		}
		String fileContent = StringUtils.replaceOnce(file, SERVLETS_PLACEHOLDER, servletsContent);
		
		FileEntry entry = new FileEntry("/etc/httpd", "web", fileContent);
		entry.setType(".xml");
		files.add(entry);
	
		return files;
	}
	
	private String generateServlets(List<MetaView> views, Context context){
		increaseIdent();
		increaseIdent();
		String ret = "";

		ret += writeString("<servlet>");
		increaseIdent();
		ret += writeString("<servlet-name>"+SERVLET_NAME_EDITOR+"</servlet-name>");
		ret += writeString("<servlet-class>net.anotheria.webutils.servlet.ControllerServlet</servlet-class>");
		ret += writeString("<init-param>");
		ret += writeIncreasedString("<param-name>application</param-name>");
		ret += writeIncreasedString("<param-value>ApplicationResources</param-value>");
		ret += writeString("</init-param>");
		ret += writeString("<init-param>");
		increaseIdent();
		ret += writeString("<param-name>config</param-name>");
		ret += writeString("<param-value>");
		ret += writeIncreasedString("/WEB-INF/appdata/struts-config.xml,");
		for (MetaView view : views){
			ret += writeIncreasedString("/WEB-INF/appdata/"+StrutsConfigGenerator.getConfigFileName(view)+".xml");
			
		}
		ret += writeString("</param-value>");
		decreaseIdent();
		ret += writeString("</init-param>");
		ret += writeString("<load-on-startup>5</load-on-startup>");
		decreaseIdent();
		ret += writeString("</servlet>");
		
		ret += writeString("<servlet-mapping>");
		increaseIdent();
		ret += writeString("<servlet-name>"+SERVLET_NAME_EDITOR+"</servlet-name>");
		ret += writeString("<url-pattern>/"+context.getServletMapping()+"/*</url-pattern>");
		decreaseIdent();
		ret += writeString("</servlet-mapping>");
		
		
		decreaseIdent();
		decreaseIdent();
		return ret;
	}
	

}


