package net.anotheria.asg.metafactory;

import org.junit.BeforeClass;
import org.junit.Test;
import static junit.framework.Assert.*;

public class ConfigurableResolverTest {
	
	private static ConfigurableResolver resolver;
	
	@BeforeClass public static void setUp(){
		resolver = new ConfigurableResolver();
	}
	
	@Test public void resolveAliasTest(){
		assertEquals("foo.bar.XxxService", resolver.resolveAlias("XxxService"));
		assertEquals("XxxService", resolver.resolveAlias("DomainXxxService"));
		assertEquals("XxxService", resolver.resolveAlias("CmsXxxService"));
		assertNull(resolver.resolveAlias("UknownXxxService"));
	}
}
