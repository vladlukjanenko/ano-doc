package net.anotheria.asg.generator.view.meta;


/**
 * This enum lists sorting possibilites for elements in the cms.
 * @author lrosenberg
 *
 */
public enum SortingType {
	/**
	 * Default is alphabethical (a,b,c)
	 */
	ALPHABETHICAL,
	/**
	 * Numerical for ids and other numbers.
	 */
	NUMERICAL{
		public String getJavaType(){
			return "int";
		}
		public String getCompareCall(){
			return "compareInt";
		}
		public String convertValue(String variableName){
			return "Integer.parseInt("+variableName+")";
		}
	}
	,
	/**
	 * Containers is for lists and other containers.
	 */
	CONTAINERS{
		public String getJavaType(){
			return "List<String>";
		}
		
		public String getCompareCall(){
			return "compareList";
		}
		 
	}
	;
	public String getJavaType(){
		return "String"; 
	}
	
	public String getCompareCall(){
		return "compareString";
	}
	
	public String convertValue(String variableName){
		return variableName;
	}
}
