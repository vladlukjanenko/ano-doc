package net.anotheria.asg.generator.forms.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaFormTableField extends MetaFormField{
	
	private int rows;
	private List<MetaFormTableColumn> columns;
	
	public MetaFormTableField(String aName){
		super(aName);
		columns = new ArrayList<MetaFormTableColumn>();
	}

	public boolean isSingle(){
		return false;
	}
    
	public boolean isComplex(){
		return true;
	}

	/**
	 * @return
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * @param i
	 */
	public void setRows(int i) {
		rows = i;
	}
	
	public String toString(){
		return "table "+getName()+" with "+rows+" row(s) and columns: "+columns;
	}
	
	public void addColumn(MetaFormTableColumn column){
		columns.add(column);
	}

	/**
	 * @return
	 */
	public List<MetaFormTableColumn> getColumns() {
		return columns;
	}

	/**
	 * @param list
	 */
	public void setColumns(List<MetaFormTableColumn> list) {
		columns = list;
	}
	
	public String getVariableName(int row, int column){
		return getName()+"R"+(row+1)+"C"+(column+1);		
	}
	

}
 