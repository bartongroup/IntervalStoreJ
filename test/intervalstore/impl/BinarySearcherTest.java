package intervalstore.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

public class BinarySearcherTest
{
  /**
   * Tests for the binary search for the first list element that satisfies a
   * given test
   */
  @Test(groups = "Functional")
  public void testFindFirst()
  {
    List<String> names = Arrays.asList("anne", "anne", "bob", "colin",
            "sally");
    // first value following "anne"
    assertEquals(BinarySearcher.findFirst(names,
            name -> "anne".compareTo(name) < 0), 2);
    // first value equal to or following "bob"
    assertEquals(BinarySearcher.findFirst(names,
            name -> "bob".compareTo(name) <= 0), 2);
    // first value following "bob"
    assertEquals(BinarySearcher.findFirst(names,
            name -> "bob".compareTo(name) < 0), 3);
    // first value following "sally"
    assertEquals(BinarySearcher.findFirst(names,
            name -> "sally".compareTo(name) < 0), names.size());

    /*
     * 'test' including as a warning that this method should not be
     * used to find an entry - see the Javadoc for details!
     */
    int pos = BinarySearcher.findFirst(names, name -> "anne".equals(name));
    assertNotEquals(pos, 0);
    assertEquals(pos, names.size());

    List<Integer> nums = Arrays.asList(1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 3, 3, 3, 3);
    // no value is larger than 3
    assertEquals(BinarySearcher.findFirst(nums, num -> num > 3),
            nums.size());
    // entry 14 is first larger than 2
    assertEquals(BinarySearcher.findFirst(nums, num -> num > 2), 14);
    // entry 5 is first larger than 1
    assertEquals(BinarySearcher.findFirst(nums, num -> num > 1), 5);
    // entry 0 is first larger than 0
    assertEquals(BinarySearcher.findFirst(nums, num -> num > 0), 0);

    /*
     * 'test' included as a warning that Collections.binarySearch is not
     * guaranteed to find the first matching entry, so is not an 
     * alternative to findFirst()
     */
    pos = Collections.binarySearch(nums, 2);
    assertNotEquals(pos, 5);
    assertEquals(pos, 8);
  }
}
