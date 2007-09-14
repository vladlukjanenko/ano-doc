package net.anotheria.asg.generator.forms.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaFormTableColumn {
	private MetaFormSingleField field;
	private MetaFormTableHeader header;
	
	public MetaFormTableColumn(){
		
	}
	
	public MetaFormTableColumn(MetaFormTableHeader aHeader, MetaFormSingleField aField){
		header = aHeader;
		field = aField;
	}
	
	public String toString(){
		return "column "+header+", "+field;
	}
	/**
	 * @return
	 */
	public MetaFormSingleField getField() {
		return field;
	}

	/**
	 * @return
	 */
	public MetaFormTableHeader getHeader() {
		return header;
	}

	/**
	 * @param field
	 */
	public void setField(MetaFormSingleField field) {
		this.field = field;
	}

	/**
	 * @param header
	 */
	public void setHeader(MetaFormTableHeader header) {
		this.header = header;
	}

}
