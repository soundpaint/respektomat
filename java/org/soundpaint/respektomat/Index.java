/*
 * @(#)Index.java 1.00 19/11/17
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

public class Index
{
  private Map<Token, IndexForToken> subIndices;

  public Index()
  {
    subIndices = new TreeMap<Token, IndexForToken>();
  }

  private IndexForTokenInSentence addLocation(final Token token,
                                              final Sentence sentence,
                                              final int index)
  {
    final IndexForToken indexForToken;
    if (subIndices.containsKey(token)) {
      indexForToken = subIndices.get(token);
    } else {
      indexForToken = new IndexForToken(token);
      subIndices.put(token, indexForToken);
    }
    return indexForToken.addLocation(sentence, index);
  }

  public void addSentence(final Sentence sentence)
  {
    if (sentence == null) {
      throw new NullPointerException("sentence");
    }
    if (Config.DEBUG) {
      System.out.println("add sentence: " + sentence);
    }
    int index = 0;
    for (final Token token : sentence.getTokens()) {
      addLocation(token, sentence, index++);
    }
  }

  public Iterable<Token> getTokens()
  {
    return subIndices.keySet();
  }

  public IndexForToken getIndexForToken(final Token token)
  {
    return subIndices.get(token);
  }

  private static final String NL = System.lineSeparator();

  public String createSummary()
  {
    final StringBuilder sb = new StringBuilder();
    sb.append("#tokens: " + subIndices.size() + NL);
    for (final Token token : subIndices.keySet()) {
      final IndexForToken indexForToken = subIndices.get(token);
      int occursInSentences = 0;
      int occursAltogether = 0;
      for (final IndexForTokenInSentence indexForTokenInSentence :
             indexForToken.getIndexForTokenInSentence()) {
        occursInSentences++;
        occursAltogether += indexForTokenInSentence.count();
      }
      sb.append("Token: " + token + ": #" + occursAltogether +
                " in #" + occursInSentences + " sentences" + NL);
    }
    return sb.toString();
  }

  @Override
  public String toString()
  {
    return "Index[subIndices=" + subIndices + "]";
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
