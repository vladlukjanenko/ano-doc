package net.anotheria.asg.metafactory;

public class SystemPropertyResolver implements AliasResolver{
	
	public static final String PROPERTY_PREFIX = "ano.doc.mf-alias";

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public String resolveAlias(String alias) {
		return System.getProperty(PROPERTY_PREFIX+alias);
	}

	
	
}
