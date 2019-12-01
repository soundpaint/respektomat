/*
 * @(#)Token.java 1.00 19/11/17
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

public class Token implements Comparable<Token>
{
  public enum Type {
    EOF,
    APOSTROPHY,
    COMMA,
    SEMICOLON,
    COLON,
    DASH,
    SLASH,
    PERCENT,
    FULL_STOP,
    QUESTION_MARK,
    EXCLAMATION_MARK,
    ELLIPSIS,
    LEFT_PARENTHESIS,
    RIGHT_PARENTHESIS,
    LEFT_QUOTES,
    RIGHT_QUOTES,
    WORD
  };

  public static final Token EOF =
    new Token(Type.EOF, "", false, false);
  public static final Token APOSTROPHY =
    new Token(Type.APOSTROPHY, "’", false, false);
  public static final Token AMPERSAND =
    new Token(Type.APOSTROPHY, "&", true, true);
  public static final Token COMMA =
    new Token(Type.COMMA, ",", false, true);
  public static final Token SEMICOLON =
    new Token(Type.SEMICOLON, ";", false, true);
  public static final Token COLON =
    new Token(Type.COLON, ":", false, true);
  public static final Token DASH =
    new Token(Type.DASH, "–", true, true);
  public static final Token SLASH =
    new Token(Type.SLASH, "/", true, true);
  public static final Token PERCENT =
    new Token(Type.PERCENT, "%", true, true);
  public static final Token FULL_STOP =
    new Token(Type.FULL_STOP, ".", false, true);
  public static final Token QUESTION_MARK =
    new Token(Type.QUESTION_MARK, "?", false, true);
  public static final Token EXCLAMATION_MARK =
    new Token(Type.EXCLAMATION_MARK, "!", false, true);
  public static final Token ELLIPSIS =
    new Token(Type.ELLIPSIS, "…", false, true);
  public static final Token LEFT_PARENTHESIS =
    new Token(Type.LEFT_PARENTHESIS, "(", true, false);
  public static final Token RIGHT_PARENTHESIS =
    new Token(Type.RIGHT_PARENTHESIS, ")", false, true);
  public static final Token LEFT_QUOTES =
    new Token(Type.LEFT_QUOTES, "„", true, false);
  public static final Token RIGHT_QUOTES =
    new Token(Type.RIGHT_QUOTES, "“", false, true);

  private final Type type;
  private final String value;
  private final boolean spacerLeft, spacerRight;

  private Token()
  {
    throw new UnsupportedOperationException("unsupported default constructor");
  }

  private Token(final Type type, final String value,
                final boolean spacerLeft, boolean spacerRight)
  {
    if (type == null) {
      throw new NullPointerException("type");
    }
    if (value == null) {
      throw new NullPointerException("value");
    }
    this.type = type;
    this.value = value;
    this.spacerLeft = spacerLeft;
    this.spacerRight = spacerRight;
  }

  public static Token createWord(final String wordText) throws ParseException
  {
    final int errorOffset = parseWordText(wordText);
    if (errorOffset >= 0) {
      throw new ParseException("not a valid word: " + wordText, errorOffset);
    }
    final Token token = new Token(Type.WORD, wordText, true, true);
    return token;
  }

  /**
   * Returns error offset or -1 if no error occurred.
   */
  private static int parseWordText(final String wordText)
  {
    if (wordText == null) {
      return 0;
    }
    // TODO: wordText ::= RegExp("[a-zA-ZäöüÄÖÜ][a-zäöüß]+") .
    return -1;
  }

  public String getValue()
  {
    return value;
  }

  public Type getType()
  {
    return type;
  }

  public boolean getSpacerLeft()
  {
    return spacerLeft;
  }

  public boolean getSpacerRight()
  {
    return spacerRight;
  }

  public double matchScore(final Token other)
  {
    if (type != Token.Type.WORD) {
      return 0.0;
    }
    if (other.type != Token.Type.WORD) {
      return 0.0;
    }
    return StringDistance.computeSimilarity(value, other.value);
  }

  @Override
  public int compareTo(final Token other)
  {
    final int thisTypeOrd = type.ordinal();
    final int otherTypeOrd = other.type.ordinal();
    if (thisTypeOrd < otherTypeOrd) {
      return -1;
    } else if (thisTypeOrd > otherTypeOrd) {
      return 1;
    } else if (type != Type.WORD) {
      return 0;
    } else {
      return value.compareTo(other.value);
    }
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (!(obj instanceof Token)) {
      return false;
    }
    final Token other = (Token)obj;
    return compareTo(other) == 0;
  }

  @Override
  public int hashCode()
  {
    return value.hashCode();
  }

  @Override
  public String toString()
  {
    return "Token[type=" + type + ", value=" + value + "]";
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
