package net.anotheria.asg.util.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.anotheria.asg.data.DataObject;
import net.anotheria.asg.data.ObjectInfo;
import net.anotheria.util.crypt.MD5Util;
import net.anotheria.util.xml.XMLNode;

import org.junit.Before;
import org.junit.Test;

public class DataObjectUtilsTest {
	
	private static class TestDataObject implements DataObject{
		static final String PROP_PROPERTY1 = "property1";
		static final String PROP_PROPERTY2 = "property2";
		static final String PROP_PROPERTY3 = "property3";
		String id;
		String property1;
		String property2;
		String property3;
		
		public TestDataObject(String anId) {
			id = anId;
		}
		
		@Override
		public String getDefinedName() {
			return "TestDataObject";
		}

		@Override
		public String getFootprint() {
			StringBuilder footprint = new StringBuilder();
			footprint.append(getProperty1());
			footprint.append(getProperty2());
			footprint.append(getProperty3());
			return MD5Util.getMD5Hash(footprint);
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public long getLastUpdateTimestamp() {
			return 0;
		}

		@Override
		public ObjectInfo getObjectInfo() {
			throw new UnsupportedOperationException("Implement me please!");
		}

		@Override
		public Object getPropertyValue(String propertyName) {
			if (PROP_PROPERTY1.equals(propertyName))
				return getProperty1();
			if (PROP_PROPERTY2.equals(propertyName))
				return getProperty2();
			if (PROP_PROPERTY3.equals(propertyName))
				return getProperty3();			
			throw new RuntimeException("No property getter for "+propertyName);
		}

		@Override
		public XMLNode toXMLNode() {
			throw new UnsupportedOperationException("Implement me please!");
		}

		public String getProperty1() {
			return property1;
		}

		public void setProperty1(String property1) {
			this.property1 = property1;
		}

		public String getProperty2() {
			return property2;
		}

		public void setProperty2(String property2) {
			this.property2 = property2;
		}

		public String getProperty3() {
			return property3;
		}

//		public void setProperty3(String property3) {
//			this.property3 = property3;
//		}
//
//		public void setId(String id) {
//			this.id = id;
//		}
		
		@Override
		public Object clone() throws CloneNotSupportedException{
			throw new CloneNotSupportedException();
		}

		@Override
		public String getDefinedParentName() {
			throw new UnsupportedOperationException("Implement me please!");
		}
		
	}
	
	private List<TestDataObject> testData;
	@Before public void initTestData(){		
		System.out.println("Initing test data");
		testData = new ArrayList<TestDataObject>();
		for(int i = 0; i < 1000; i++){
			TestDataObject dataObject = new TestDataObject(i + "");
			dataObject.setProperty1(i + "");
			dataObject.setProperty2((i % 100) + "");
			testData.add(dataObject);
		}
	}
	
	@Test public void createMapByIdTest(){
		Map<String,TestDataObject> resultMap = DataObjectUtils.createMapById(testData);
		assertTrue("Wrong result Map by Id size: ", 1000 == resultMap.size());
		for(TestDataObject d: testData)
			assertEquals("Wrong mapping. ",d, resultMap.get(d.getId()));
	}
	
	@Test public void createMapByKeyPropertyTest(){
		Map<String,TestDataObject> resultMap = DataObjectUtils.createMapByKeyProperty(TestDataObject.PROP_PROPERTY1, String.class, testData);
//		assertEquals("Wrong result map size: ",testData.size(), resultMap.size());
		assertTrue("Wrong result Map by Property1 size: ", 1000 == resultMap.size());
		for(TestDataObject d: testData)
			assertEquals("Wrong mapping. ",d, resultMap.get(d.getProperty1()));
		
	}
	
	@Test public void createMapByPropertyTest(){
		Map<String,TestDataObject[]> resultMap = DataObjectUtils.createMapByProperty(TestDataObject.PROP_PROPERTY1, String.class, testData);
//		assertEquals("Wrong result map size: ",testData.size(), resultMap.size());
		assertTrue("Wrong result Map by Property1 size: ", 1000 == resultMap.size());
		for(TestDataObject d: testData){
			TestDataObject[] fromMap = resultMap.get(d.getProperty1());
			assertTrue(fromMap.length == 1);
			assertEquals("Wrong mapping. ",d, resultMap.get(d.getProperty1())[0]);
		}
		
		
		resultMap = DataObjectUtils.createMapByProperty(TestDataObject.PROP_PROPERTY2, String.class, testData);
		assertTrue("Wrong result Map by Property2 size: ", 100 == resultMap.size());
		for(TestDataObject d: testData){
			TestDataObject[] dataFromMap = resultMap.get(d.getProperty2());
			assertTrue(dataFromMap.length == 10);
			for(TestDataObject dFromMap:dataFromMap)
			assertEquals("Wrong mapping. ",d.getProperty2(), dFromMap.getProperty2());
		}
	}
	
}
