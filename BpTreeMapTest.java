import static java.lang.System.out;
import static org.junit.Assert.*;

import java.util.SortedMap;
import java.util.function.Predicate;

import junit.framework.TestCase;




/**
 * This class provides tests for the select(Predicate <Comparable []> predicate)
 * and select (KeyType keyVal) methods from the table class
 */
public class BpTreeMapTest extends TestCase 
{
	BpTreeMap <Integer, Integer> upTree = new BpTreeMap <> (Integer.class, Integer.class);
	BpTreeMap <Integer, Integer> downTree = new BpTreeMap <> (Integer.class, Integer.class);
	BpTreeMap <Integer, Integer> splitReady = new BpTreeMap <> (Integer.class, Integer.class);
	/**
	 * Sets up both tables prior to the running of each test
	 */
	public void setUp() 
	{
		int totKeys = 20;
        for (int i = 1; i < totKeys; i += 2) { upTree.put (i, i * i);} // for
        for (int i = totKeys; i > 1; i -= 2) { downTree.put (i, i * i);} // for	
        splitReady.put(1,1); splitReady.put(2,2); splitReady.put(5,5); splitReady.put(4,4);
	}

	
	
	/**
	 * Tests entrySet function on an ascending inserted BpTreeMap
	 */
	public void testEntrySet1() 
	{
		String entrySetString = upTree.entrySet().toString();
		System.out.println(entrySetString);
		String testString = "[1=1, 3=9, 5=25, 7=49, 9=81, 11=121, 13=169, 15=225, 17=289, 19=361]";
		System.out.println(testString.equals(entrySetString));
		assertEquals("Test entry set 1", testString.equals(entrySetString), true);
	}
	
	/**
	 * Tests entrySet function on an descending inserted BpTreeMap
	 */
	public void testEntrySet2() 
	{
		String entrySetString = downTree.entrySet().toString();
		System.out.println(entrySetString);
		String testString = "[2=4, 4=16, 6=36, 8=64, 10=100, 12=144, 14=196, 16=256, 18=324, 20=400]";
		System.out.println(testString.equals(entrySetString));
		assertEquals("Test entry set 2", testString.equals(entrySetString), true);
	}
	
	/**
	 * Tests the size function of the BpTreeMap
	 */
	public void testSize1() 
	{
		int size = upTree.size();
		assertEquals("Test size 1", size, 10);
	}
	
	/**
	 * Tests the size function of the BpTreeMap after inserting another node into tree
	 */
	public void testSize2() 
	{
		upTree.put(50, 1000);
		int size = upTree.size();
		assertEquals("Test size 2", size, 11);
	}
	
	/**
	 * Tests if tailMap returns range firstKey to toKey
	 */
	public void testHeadMap()
	{
		SortedMap<Integer, Integer> SubMapTest = (SortedMap<Integer, Integer>) upTree.headMap(12);
      	String  st = SubMapTest.toString();
      	String testString = "{1=1, 3=9, 5=25, 7=49, 9=81, 11=121}";
      	out.println(st);
      	assertEquals("Test headMap", testString, st);
	}
	
	/**
	 * Tests if tailMap returns range fromKey to lastKey
	 */
	public void testTailMap()
	{
		SortedMap<Integer, Integer> SubMapTest = (SortedMap<Integer, Integer>) upTree.tailMap(12);
      	String  st = SubMapTest.toString();
      	String testString = "{13=169, 15=225, 17=289, 19=361}";
      	out.println(st);
      	assertEquals("Test tailMap", testString, st);
	}
	
	/**
	 * Tests if subMap returns correct range of keys and refs
	 */
	public void testSubMap()
	{
		SortedMap<Integer, Integer> SubMapTest = (SortedMap<Integer, Integer>) upTree.subMap(5, 13);
      	String  st = SubMapTest.toString();
      	String testString = "{5=25, 7=49, 9=81, 11=121, 13=169}";
      	out.println(st);
      	assertEquals("Test subMap", testString, st);
	}
	
	/**
	 * Tests the first key function
	 */
	public void testFirstKey()
	{
		int firstKey = upTree.firstKey();
		assertEquals("Test firstKey", firstKey, 1);
	}
	/**
	 * Tests the last key function
	 */
	public void testLastKey()
	{
		int lastKey = upTree.lastKey();
		assertEquals("Test lastKey", lastKey, 19);
	}
	
	
}
