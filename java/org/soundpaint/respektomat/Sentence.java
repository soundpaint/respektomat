/*
 * @(#)Sentence.java 1.00 19/11/17
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

import java.util.List;
import java.util.ArrayList;

public class Sentence implements Comparable<Sentence>
{
  private static int idCount = 0;

  private int id;
  private List<Token> tokens;
  private boolean sealed;

  public static final Sentence EOF = new Sentence().seal();

  public Sentence()
  {
    id = idCount++;
    tokens = new ArrayList<Token>();
    sealed = false;
  }

  public void addToken(final Token token)
  {
    if (!sealed) {
      tokens.add(token);
    } else {
      throw new IllegalStateException("can not add token to sealed sentence");
    }
  }

  public Sentence seal()
  {
    if (sealed) {
      throw new IllegalStateException("sentence already sealed");
    }
    sealed = true;
    return this;
  }

  public boolean isSealed()
  {
    return sealed;
  }

  public Iterable<Token> getTokens()
  {
    return tokens;
  }

  @Override
  public int compareTo(final Sentence other)
  {
    final int otherId = other.id;
    return id < otherId ? -1 : (id > otherId ? 1 : 0);
  }

  private String toString(final boolean debug)
  {
    final StringBuffer sb = new StringBuffer();
    Token prevToken = null;
    for (final Token token : tokens) {
      if (prevToken != null) {
        if (((prevToken.getType() == Token.Type.WORD) && token.getSpacerLeft()) ||
            ((token.getType() == Token.Type.WORD) && prevToken.getSpacerRight())) {
          sb.append(" ");
        }
      }
      sb.append(token.getValue());
      prevToken = token;
    }
    if (debug) {
      if (sealed) {
        sb.append(" [sealed]");
      } else {
        sb.append(" [mutable]");
      }
    }
    return sb.toString();
  }

  public String prettyPrint()
  {
    return toString(false);
  }

  @Override
  public String toString()
  {
    return toString(true);
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
