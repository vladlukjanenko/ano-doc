package net.anotheria.anodoc.query2;

import org.junit.Test;
import static org.junit.Assert.*;
public class QueryLessThenPropertyTest {
	
	@Test public void doesMatchTest(){
		QueryProperty qp = new QueryLessThenProperty<Integer>("property1", 100);
		assertTrue(qp.doesMatch(-99));
		assertTrue(qp.doesMatch(99));
		assertFalse(qp.doesMatch(100));
		assertFalse(qp.doesMatch(101));
		assertFalse(qp.doesMatch(1001));
		
		
		qp = new QueryLessThenProperty<Long>("property1", 100L);
//		assertTrue(qp.doesMatch(99));
		assertTrue(qp.doesMatch(99L));
//		assertFalse(qp.doesMatch(100));
		assertFalse(qp.doesMatch(100L));
//		assertFalse(qp.doesMatch(101));
		assertFalse(qp.doesMatch(101L));
		
		qp = new QueryLessThenProperty<Double>("property1", 100.00);
		assertTrue(qp.doesMatch(-99D));
		assertTrue(qp.doesMatch(99D));
		assertFalse(qp.doesMatch(100D));
		assertFalse(qp.doesMatch(101D));
		assertFalse(qp.doesMatch(1001D));
		
		qp = new QueryLessThenProperty<String>("property1", "bbb");
		assertTrue(qp.doesMatch("aaa"));
		assertTrue(qp.doesMatch("bb"));
		assertFalse(qp.doesMatch("bbb"));
		assertFalse(qp.doesMatch("bbc"));
		assertFalse(qp.doesMatch("bbbb"));
	}

}
