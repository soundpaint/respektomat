/*
 * @(#)Tokenizer.java 1.00 19/11/17
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

import java.io.FileReader;
import java.io.PushbackReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Deque;

public class Tokenizer
{
  private final String resourceId;
  private final PushbackReader pushbackReader;
  private final Deque<Token> lookAhead;

  private Tokenizer()
  {
    throw new UnsupportedOperationException("unsupported default constructor");
  }

  public Tokenizer(final String filePath) throws IOException
  {
    this(new FileReader(filePath), filePath);
  }

  public Tokenizer(final Reader reader)
  {
    this(new PushbackReader(reader));
  }

  public Tokenizer(final Reader reader, final String resourceId)
  {
    this(new PushbackReader(reader), resourceId);
  }

  public Tokenizer(final PushbackReader pushbackReader)
  {
    this(pushbackReader, null);
  }

  public Tokenizer(final PushbackReader pushbackReader,
                   final String resourceId)
  {
    if (pushbackReader == null) {
      throw new NullPointerException("pushbackReader");
    }
    this.resourceId = resourceId;
    this.pushbackReader = pushbackReader;
    lookAhead = new ArrayDeque<Token>();
  }

  private enum ParseState {
    START,
    IN_WORD,
    STOP,
    ERROR
  };

  private boolean isWhiteSpace(final int ch)
  {
    return
      (ch == ' ') ||
      (ch == '\t') ||
      (ch == '\r') ||
      (ch == '\n');
  }

  private boolean isWordChar(final int ch)
  {
    return
      ((ch >= '0') && (ch <= '9')) ||
      ((ch >= 'A') && (ch <= 'Z')) ||
      (ch == 'Ä') ||
      (ch == 'Ö') ||
      (ch == 'Ü') ||
      (ch == 'ẞ') ||
      ((ch >= 'a') && (ch <= 'z')) ||
      (ch == 'ä') ||
      (ch == 'ö') ||
      (ch == 'ü') ||
      (ch == 'ß');
  }

  private void skipWhiteSpace() throws IOException
  {
    while (true) {
      final int ch = pushbackReader.read();
      if (ch == -1) break;
      if (!isWhiteSpace(ch)) {
        pushbackReader.unread(ch);
        break;
      }
    }
  }

  private void fillLookAhead() throws IOException, ParseException
  {
    skipWhiteSpace();
    ParseState parseState = ParseState.START;
    StringBuffer wordText = null;
    Token token = null;
    while (parseState != ParseState.STOP) {
      final int ch = pushbackReader.read();
      switch (parseState) {
      case START:
        switch (ch) {
        case -1:
        case 65535:
          token = Token.EOF;
          parseState = ParseState.STOP;
          break;
        case ',':
          token = Token.COMMA;
          parseState = ParseState.STOP;
          break;
        case ';':
          token = Token.SEMICOLON;
          parseState = ParseState.STOP;
          break;
        case ':':
          token = Token.COLON;
          parseState = ParseState.STOP;
          break;
        case '-':
        case '–':
          token = Token.DASH;
          parseState = ParseState.STOP;
          break;
        case '.':
          token = Token.FULL_STOP;
          parseState = ParseState.STOP;
          break;
        case '!':
          token = Token.EXCLAMATION_MARK;
          parseState = ParseState.STOP;
          break;
        case '?':
          token = Token.QUESTION_MARK;
          parseState = ParseState.STOP;
          break;
        case '…':
          token = Token.ELLIPSIS;
          parseState = ParseState.STOP;
          break;
        case '(':
          token = Token.LEFT_PARENTHESIS;
          parseState = ParseState.STOP;
          break;
        case ')':
          token = Token.RIGHT_PARENTHESIS;
          parseState = ParseState.STOP;
          break;
        case '„':
          token = Token.LEFT_QUOTES;
          parseState = ParseState.STOP;
          break;
        case '“':
          token = Token.RIGHT_QUOTES;
          parseState = ParseState.STOP;
          break;
        default:
          if (isWordChar(ch)) {
            wordText = new StringBuffer();
            wordText.append((char)ch);
            parseState = ParseState.IN_WORD;
          } else {
            if (Config.DEBUG) {
              System.out.println("not a word char: " +
                                 (char)ch + "(" + ch + ")");
            }
            parseState = ParseState.ERROR;
          }
          break;
        }
        break;
      case IN_WORD:
        if (isWordChar(ch)) {
          wordText.append((char)ch);
          // parseState keeps IN_WORD
        } else {
          token = Token.createWord(wordText.toString());
          pushbackReader.unread(ch);
          parseState = ParseState.STOP;
        }
        break;
      default:
        throw new IllegalStateException("unexpected fall-through: " +
                                        parseState);
      }
    }
    lookAhead.addFirst(token);
  }

  public Token peekNext() throws IOException, ParseException
  {
    if (lookAhead.isEmpty()) {
      fillLookAhead();
    }
    return lookAhead.peekFirst();
  }

  public boolean hasNext() throws IOException, ParseException
  {
    return peekNext() != Token.EOF;
  }

  public Token getNext() throws IOException, ParseException
  {
    if (lookAhead.isEmpty()) {
      fillLookAhead();
    }
    return lookAhead.removeFirst();
  }

  public void unread(final Token token)
  {
    lookAhead.addFirst(token);
  }

  private String lookAheadToString()
  {
    final StringBuffer sb = new StringBuffer();
    for (final Token token : lookAhead) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(token);
    }
    return "{" + sb + "}";
  }

  @Override
  public String toString()
  {
    return "Tokenizer[resourceId=" + resourceId +
      ", pushbackReader=" + pushbackReader +
      ", lookAhead=" + lookAheadToString() + "]";
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
