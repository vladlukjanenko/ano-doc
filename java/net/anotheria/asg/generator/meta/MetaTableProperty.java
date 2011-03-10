package net.anotheria.asg.generator.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * A property type for representation of tables.
 * @author another
 */
public class MetaTableProperty extends MetaContainerProperty{
	/**
	 * Columns of the table.
	 */
	private List<MetaProperty> columns;
	
	public MetaTableProperty(String name){
		super(name); 
		columns = new ArrayList<MetaProperty>();
	}
	
	public void addColumn(String columnName){
		MetaProperty p = new MetaProperty(getName()+"_"+columnName,MetaProperty.Type.LIST);
		columns.add(p);	
	}
	
	/**
	 * @return
	 */
	public List<MetaProperty> getColumns() {
		return columns;
	}

	/**
	 * @param list
	 */
	public void setColumns(List<MetaProperty> list) {
		columns = list;
	}
	
	public String extractSubName(MetaProperty p){
		return p.getName().substring(getName().length()+1);	
	}

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.meta.MetaContainerProperty#getContainerEntryName()
	 */
	public String getContainerEntryName() {
		return "Row";
	}

}
