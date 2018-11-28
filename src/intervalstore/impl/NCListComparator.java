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

import java.util.Comparator;

import intervalstore.api.IntervalI;

/**
 * A comparator that orders ranges by either start position or end position
 * ascending. If position matches, ordering is by length descending.
 * 
 * @author gmcarstairs
 */
public class NCListComparator implements Comparator<IntervalI>
{
  /**
   * A comparator that orders intervals by start position ascending, and within
   * that by length descending. This provides the canonical ordering of
   * intervals into subranges in order to build a nested containment list.
   */
  public static final Comparator<IntervalI> BY_START_POSITION = new NCListComparator(
          true);

  /**
   * A comparator that orders intervals by end position ascending, and within
   * that by length descending
   */
  public static final Comparator<IntervalI> BY_END_POSITION = new NCListComparator(
          false);

  boolean byStart;

  /**
   * Constructor
   * 
   * @param byStartPosition
   *          if true, order based on start position, if false by end position
   */
  NCListComparator(boolean byStartPosition)
  {
    byStart = byStartPosition;
  }

  @Override
  public int compare(IntervalI o1, IntervalI o2)
  {
    int len1 = o1.getEnd() - o1.getBegin();
    int len2 = o2.getEnd() - o2.getBegin();

    if (byStart)
    {
      return compare(o1.getBegin(), o2.getBegin(), len1, len2);
    }
    else
    {
      return compare(o1.getEnd(), o2.getEnd(), len1, len2);
    }
  }

  /**
   * Compares two ranges for ordering
   * 
   * @param pos1
   *          first range positional ordering criterion
   * @param pos2
   *          second range positional ordering criterion
   * @param len1
   *          first range length ordering criterion
   * @param len2
   *          second range length ordering criterion
   * @return
   */
  public int compare(long pos1, long pos2, int len1, int len2)
  {
    int order = Long.compare(pos1, pos2);
    if (order == 0)
    {
      /*
       * if tied on position order, longer length sorts to left
       * i.e. the negation of normal ordering by length
       */
      order = -Integer.compare(len1, len2);
    }
    return order;
  }
}
