package net.anotheria.anodoc.query2;

import org.junit.Test;
import static org.junit.Assert.*;
public class QueryLessThenPropertyTest {
	
	@Test public void doesMatchWithoutIncludingTest(){
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
	
	@Test public void doesMatchWithIncludingTest(){
		QueryProperty qp = new QueryLessThenProperty<Integer>("property1", 100, true);
		assertTrue(qp.doesMatch(-99));
		assertTrue(qp.doesMatch(99));
		assertTrue(qp.doesMatch(100));
		assertFalse(qp.doesMatch(101));
		assertFalse(qp.doesMatch(1001));
		
		
		qp = new QueryLessThenProperty<Long>("property1", 100L, true);
//		assertTrue(qp.doesMatch(99));
		assertTrue(qp.doesMatch(99L));
//		assertFalse(qp.doesMatch(100));
		assertTrue(qp.doesMatch(100L));
//		assertFalse(qp.doesMatch(101));
		assertFalse(qp.doesMatch(101L));
		
		qp = new QueryLessThenProperty<Double>("property1", 100.00, true);
		assertTrue(qp.doesMatch(-99D));
		assertTrue(qp.doesMatch(99D));
		assertTrue(qp.doesMatch(100D));
		assertFalse(qp.doesMatch(101D));
		assertFalse(qp.doesMatch(1001D));
		
		qp = new QueryLessThenProperty<String>("property1", "bbb", true);
		assertTrue(qp.doesMatch("aaa"));
		assertTrue(qp.doesMatch("bb"));
		assertTrue(qp.doesMatch("bbb"));
		assertFalse(qp.doesMatch("bbc"));
		assertFalse(qp.doesMatch("bbbb"));
		
	}
	
	@Test public void getComparatorTest(){
		QueryProperty qp = new QueryLessThenProperty<Integer>("property1", 100);
		assertEquals("<", qp.getComparator().trim());
		qp = new QueryLessThenProperty<Integer>("property1", 100, true);
		assertEquals("<=", qp.getComparator().trim());
	}

}
