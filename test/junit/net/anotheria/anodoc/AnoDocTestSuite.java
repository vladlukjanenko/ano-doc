package net.anotheria.anodoc;

import net.anotheria.anodoc.query2.QueryCaseInsensitiveLikePropertyTest;
import net.anotheria.anodoc.query2.QueryLessThenPropertyTest;
import net.anotheria.anodoc.query2.string.ContainsStringQueryTest;
import net.anotheria.anodoc.query2.string.ContainsWordsQueryTest;
import net.anotheria.anodoc.query2.string.SimpleContainsStringQueryTest;
import net.anotheria.asg.util.data.DataObjectUtilsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(value=Suite.class)
@SuiteClasses(value={QueryCaseInsensitiveLikePropertyTest.class, QueryLessThenPropertyTest.class, DataObjectUtilsTest.class,
        ContainsStringQueryTest.class, ContainsWordsQueryTest.class, SimpleContainsStringQueryTest.class})
public class AnoDocTestSuite {

}
