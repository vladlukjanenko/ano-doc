package net.anotheria.anodoc;

import net.anotheria.anodoc.query2.QueryLessThenPropertyTest;
import net.anotheria.asg.metafactory.MetaFactoryTest;
import net.anotheria.asg.util.data.DataObjectUtilsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(value=Suite.class)
@SuiteClasses(value={QueryLessThenPropertyTest.class, MetaFactoryTest.class,DataObjectUtilsTest.class})
public class AnoDocTestSuite {

}
