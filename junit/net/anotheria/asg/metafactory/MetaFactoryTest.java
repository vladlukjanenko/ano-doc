package net.anotheria.asg.metafactory;

import java.util.List;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
public class MetaFactoryTest {
	
	@Test
	public void testPriority(){
		for (int i=0; i<10; i++){
			TestAliasResolver r = new TestAliasResolver();
			r.setPriority(100-i);
			MetaFactory.addAliasResolver(r);
		}
		
		List<AliasResolver> resolverList = MetaFactory.getAliasResolverList();
		assertEquals(91, resolverList.get(1).getPriority());
		
		assertEquals("test.91", MetaFactory.resolveAlias("test"));
		
		System.setProperty(SystemPropertyResolver.PROPERTY_PREFIX+"test", "a_system_property");
		assertEquals("a_system_property.91", MetaFactory.resolveAlias("test"));
		
	}
}
