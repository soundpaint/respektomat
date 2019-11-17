/*
 * @(#)SentenceFilter.java 1.00 19/11/17
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

import java.text.ParseException;

public class SentenceFilter
{
  private final Token token;

  private SentenceFilter()
  {
    throw new UnsupportedOperationException("unsupported default constructor");
  }

  public SentenceFilter(final String token) throws ParseException
  {
    this(Token.createWord(token));
  }

  public SentenceFilter(final Token token)
  {
    if (token == null) {
      throw new NullPointerException("token");
    }
    if (Config.DEBUG) {
      System.out.println("setting up sentence filter for token " + token);
    }
    this.token = token;
  }

  public boolean accept(final Sentence sentence)
  {
    for (final Token token : sentence.getTokens()) {
      if (token.equals(this.token)) {
        return true;
      } else if (Config.DEBUG) {
        System.out.println(token + " != " + this.token);
      }
    }
    return false;
  }

  @Override
  public String toString()
  {
    return "SentenceFilter[token=" + token + "]";
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
