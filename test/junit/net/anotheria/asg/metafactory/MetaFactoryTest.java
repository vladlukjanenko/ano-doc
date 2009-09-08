package net.anotheria.asg.metafactory;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
public class MetaFactoryTest {
	
	@Before public void reinit(){
		System.out.println("Reseting...");
		MetaFactory.reset();
	}
	
	@Test
	public void testPriority(){
		for (int i=0; i<10; i++){
			TestAliasResolver r = new TestAliasResolver();
			r.setPriority(100-i);
			MetaFactory.addAliasResolver(r);
		}
		
		List<AliasResolver> resolverList = MetaFactory.getAliasResolverList();
		assertEquals(91, resolverList.get(2).getPriority());
		
		assertEquals("test.91", MetaFactory.resolveAlias("test"));
		
		System.setProperty(SystemPropertyResolver.PROPERTY_PREFIX+"test", "a_system_property");
		assertEquals("a_system_property.91", MetaFactory.resolveAlias("test"));
		
	}
	
	@Test public void resolveAliasTest(){
		//Testing configurable aliases (factories.json)
		assertEquals("foo.bar.XxxService", MetaFactory.resolveAlias("XxxService"));
		assertEquals("foo.bar.XxxService", MetaFactory.resolveAlias("CmsXxxService"));
		assertEquals("foo.bar.XxxService", MetaFactory.resolveAlias("DomainXxxService"));
		
		//Now add new aliases from code 
		MetaFactory.addAlias("foo.bar.YyyService", "YyyService");
		MetaFactory.addAlias("YyyService", "CmsYyyService");
		MetaFactory.addAlias("YyyService", "DomainYyyService");
		
		//Testing just coded aliases
		assertEquals("foo.bar.YyyService", MetaFactory.resolveAlias("YyyService"));
		assertEquals("foo.bar.YyyService", MetaFactory.resolveAlias("CmsYyyService"));
		assertEquals("foo.bar.YyyService", MetaFactory.resolveAlias("DomainYyyService"));
		
		//Now mix up coded alias with configurable one
		MetaFactory.addAlias("XxxService", "FoobarXxxService");
		//Testing aliases mix up
		assertEquals("foo.bar.XxxService", MetaFactory.resolveAlias("FoobarXxxService"));

		//Overriding configurable alias
		MetaFactory.addAlias("foobar.XxxService", "XxxService");
		
		/*
		 *NOTE: MetaFactory.addAlias doesn't override aliases resolved upon resolversList. 
		 */
		//Failure assertion
		//assertEquals("foobar.XxxService", MetaFactory.resolveAlias("XxxService"));

		//Alias from configuration is not overrode 
		assertEquals("foo.bar.XxxService", MetaFactory.resolveAlias("XxxService"));
		
		//Testing unregistered alias: must be resolved to itself
		assertEquals("UnknownService", MetaFactory.resolveAlias("UnknownService"));
	}
	
}
