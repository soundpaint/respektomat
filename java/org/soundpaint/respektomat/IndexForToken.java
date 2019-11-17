/*
 * @(#)IndexForToken.java 1.00 19/11/17
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

import java.util.Map;
import java.util.TreeMap;

public class IndexForToken
{
  private final Token token;
  private final Map<Sentence, IndexForTokenInSentence> subIndices;

  private IndexForToken()
  {
    throw new UnsupportedOperationException("unsupported default constructor");
  }

  public IndexForToken(final Token token)
  {
    this.token = token;
    subIndices = new TreeMap<Sentence, IndexForTokenInSentence>();
  }

  public Token getToken()
  {
    return token;
  }

  public IndexForTokenInSentence addLocation(final Sentence sentence,
                                             final int index)
  {
    final IndexForTokenInSentence indexForTokenInSentence;
    if (subIndices.containsKey(sentence)) {
      indexForTokenInSentence = subIndices.get(sentence);
    } else {
      indexForTokenInSentence = new IndexForTokenInSentence(token, sentence);
      subIndices.put(sentence, indexForTokenInSentence);
    }
    indexForTokenInSentence.addIndex(index);
    return indexForTokenInSentence;
  }

  public IndexForTokenInSentence
    getIndexForTokenInSentence(final Sentence sentence)
  {
    return subIndices.get(sentence);
  }

  public Iterable<IndexForTokenInSentence> getIndexForTokenInSentence()
  {
    return subIndices.values();
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
