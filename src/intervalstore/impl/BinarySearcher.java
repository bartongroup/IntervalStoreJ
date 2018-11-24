package intervalstore.impl;

import java.util.List;
import java.util.function.Function;

/**
 * Provides a method to perform binary search of an ordered list for the first
 * entry that satisfies a supplied condition
 * 
 * @author gmcarstairs
 */
public class BinarySearcher
{
  /**
   * Performs a binary search of the list to find the index of the first entry
   * for which the test returns true. Answers the length of the list if there is
   * no such entry.
   * <p>
   * For correct behaviour, the provided list must be ordered consistent with
   * the test, that is, any entries returning false must precede any entries
   * returning true. Note that this means that this method is <em>not</em>
   * usable to search for equality (to find a specific entry), as all unequal
   * entries will answer false to the test. To do that, use
   * <code>Collections.binarySearch</code> instead.
   * 
   * @param list
   * @param test
   * @return
   * @see java.util.Collections#binarySearch(List, Object)
   */
  public static <T> int findFirst(List<? extends T> list,
          Function<T, Boolean> test)
  {
    int start = 0;
    int end = list.size() - 1;
    int matched = list.size();
  
    while (start <= end)
    {
      int mid = (start + end) / 2;
      T entry = list.get(mid);
      boolean compare = test.apply(entry);
      if (compare)
      {
        matched = mid;
        end = mid - 1;
      }
      else
      {
        start = mid + 1;
      }
    }
  
    return matched;
  }
}
