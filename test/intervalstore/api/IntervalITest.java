/*
BSD 3-Clause License

Copyright (c) 2018, Mungo Carstairs
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
package intervalstore.api;

import static org.testng.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import intervalstore.impl.SimpleFeature;

/**
 * Test for the static utility sort method of interface IntervalI
 * 
 * @author gmcarstairs
 */
public class IntervalITest
{
  @Test(groups = "Functional")
  public void testSortIntervals()
  {
    List<SimpleFeature> sfs = new ArrayList<>();
    SimpleFeature sf1 = new SimpleFeature(30, 80, "Pfam");
    sfs.add(sf1);
    SimpleFeature sf2 = new SimpleFeature(40, 50, "Metal");
    sfs.add(sf2);
    SimpleFeature sf3 = new SimpleFeature(50, 60, "Helix");
    sfs.add(sf3);
  
    // sort by end position descending
    IntervalI.sortIntervals(sfs, false);
    assertSame(sfs.get(0), sf1);
    assertSame(sfs.get(1), sf3);
    assertSame(sfs.get(2), sf2);
  
    // sort by start position ascending
    IntervalI.sortIntervals(sfs, true);
    assertSame(sfs.get(0), sf1);
    assertSame(sfs.get(1), sf2);
    assertSame(sfs.get(2), sf3);
  }
}
