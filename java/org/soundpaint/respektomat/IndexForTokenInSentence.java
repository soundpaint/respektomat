/*
 * @(#)IndexForTokenInSentence.java 1.00 19/11/17
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

import java.util.TreeSet;

public class IndexForTokenInSentence
{
  private final Token token;
  private final Sentence sentence;
  private final TreeSet<Integer> indices;

  private IndexForTokenInSentence()
  {
    throw new UnsupportedOperationException("unsupported default constructor");
  }

  public IndexForTokenInSentence(final Token token, final Sentence sentence)
  {
    if (!sentence.isSealed()) {
      throw new IllegalArgumentException("can not index mutable sentence");
    }
    this.token = token;
    this.sentence = sentence;
    indices = new TreeSet<Integer>();
  }

  public Token getToken()
  {
    return token;
  }

  public Sentence getSentence()
  {
    return sentence;
  }

  public void addIndex(final int index)
  {
    indices.add(index);
  }

  public boolean hasIndex(final int index)
  {
    return indices.contains(index);
  }

  public Iterable<Integer> getIndices()
  {
    return indices;
  }

  public int count() {
    return indices.size();
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
