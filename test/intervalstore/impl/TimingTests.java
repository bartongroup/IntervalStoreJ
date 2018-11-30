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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A class with methods to inspect the performance and scalability of loading
 * and querying IntervalStore and NCList
 * 
 * @author gmcarstairs
 */
// this is a long running test so normally left disabled
// set enabled = true to run
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

  @BeforeClass
  public void setUp()
  {
    rand = new Random(RANDOM_SEED);
    System.out.println("Test\tsize\titeration\tms\tcount/ms");
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
        float ratio = elapsed == 0 ? 0 : count / (float) elapsed;
        if (i >= WARMUPS)
        {
          System.out.println(
                  String.format("%s\t%d\t%d\t%d\t%.1f", "NCList bulk load",
                          count, (i + 1 - WARMUPS), elapsed, ratio));
        }
        assertTrue(ncl.isValid());
      }
    }
  }

  /**
   * Generates a list of <code>count</code> intervals in the range [1, 4*count]
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
        float ratio = elapsed == 0 ? 0 : count / (float) elapsed;
        System.out.println(String.format("%s\t%d\t%d\t%d\t%.1f",
                "Naive bulk load", count, (i + 1), elapsed, ratio));
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
        float ratio = elapsed == 0 ? 0 : count / (float) elapsed;
        System.out.println(String.format("%s\t%d\t%d\t%d\t%.1f",
                "Naive no duplicates", count, (i + 1), elapsed, ratio));
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
      float ratio = elapsed == 0 ? 0 : count / (float) elapsed;
      System.out.println(String.format("%s\t%d\t%d\t%d\t%.1f",
              testName, count, (i + 1), elapsed, ratio));
      assertTrue(ncl.isValid());
    }
  }

  /**
   * Timing tests of querying an NCList for overlaps
   */
  public void testQueryTime_nclist()
  {
    int[] counts = { 10 * 1000, 100 * 1000, 200 * 1000, 300 * 1000,
        400 * 1000, 500 * 1000 };
    for (int count : counts)
    {
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
        float ratio = elapsed == 0 ? 0 : count / (float) elapsed;
        if (i >= WARMUPS)
        {
          System.out.println(
                  String.format("%s\t%d\t%d\t%d\t%.1f", "NCList query",
                          count, (i + 1 - WARMUPS), elapsed, ratio));
        }
        assertTrue(ncl.isValid());
      }
    }
  }

  /**
   * Timing tests of querying an NCList for overlaps
   */
  public void testQueryTime_naive()
  {
    for (int j = 1; j <= 5; j++)
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
        float ratio = elapsed == 0 ? 0 : count / (float) elapsed;
        System.out.println(String.format("%s\t%d\t%d\t%d\t%.1f",
                "Naive query", count, (i + 1), elapsed, ratio));
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
}
