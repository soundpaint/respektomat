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

    /*
    int xPos = 0;
    int yPos = 0;
    int score = 0;
    while ((xPos < x.length()) && (yPos < y.length())) {
      final char xChar = x.charAt(xPos);
      final char yChar = y.charAt(yPos);
      if (xChar == yChar) {
        score++;
        xPos++;
        yPos++;
      } else if (xChar < yChar) {
        xPos++;
      } else {
        yPos++;
      }
    }
    */

    final String xN = normAndSort(x);
    final String yN = normAndSort(y);
    int xNPos = 0;
    int yNPos = 0;
    int scoreN = 0;
    final StringBuilder commonN = new StringBuilder();
    while ((xNPos < xN.length()) && (yNPos < yN.length())) {
      final char xNChar = xN.charAt(xNPos);
      final char yNChar = yN.charAt(yNPos);
      if (xNChar == yNChar) {
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
    final int minLength = x.length() < y.length() ? x.length() : y.length();
    final int maxLength = x.length() > y.length() ? x.length() : y.length();

    if (Config.DEBUG) {
      System.out.println(minLength * maxLength * scoreN + ": " + x + " | " + y);
      System.out.println("common: " + commonN);
    }

    /*
     * TODO: Idea: Track together with commonN which positions (for
     * strings x and y, respectively) these characters originate from,
     * e.g.  for x="Koalitionsregierung", y="Regierung",
     * commonN="eegginrru", and compute position deltas δ:
     *
     * e -> x[11], y[1]
     * δ:     +3    +3    (ok)
     * e -> x[14], y[4]
     * δ:     -2    -2    (ok)
     * g -> x[12], y[2]
     * δ:     +6    +6    (ok)
     * g -> x[18], y[8]
     * δ:    -14    -5    (not ok)
     * i ->  x[4], y[3]
     * δ:     +4    +4    (ok)
     * n ->  x[8], y[7]
     * δ:     +2    -7    (not ok)
     * r -> x[10], y[0]
     * δ:     +5    +5    (ok)
     * r -> x[15], y[5]
     * δ:     +1    +1    (ok)
     * u -> x[16], y[6]
     *
     * => deltas differ only in 2 of 8 places
     * => assume (at least) 6 chars match in the correct order
     * => consider this value as score
     */
    return minLength * maxLength * scoreN;
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
