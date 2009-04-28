package net.anotheria.asg.metafactory;

public class TestAliasResolver implements AliasResolver{

	private int priority;

	void setPriority(int aPriority){
		priority = aPriority;
	}
	
	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public String resolveAlias(String alias) {
		String p = "."+priority;
		if (alias.indexOf('.')==-1)
			return alias + p;
		return null;
	}
	
	public String toString(){
		return "Resolver P"+getPriority();
	}
}
