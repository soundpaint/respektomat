/*
 * @(#)StringDistance.java 1.00 19/11/17
 *
 * Copyright (C) 2019 Jürgen Reuter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.soundpaint.respektomat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class StringDistance
{
  private static String normAndSort(final String x)
  {
    final char[] chars = x.toLowerCase().toCharArray();
    Arrays.sort(chars);
    return new String(chars);
  }

  public static double computeSimilarity(final String x, final String y)
  {
    if (x == null) throw new NullPointerException("x");
    if (y == null) throw new NullPointerException("y");
    final int minLength = x.length() < y.length() ? x.length() : y.length();
    final int maxLength = x.length() > y.length() ? x.length() : y.length();
    final String xN = normAndSort(x);
    final String yN = normAndSort(y);
    int xNPos = 0;
    int yNPos = 0;
    int scoreN = 0;
    int lastXMatchPos = 0;
    int lastYMatchPos = 0;
    int totalShiftScore = 0;
    final StringBuilder commonN = new StringBuilder();
    while ((xNPos < xN.length()) && (yNPos < yN.length())) {
      final char xNChar = xN.charAt(xNPos);
      final char yNChar = yN.charAt(yNPos);
      if (xNChar == yNChar) {
        final int shiftScore =
          (xNPos - lastXMatchPos) - (yNPos - lastYMatchPos);
        totalShiftScore += Math.abs(shiftScore);
        lastXMatchPos = xNPos;
        lastYMatchPos = yNPos;
        scoreN++;
        xNPos++;
        yNPos++;
        commonN.append(xNChar);
      } else if (xNChar < yNChar) {
        xNPos++;
      } else {
        yNPos++;
      }
    }
    final double score = scoreN * 2.0 / (minLength + maxLength) *
      (1.0 - totalShiftScore * 1.0 / maxLength);
    return score >= 0.5 ? score : 0.0;
  }

  private static void test(final String x, final String y)
  {
    final double similarity = computeSimilarity(x, y);
    System.out.println("x=" + x + ", y=" + y + ", similarity=" + similarity);
  }

  /**
   * For testing only.
   */
  public static void main(final String argv[])
  {
    test("abcdef", "abcdef");
    test("abcde", "abcdef");
    test("abcdef", "abcde");
    test("abcd", "abcdef");
    test("abcdef", "abcd");
    test("cdef", "abcdef");
    test("abcdef", "cdef");
    test("avwxyf", "uvwxyz");
    test("a", "u");
    test("ab", "uv");
    test("aaa", "uuu");
    test("abc", "uvw");
    test("def", "xyz");
    test("abc", "xyz");
    test("ace", "uwy");
    test("aye", "uwy");
    test("auw", "uwy");
    test("uwe", "uwy");
    test("abcd", "uvwx");
    test("abcde", "uvwxy");
    test("abcdef", "uvwxyz");
    test("Regierung", "Koalitionsregierung");
    test("Koalitionsregierung", "Regierung");
    test("Regierung", "Regierungskoalition");
    test("Regierungskoalition", "Regierung");
    test("Ministerin", "Ministerin");
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
