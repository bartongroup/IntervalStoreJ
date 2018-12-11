/*
BSD 3-Clause License

Copyright (c) 2018, Geoff Barton's Computational Biology Group
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package intervalstore.impl;

import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A class with methods to inspect the performance and scalability of loading
 * and querying IntervalStore and NCList, and also a 'naive' (unordered) list
 * for comparison
 * <ul>
 * <li>Enable this test by setting @Test(enabled = true)</li>
 * <li>Run the class as TestNG test</li>
 * <li>Copy the data rows from console output</li>
 * <li>Paste into spreadsheet Timings.xlsx, columns A to D</li>
 * <li>- use 'Paste Special - Text' to paste tab-delimited values into
 * columns</li>
 * <li>other columns compute derived values from raw data</li>
 * <li>graphs select their data ranges based on the text values in column A, so
 * don't change these</li>
 * </ul>
 * 
 * @author gmcarstairs
 */
// this is a long running test so normally left disabled
@Test(enabled = false)
public class TimingTests
{
  /*
   * use a fixed random seed for repeatable tests
   */
  static final int RANDOM_SEED = 732;

  /*
   * repeat count for each test, to check consistency
   */
  static final int REPEATS = 10;

  /*
   * number of iterations to run before starting timings
   */
  static final int WARMUPS = 3;

  private Random rand;

  /*
   * output of averages of REPEAT iterations
   * for selected tests and sizes N;
   * printing these out together at the end just makes them
   * easier to select as graph ranges in Excel
   */
  StringBuilder averages;

  @BeforeClass
  public void setUp()
  {
    rand = new Random(RANDOM_SEED);
    averages = new StringBuilder(2345);
    System.out.println("Test\tsize\titeration\tms");
  }

  @AfterClass
  public void tearDown()
  {
    System.out.println(averages.toString());
  }

  /**
   * Timing tests of loading an NCList, with all intervals loaded in the
   * constructor
   */
  public void testLoadTime_nclist_bulkLoad()
  {
    for (int j = 1; j <= 5; j++)
    {
      int count = j * 100 * 1000; // 200K - 500K
      for (int i = 0; i < REPEATS + WARMUPS; i++)
      {
        List<Range> ranges = generateIntervals(count);
        long now = System.currentTimeMillis();
        NCList<Range> ncl = new NCList<>(ranges);
        long elapsed = System.currentTimeMillis() - now;
        if (i >= WARMUPS)
        {
          System.out.println(
                  String.format("%s\t%d\t%d\t%d", "NCList bulk load", count,
                          (i + 1 - WARMUPS), elapsed));
        }
        assertTrue(ncl.isValid());
      }
    }
  }

  /**
   * Generates a list of <code>count</code> intervals of length 50 in the range
   * [1, 4*count]
   * 
   * @param count
   * @return
   */
  protected List<Range> generateIntervals(Integer count)
  {
    int maxPos = 4 * count;
    List<Range> ranges = new ArrayList<>();
    for (int j = 0; j < count; j++)
    {
      int from = 1 + rand.nextInt(maxPos);
      int to = from + 50; // 1 + rand.nextInt(maxPos);
      ranges.add(new Range(Math.min(from, to), Math.max(from, to)));
    }
    return ranges;
  }

  /**
   * Timing tests of loading an NCList, with intervals loaded one at a time
   */
  public void testLoadTime_nclist_incremental()
  {
    for (int j = 1; j <= 5; j++)
    {
      int count = j * 100 * 1000; // 200K - 500K
      loadNclist(count, false, "NCList incr");
    }
  }

  /**
   * Timing tests of loading a simple list, with all intervals loaded in the
   * constructor
   */
  public void testLoadTime_naiveList_bulkLoad()
  {
    int[] counts = { 100 * 1000, 500 * 1000 };
    for (int count : counts)
    {
      for (int i = 0; i < REPEATS; i++)
      {
        List<Range> simple = new ArrayList<>();
        List<Range> ranges = generateIntervals(count);
        long now = System.currentTimeMillis();
        simple.addAll(ranges);
        long elapsed = System.currentTimeMillis() - now;
        System.out.println(String.format("%s\t%d\t%d\t%d",
                "Naive bulk load", count, (i + 1), elapsed));
      }
    }
  }

  /**
   * Timing tests of loading a simple list, with intervals loaded one at a time
   */
  public void testLoadTime_naiveList_noDuplicates()
  {
    int[] counts = { 10 * 1000, 100 * 1000, 200 * 1000 };
    for (int count : counts)
    {
      for (int i = 0; i < REPEATS; i++)
      {
        List<Range> simple = new ArrayList<>();
        List<Range> ranges = generateIntervals(count);
        long now = System.currentTimeMillis();
        for (Range r : ranges)
        {
          if (!simple.contains(r))
          {
            simple.addAll(ranges);
          }
        }
        long elapsed = System.currentTimeMillis() - now;
        System.out.println(String.format("%s\t%d\t%d\t%d",
                "Naive no duplicates", count, (i + 1), elapsed));
      }
    }
  }

  /**
   * Timing tests of loading an NCList, with intervals loaded one at a time, and
   * a check for duplicates before adding each interval
   */
  public void testLoadTime_nclist_incrementalNoDuplicates()
  {
    for (int j = 1; j <= 5; j++)
    {
      int count = j * 100 * 1000; // 200K - 500K
      loadNclist(count, true, "NCList no duplicates");
    }
  }

  /**
   * Performs a number of repeats of a timing test which adds a number of
   * intervals one at a time to an NCList, optionally testing first for
   * duplicate (i.e. whether the list already contains the interval)
   * 
   * @param count
   * @param allowDuplicates
   * @param testName
   */
  private void loadNclist(Integer count, boolean allowDuplicates,
          String testName)
  {
    for (int i = 0; i < REPEATS; i++)
    {
      NCList<Range> ncl = new NCList<>();
      List<Range> ranges = generateIntervals(count);
      long now = System.currentTimeMillis();
      for (Range r : ranges)
      {
        if (allowDuplicates || !ncl.contains(r))
        {
          ncl.add(r);
        }
      }
      long elapsed = System.currentTimeMillis() - now;
      System.out.println(String.format("%s\t%d\t%d\t%d", testName, count,
              (i + 1), elapsed));
      assertTrue(ncl.isValid());
    }
  }

  /**
   * Timing tests of querying an NCList for overlaps
   */
  public void testQueryTime_nclist()
  {
    /*
     * below N=20K, measured time is <10ms so prone to noise
     */
    int[] thousands = { 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 300, 400,
        500 };
    for (int k : thousands)
    {
      int count = k * 1000;
      long total = 0;
      for (int i = 0; i < REPEATS + WARMUPS; i++)
      {
        List<Range> ranges = generateIntervals(count);
        NCList<Range> ncl = new NCList<>(ranges);

        List<Range> queries = generateIntervals(count);
        long now = System.currentTimeMillis();
        for (Range q : queries)
        {
          ncl.findOverlaps(q.getBegin(), q.getEnd());
        }
        long elapsed = System.currentTimeMillis() - now;
        if (i >= WARMUPS)
        {
          total += elapsed;
          System.out.println(
                  String.format("%s\t%d\t%d\t%d", "NCList query", count,
                          (i + 1 - WARMUPS), elapsed));
        }
        assertTrue(ncl.isValid());
      }
      averages.append(String.format("%s\t%d\t%d\t%.1f\n",
              "NCList query avg",
              count, 0, total / (float) REPEATS));
    }
  }

  /**
   * Timing tests of querying an NCList for overlaps
   */
  public void testQueryTime_naive()
  {
    for (int j = 2; j <= 6; j++)
    {
      int count = j * 10 * 1000; // 10K - 50K
      for (int i = 0; i < REPEATS; i++)
      {
        List<Range> ranges = generateIntervals(count);

        List<Range> queries = generateIntervals(count);
        long now = System.currentTimeMillis();
        for (Range q : queries)
        {
          findOverlaps(ranges, q);
        }
        long elapsed = System.currentTimeMillis() - now;
        System.out.println(String.format("%s\t%d\t%d\t%d", "Naive query",
                count, (i + 1), elapsed));
      }
    }
  }

  /**
   * 'Naive' exhaustive search of an list of intervals for overlaps
   * 
   * @param ranges
   * @param begin
   * @param end
   */
  private List<Range> findOverlaps(List<Range> ranges, Range query)
  {
    List<Range> result = new ArrayList<>();
    for (Range r : ranges)
    {
      if (r.overlapsInterval(query))
      {
        result.add(r);
      }
    }
    return result;
  }

  /**
   * Performs a number of repeats of a timing test which adds a number of
   * intervals one at a time to an IntervalStore, optionally testing first for
   * duplicate (i.e. whether the list already contains the interval)
   * 
   * @param count
   * @param allowDuplicates
   * @param testName
   */
  private void loadIntervalStore(Integer count, boolean allowDuplicates,
          String testName)
  {
    for (int i = 0; i < REPEATS; i++)
    {
      IntervalStore<Range> ncl = new IntervalStore<>();
      List<Range> ranges = generateIntervals(count);
      long now = System.currentTimeMillis();
      for (Range r : ranges)
      {
        if (allowDuplicates || !ncl.contains(r))
        {
          ncl.add(r);
        }
      }
      long elapsed = System.currentTimeMillis() - now;
      System.out.println(String.format("%s\t%d\t%d\t%d", testName, count,
              (i + 1), elapsed));
      assertTrue(ncl.isValid());
    }
  }

  /**
   * Timing tests of loading an IntervalStore, with all intervals loaded in the
   * constructor
   */
  public void testLoadTime_intervalstore_bulkLoad()
  {
    for (int j = 1; j <= 5; j++)
    {
      int count = j * 100 * 1000; // 200K - 500K
      for (int i = 0; i < REPEATS + WARMUPS; i++)
      {
        List<Range> ranges = generateIntervals(count);
        long now = System.currentTimeMillis();
        IntervalStore<Range> ncl = new IntervalStore<>(ranges);
        long elapsed = System.currentTimeMillis() - now;
        if (i >= WARMUPS)
        {
          System.out.println(
                  String.format("%s\t%d\t%d\t%d",
                          "IntervalStore bulk load",
                          count, (i + 1 - WARMUPS), elapsed));
        }
        assertTrue(ncl.isValid());
      }
    }
  }

  /**
   * Timing tests of loading an IntervalStore, with intervals loaded one at a
   * time
   */
  public void testLoadTime_intervalstore_incremental()
  {
    for (int j = 1; j <= 5; j++)
    {
      int count = j * 100 * 1000; // 200K - 500K
      loadIntervalStore(count, false, "IntervalStore incr");
    }
  }

  /**
   * Timing tests of loading an IntervalStore, with intervals loaded one at a
   * time, and a check for duplicates before adding each interval
   */
  public void testLoadTime_intervalstore_incrementalNoDuplicates()
  {
    for (int j = 1; j <= 5; j++)
    {
      int count = j * 100 * 1000; // 200K - 500K
      loadIntervalStore(count, true, "IntervalStore no duplicates");
    }
  }

  /**
   * Timing tests of querying an IntervalStore for overlaps
   */
  public void testQueryTime_intervalstore()
  {
    for (int k = 2; k <= 10; k++)
    {
      /*
       * N is 200K, ... 1000K
       */
      int count = k * 100 * 1000;
      long total = 0L;
      for (int i = 0; i < REPEATS + WARMUPS; i++)
      {
        List<Range> ranges = generateIntervals(count);
        IntervalStore<Range> ncl = new IntervalStore<>(ranges);
  
        List<Range> queries = generateIntervals(count);
        long now = System.currentTimeMillis();
        for (Range q : queries)
        {
          ncl.findOverlaps(q.getBegin(), q.getEnd());
        }
        long elapsed = System.currentTimeMillis() - now;
        if (i >= WARMUPS)
        {
          total += elapsed;
          System.out.println(
                  String.format("%s\t%d\t%d\t%d", "IntervalStore query",
                          count, (i + 1 - WARMUPS), elapsed));
        }
        assertTrue(ncl.isValid());
      }
      averages.append(
              String.format("%s\t%d\t%d\t%.1f\n", "IntervalStore query avg",
                      count, 0, total / (float) REPEATS));
    }
  }

  /**
   * Timing tests for deleting from an IntervalStore
   */
  public void testRemoveTime_intervalstore()
  {
    /*
     * time deleting 1000 entries from stores of various sizes N
     */
    final int deleteCount = 1000;
    for (int k = 2; k <= 10; k++)
    {
      /*
       * N = 200K, ..., 1000K
       */
      int count = k * 100 * 1000;
      long total = 0L;
      for (int i = 0; i < REPEATS + WARMUPS; i++)
      {
        List<Range> ranges = generateIntervals(count);
        IntervalStore<Range> ncl = new IntervalStore<>(ranges);
  
        /*
         * remove intervals picked pseudo-randomly; attempts to remove the
         * same interval may fail but that doesn't affect the test timings
         */
        long now = System.currentTimeMillis();
        for (int j = 0; j < deleteCount; j++)
        {
          Range toDelete = ranges.get(this.rand.nextInt(count));
          ncl.remove(toDelete);
        }
        long elapsed = System.currentTimeMillis() - now;
        if (i >= WARMUPS)
        {
          total += elapsed;
          System.out.println(
                  String.format("%s\t%d\t%d\t%d", "IntervalStore remove",
                          count, (i + 1 - WARMUPS), elapsed));
        }
        assertTrue(ncl.isValid());
      }
      averages.append(
              String.format("%s\t%d\t%d\t%.1f\n",
                      "IntervalStore remove avg",
                      count, 0, total / (float) REPEATS));
    }
  }
}
