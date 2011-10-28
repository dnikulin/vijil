// Copyright (c) 2009, incava.org
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//     * Redistributions of source code must retain the above copyright notice,
//     * this list of conditions and the following disclaimer.
//
//     * Redistributions in binary form must reproduce the above copyright notice,
//     * this list of conditions and the following disclaimer in the documentation
//     * and/or other materials provided with the distribution.
//
//     * Neither the name of incava.org nor the names of its contributors may be
//     * used to endorse or promote products derived from this software without
//     * specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

// Copyright (C) 2011  Dmitri Nikulin
//
// This file is part of Vijil.
//
// Vijil is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Vijil is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Vijil.  If not, see <http://www.gnu.org/licenses/>.
//
// Repository:     https://github.com/dnikulin/vijil
// Email:          dnikulin+vijil@gmail.com

package org.incava.util.diff;

import java.util.*;

import com.dnikulin.vijil.tools.Integers;

class Link {
    public final Link next;
    public final int i;
    public final int j;

    public Link(Link next, int i, int j) {
        this.next = next;
        this.i = i;
        this.j = j;
    }
}

/**
 * Compares two lists, returning a list of the additions, changes, and deletions
 * between them. A <code>Comparator</code> may be passed as an argument to the
 * constructor, and will thus be used. If not provided, the initial value in the
 * <code>a</code> ("from") list will be looked at to see if it supports the
 * <code>Comparable</code> interface. If so, its <code>equals</code> and
 * <code>compareTo</code> methods will be invoked on the instances in the "from"
 * and "to" lists; otherwise, for speed, hash codes from the objects will be
 * used instead for comparison.
 *
 * <p>The file FileDiff.java shows an example usage of this class, in an
 * application similar to the Unix "diff" program.</p>
 */
public class Diff<Type>
{
    /**
     * The source list, AKA the "from" values.
     */
    protected Type[] a;

    /**
     * The target list, AKA the "to" values.
     */
    protected Type[] b;

    /**
     * The list of differences, as <code>Difference</code> instances.
     */
    protected List<Difference> diffs = new ArrayList<Difference>();

    /**
     * The pending, uncommitted difference.
     */
    private Difference pending;

    /**
     * The comparator used, if any.
     */
    private Comparator<Type> comparator;

    /**
     * The thresholds.
     */
    private int[] thresh;
    private int lastKey;

    /**
     * Constructs the Diff object for the two arrays, using the default
     * comparison mechanism between the objects, such as <code>equals</code> and
     * <code>compareTo</code>.
     */
    public Diff(Type[] a, Type[] b)
    {
        this(a, b, null);
    }

    /**
     * Constructs the Diff object for the two lists, using the given comparator.
     */
    public Diff(Type[] a, Type[] b, Comparator<Type> comp)
    {
        this.a = a;
        this.b = b;
        this.comparator = comp;
        this.thresh = null;
        lastKey = -1;
    }

    /**
     * Runs diff and returns the results.
     */
    public List<Difference> diff()
    {
        traverseSequences();

        // add the last difference, if pending:
        if (pending != null) {
            diffs.add(pending);
        }

        return diffs;
    }

    /**
     * Traverses the sequences, seeking the longest common subsequences,
     * invoking the methods <code>finishedA</code>, <code>finishedB</code>,
     * <code>onANotB</code>, and <code>onBNotA</code>.
     */
    protected void traverseSequences()
    {
        int[] matches = getLongestCommonSubsequences();

        int lastA = a.length - 1;
        int lastB = b.length - 1;
        int bi = 0;
        int ai;

        int lastMatch = matches.length - 1;

        for (ai = 0; ai <= lastMatch; ++ai) {
            int bLine = matches[ai];

            if (bLine < 0) {
                onANotB(ai, bi);
            }
            else {
                while (bi < bLine) {
                    onBNotA(ai, bi++);
                }

                onMatch(ai, bi++);
            }
        }

        boolean calledFinishA = false;
        boolean calledFinishB = false;

        while (ai <= lastA || bi <= lastB) {

            // last A?
            if (ai == lastA + 1 && bi <= lastB) {
                if (!calledFinishA && callFinishedA()) {
                    finishedA(lastA);
                    calledFinishA = true;
                }
                else {
                    while (bi <= lastB) {
                        onBNotA(ai, bi++);
                    }
                }
            }

            // last B?
            if (bi == lastB + 1 && ai <= lastA) {
                if (!calledFinishB && callFinishedB()) {
                    finishedB(lastB);
                    calledFinishB = true;
                }
                else {
                    while (ai <= lastA) {
                        onANotB(ai++, bi);
                    }
                }
            }

            if (ai <= lastA) {
                onANotB(ai++, bi);
            }

            if (bi <= lastB) {
                onBNotA(ai, bi++);
            }
        }
    }

    /**
     * Override and return true in order to have <code>finishedA</code> invoked
     * at the last element in the <code>a</code> array.
     */
    protected boolean callFinishedA()
    {
        return false;
    }

    /**
     * Override and return true in order to have <code>finishedB</code> invoked
     * at the last element in the <code>b</code> array.
     */
    protected boolean callFinishedB()
    {
        return false;
    }

    /**
     * Invoked at the last element in <code>a</code>, if
     * <code>callFinishedA</code> returns true.
     */
    protected void finishedA(int lastA)
    {
    }

    /**
     * Invoked at the last element in <code>b</code>, if
     * <code>callFinishedB</code> returns true.
     */
    protected void finishedB(int lastB)
    {
    }

    /**
     * Invoked for elements in <code>a</code> and not in <code>b</code>.
     */
    protected void onANotB(int ai, int bi)
    {
        if (pending == null) {
            pending = new Difference(ai, ai, bi, -1);
        }
        else {
            pending.setDeleted(ai);
        }
    }

    /**
     * Invoked for elements in <code>b</code> and not in <code>a</code>.
     */
    protected void onBNotA(int ai, int bi)
    {
        if (pending == null) {
            pending = new Difference(ai, -1, bi, bi);
        }
        else {
            pending.setAdded(bi);
        }
    }

    /**
     * Invoked for elements matching in <code>a</code> and <code>b</code>.
     */
    protected void onMatch(int ai, int bi)
    {
        if (pending == null) {
            // no current pending
        }
        else {
            diffs.add(pending);
            pending = null;
        }
    }

    /**
     * Compares the two objects, using the comparator provided with the
     * constructor, if any.
     */
    protected boolean equals(Type x, Type y)
    {
        return comparator == null ? x.equals(y) : comparator.compare(x, y) == 0;
    }

    /**
     * Returns an array of the longest common subsequences.
     */
    public int[] getLongestCommonSubsequences()
    {
        int aStart = 0;
        int aEnd = a.length - 1;

        int bStart = 0;
        int bEnd = b.length - 1;

        int[] matches = new int[Math.max(a.length, b.length)];
        thresh = new int[matches.length];

        Arrays.fill(matches, -1);
        Arrays.fill(thresh,  -1);

        int lastKeyMatches = -1;
        lastKey = -1;

        while (aStart <= aEnd && bStart <= bEnd && equals(a[aStart], b[bStart])) {
            lastKeyMatches = Math.max(lastKeyMatches, aStart);
            matches[aStart++] = bStart++;
        }

        while (aStart <= aEnd && bStart <= bEnd && equals(a[aEnd], b[bEnd])) {
            lastKeyMatches = Math.max(lastKeyMatches, aEnd);
            matches[aEnd--] = bEnd--;
        }

        HashMap<Type, Integers> bMatches = new HashMap<Type, Integers>();

        for (int bi = bStart; bi <= bEnd; ++bi) {
            Type          element   = b[bi];
            Type          key       = element;
            Integers      positions = bMatches.get(key);

            if (positions == null) {
                positions = new Integers();
                bMatches.put(key, positions);
            }

            positions.add(bi);
        }

        Link[] links = new Link[matches.length];

        for (int i = aStart; i <= aEnd; ++i) {
            Type     aElement  = a[i];
            Integers positions = bMatches.get(aElement);

            if (positions != null) {
                int  k   = 0;

                for (int n = positions.count - 1; n >= 0; n--) {
                    int j = positions.data[n];

                    k = insert(j, k);

                    if (k < 0) {
                        // nothing
                    }
                    else {
                        Link next = k > 0 ? links[k - 1] : null;
                        links[k] = new Link(next, i, j);
                    }
                }
            }
        }

        if (lastKey >= 0) {
            int      ti   = lastKey;
            Link     link = links[ti];
            while (link != null) {
                matches[link.i] = link.j;
                lastKeyMatches = Math.max(lastKeyMatches, link.i);
                link = link.next;
            }
        }

        int size = 1 + lastKeyMatches;
        return Arrays.copyOf(matches, size);
    }

    /**
     * Returns whether the integer is not zero (including if it is not null).
     */
    protected static boolean isNonzero(int i)
    {
        return i > 0;
    }

    /**
     * Returns whether the value in the map for the given index is greater than
     * the given value.
     */
    protected boolean isGreaterThan(int index, int val)
    {
        return (index >= 0) && (val >= 0) && (thresh[index] > val);
    }

    /**
     * Returns whether the value in the map for the given index is less than
     * the given value.
     */
    protected boolean isLessThan(int index, int val)
    {
        if (index < 0) return false;
        int lhs = thresh[index];
        return (lhs >= 0) && ((val < 0) || (lhs < val));
    }

    /**
     * Returns the value for the greatest key in the map.
     */
    protected int getLastValue()
    {
        return thresh[lastKey];
    }

    /**
     * Adds the given value to the "end" of the threshold map, that is, with the
     * greatest index/key.
     */
    protected void append(int value)
    {
        thresh[++lastKey] = value;
    }

    /**
     * Inserts the given values into the threshold map.
     */
    protected int insert(int j, int k)
    {
        if (isNonzero(k) && isGreaterThan(k, j) && isLessThan(k - 1, j)) {
            thresh[k] = j;
            lastKey(k);
        }
        else {
            int high = -1;

            if (isNonzero(k)) {
                high = k;
            }
            else if (lastKey >= 0) {
                high = lastKey;
            }

            // off the end?
            if ((high == -1) || (j > getLastValue())) {
                append(j);
                k = high + 1;
            }
            else {
                // binary search for insertion point:
                int low = 0;

                while (low <= high) {
                    int     index = (high + low) / 2;
                    int     val   = thresh[index];
                    int     cmp   = (j - val);

                    if (cmp == 0) {
                        return -1;
                    }
                    else if (cmp > 0) {
                        low = index + 1;
                    }
                    else {
                        high = index - 1;
                    }
                }

                thresh[low] = j;
                lastKey(low);

                k = low;
            }
        }

        return k;
    }

    private void lastKey(int now) {
        lastKey = Math.max(lastKey, now);
    }
}
