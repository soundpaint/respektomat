/*
 * @(#)Sentencizer.java 1.00 19/11/17
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

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Deque;

public class Sentencizer
{
  private final Tokenizer tokenizer;
  private final SentenceFilter sentenceFilter;
  private final Deque<Sentence> lookAhead;

  private enum ParseState {
    START,
    IN_SENTENCE,
    STOP,
    EOF
  };

  private Sentencizer()
  {
    throw new UnsupportedOperationException("unsupported default constructor");
  }

  public Sentencizer(final Tokenizer tokenizer)
  {
    this(tokenizer, null);
  }

  public Sentencizer(final Tokenizer tokenizer,
                     final SentenceFilter sentenceFilter)
  {
    if (tokenizer == null) {
      throw new NullPointerException("tokenizer");
    }
    this.tokenizer = tokenizer;
    this.sentenceFilter = sentenceFilter;
    lookAhead = new ArrayDeque<Sentence>();
  }

  private Sentence parseSentence() throws IOException, ParseException
  {
    ParseState parseState = ParseState.START;
    Sentence sentence = null;
    while ((parseState != ParseState.STOP) &&
           (parseState != ParseState.EOF)) {
      final Token token = tokenizer.getNext();
      switch (parseState) {
      case START:
        if (token == Token.EOF) {
          sentence = Sentence.EOF;
          parseState = ParseState.EOF;
        } else if (token == Token.FULL_STOP) {
          sentence = new Sentence();
          sentence.addToken(token);
          parseState = ParseState.STOP;
        } else {
          sentence = new Sentence();
          sentence.addToken(token);
          parseState = ParseState.IN_SENTENCE;
        }
        break;
      case IN_SENTENCE:
        if (token == Token.EOF) {
          parseState = ParseState.EOF;
        } else if (token != Token.FULL_STOP) {
          sentence.addToken(token);
          // parseState keeps IN_SENTENCE
        } else {
          sentence.addToken(token);
          parseState = ParseState.STOP;
        }
        break;
      default:
        throw new IllegalStateException("unexpected fall-through: " +
                                        parseState);
      }
    }
    if (parseState == ParseState.STOP) {
      sentence.seal();
    }
    return sentence;
  }

  private void fillLookAhead() throws IOException, ParseException
  {
    while (true) {
      final Sentence sentence = parseSentence();
      if ((sentence == Sentence.EOF) ||
          (sentenceFilter == null) ||
          sentenceFilter.accept(sentence)) {
        lookAhead.addFirst(sentence);
        break;
      }
    }
  }

  public Sentence peekNext() throws IOException, ParseException
  {
    if (lookAhead.isEmpty()) {
      fillLookAhead();
    }
    return lookAhead.peekFirst();
  }

  public boolean hasNext() throws IOException, ParseException
  {
    return peekNext() != Sentence.EOF;
  }

  public Sentence getNext() throws IOException, ParseException
  {
    if (lookAhead.isEmpty()) {
      fillLookAhead();
    }
    return lookAhead.removeFirst();
  }

  public void unread(final Sentence sentence)
  {
    lookAhead.addFirst(sentence);
  }

  private String lookAheadToString()
  {
    final StringBuffer sb = new StringBuffer();
    for (final Sentence sentence : lookAhead) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(sentence);
    }
    return "{" + sb + "}";
  }

  @Override
  public String toString()
  {
    return "Sentencizer[tokenizer=" + tokenizer +
      ", sentenceFilter=" + sentenceFilter +
      ", lookAhead=" + lookAheadToString() + "]";
  }

  public static Sentence parseIncomplete(final String incompleteSentence)
    throws IOException, ParseException
  {
    final Tokenizer tokenizer =
      new Tokenizer(new StringReader(incompleteSentence));
    final Sentencizer sentencizer = new Sentencizer(tokenizer);
    final Sentence sentence = sentencizer.getNext();
    if (sentencizer.hasNext()) {
      throw new IllegalArgumentException("unexpected data after end of sentence");
    }
    return sentence;
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
