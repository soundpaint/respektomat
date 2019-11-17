/*
 * @(#)History.java 1.00 19/11/17
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

import java.util.LinkedList;

public class History
{
  private static final int MAX_LENGTH = 100;

  private LinkedList<Sentence> sentences;

  public History()
  {
    sentences = new LinkedList<Sentence>();
  }

  public void add(final Sentence sentence)
  {
    if (sentences.contains(sentence)) {
      sentences.remove(sentence);
    }
    sentences.addFirst(sentence);
  }

  public double getScore(final Sentence sentence)
  {
    final int index = sentences.indexOf(sentence);
    if (index == -1) {
      return 0.0;
    }
    return -1000.0 * (MAX_LENGTH - index);
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
