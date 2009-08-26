package net.anotheria.anodoc.query2;

import org.junit.Test;
import static org.junit.Assert.*;
public class QueryMoreThenPropertyTest {
	
	@Test public void doesMatchWithoutIncludingTest(){
		QueryProperty qp = new QueryMoreThenProperty<Integer>("property1", 100);
		assertFalse(qp.doesMatch(-99));
		assertFalse(qp.doesMatch(99));
		assertFalse(qp.doesMatch(100));
		assertTrue(qp.doesMatch(101));
		assertTrue(qp.doesMatch(1001));
		
		
		qp = new QueryMoreThenProperty<Long>("property1", 100L);
//		assertTrue(qp.doesMatch(99));
		assertFalse(qp.doesMatch(99L));
//		assertFalse(qp.doesMatch(100));
		assertFalse(qp.doesMatch(100L));
//		assertFalse(qp.doesMatch(101));
		assertTrue(qp.doesMatch(101L));
		
		qp = new QueryMoreThenProperty<Double>("property1", 100.00);
		assertFalse(qp.doesMatch(-99D));
		assertFalse(qp.doesMatch(99D));
		assertFalse(qp.doesMatch(100D));
		assertTrue(qp.doesMatch(101D));
		assertTrue(qp.doesMatch(1001D));
		
		qp = new QueryMoreThenProperty<String>("property1", "bbb");
		assertFalse(qp.doesMatch("aaa"));
		assertFalse(qp.doesMatch("bb"));
		assertFalse(qp.doesMatch("bbb"));
		assertTrue(qp.doesMatch("bbc"));
		assertTrue(qp.doesMatch("bbbb"));
		
	}
	
	@Test public void doesMatchWithIncludingTest(){
		QueryProperty qp = new QueryMoreThenProperty<Integer>("property1", 100, true);
		assertFalse(qp.doesMatch(-99));
		assertFalse(qp.doesMatch(99));
		assertTrue(qp.doesMatch(100));
		assertTrue(qp.doesMatch(101));
		assertTrue(qp.doesMatch(1001));
		
		
		qp = new QueryMoreThenProperty<Long>("property1", 100L, true);
//		assertTrue(qp.doesMatch(99));
		assertFalse(qp.doesMatch(99L));
//		assertFalse(qp.doesMatch(100));
		assertTrue(qp.doesMatch(100L));
//		assertFalse(qp.doesMatch(101));
		assertTrue(qp.doesMatch(101L));
		
		qp = new QueryMoreThenProperty<Double>("property1", 100.00, true);
		assertFalse(qp.doesMatch(-99D));
		assertFalse(qp.doesMatch(99D));
		assertTrue(qp.doesMatch(100D));
		assertTrue(qp.doesMatch(101D));
		assertTrue(qp.doesMatch(1001D));
		
		qp = new QueryMoreThenProperty<String>("property1", "bbb", true);
		assertFalse(qp.doesMatch("aaa"));
		assertFalse(qp.doesMatch("bb"));
		assertTrue(qp.doesMatch("bbb"));
		assertTrue(qp.doesMatch("bbc"));
		assertTrue(qp.doesMatch("bbbb"));
		
	}
	
	@Test public void getComparatorTest(){
		QueryProperty qp = new QueryMoreThenProperty<Integer>("property1", 100);
		assertEquals(">", qp.getComparator().trim());
		qp = new QueryMoreThenProperty<Integer>("property1", 100, true);
		assertEquals(">=", qp.getComparator().trim());
	}

}
