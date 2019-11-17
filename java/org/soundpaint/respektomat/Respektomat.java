/*
 * @(#)Respektomat.java 1.00 19/11/17
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

import java.io.PushbackReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Respektomat
{
  private static final String ESC = "\u001b";
  private static final String CSI = ESC + "[";
  private static final String ANSI_CLEAR_SCREEN = CSI + "2J";
  private static final String ANSI_CURSOR_HOME = CSI + "H";
  private static final String ANSI_BOLD = CSI + "1m";
  private static final String ANSI_NORMAL = CSI + "22m";
  private static final String ANSI_BG_BLACK = CSI + ";40m";
  private static final String ANSI_BG_WHITE = CSI + ";47m";
  private static final String ANSI_FG_RED = CSI + "31m";
  private static final String ANSI_FG_GREEN = CSI + "32m";
  private static final String ANSI_FG_YELLOW = CSI + "33m";
  private static final String ANSI_FG_CYAN = CSI + "36m";
  private static final String ANSI_FG_BLACK = CSI + "30m";
  private static final String ANSI_FG_WHITE = CSI + "37m";
  private static final String ANSI_INIT = ANSI_BG_BLACK + ANSI_FG_WHITE;
  private static final String ANSI_USER = ANSI_BOLD + ANSI_FG_RED;
  private static final String ANSI_PC = ANSI_NORMAL + ANSI_FG_CYAN;
  private static final String ANSI_PLAIN = ANSI_NORMAL + ANSI_FG_WHITE;

  private final Index index;
  private final List<Sentence> sentences;
  private final History history;

  private Respektomat()
  {
    throw new UnsupportedOperationException("unsupported default constructor");
  }

  public Respektomat(final String filePath)
    throws ParseException, IOException
  {
    this(new Tokenizer(filePath));
  }

  public Respektomat(final FileReader fileReader)
    throws ParseException, IOException
  {
    this(new Tokenizer(fileReader));
  }

  public Respektomat(final PushbackReader pushbackReader)
    throws ParseException, IOException
  {
    this(new Tokenizer(pushbackReader));
  }

  public Respektomat(final Tokenizer tokenizer)
    throws ParseException, IOException
  {
    this(new Sentencizer(tokenizer,
                         new SentenceFilter(Config.SENTENCE_FILTER)));
  }

  public Respektomat(final Sentencizer sentencizer)
    throws IOException, ParseException
  {
    index = new Index();
    sentences = new ArrayList<Sentence>();
    while (sentencizer.hasNext()) {
      final Sentence sentence = sentencizer.getNext();
      index.addSentence(sentence);
      sentences.add(sentence);
    }
    if (Config.DEBUG) {
      System.out.println("summary of index:");
      System.out.println("#sentences: " + sentences.size());
      System.out.println(index.createSummary());
    }
    history = new History();
  }

  private double computeScore(final Sentence sentence,
                              final Sentence incompleteSentence)
  {
    double score = 0;
    for (final Token token : incompleteSentence.getTokens()) {
      for (final Token indexedToken : index.getTokens()) {
        final double tokenScore = token.matchScore(indexedToken);
        if (tokenScore != 0.0) {
          final IndexForToken indexForToken =
            index.getIndexForToken(indexedToken);
          final IndexForTokenInSentence indexForTokenInSentence =
            indexForToken.getIndexForTokenInSentence(sentence);
          if (indexForTokenInSentence != null) {
            score += tokenScore * indexForTokenInSentence.count();
          }
        }
      }
    }
    score /*+*/= history.getScore(sentence);
    return score;
  }

  public Sentence suggestContinuation(final Sentence incompleteSentence)
  {
    final Map<Sentence, Double> scores = new TreeMap<Sentence, Double>();
    for (final Sentence sentence : sentences) {
      final double score = computeScore(sentence, incompleteSentence);
      scores.put(sentence, score);
    }
    final List<Sentence> suggestedSentences = new ArrayList<Sentence>();
    suggestedSentences.addAll(sentences);
    suggestedSentences.sort(new Comparator<Sentence>() {
        @Override
        public int compare(final Sentence sentence1, final Sentence sentence2) {
          return
            (int)(Math.signum(scores.get(sentence2) - scores.get(sentence1)));
        }
      });
    if (Config.DEBUG) {
      for (final Sentence sentence : suggestedSentences) {
        System.out.println(ANSI_FG_GREEN + scores.get(sentence) + " " +
                           ANSI_PLAIN + sentence);
      }
    }
    if (!suggestedSentences.isEmpty()) {
      return suggestedSentences.get(0);
    } else {
      return Sentence.EOF;
    }
  }

  private void run() throws IOException, ParseException
  {
    System.out.print(ANSI_INIT + ANSI_CLEAR_SCREEN + ANSI_CURSOR_HOME);
    System.out.println(ANSI_FG_YELLOW + ANSI_BOLD +
                       "----===|    " + Config.GREETING_TITLE + "    |===----" +
                       ANSI_PLAIN);
    System.out.println("Version 1.0, (C) 2019 by J. Reuter, Karlsruhe");
    System.out.println("This software is free software licensed by GNU GPLv3.");
    System.out.println();
    System.out.println("Enter random text or 'q' to quit.");
    while (true) {
      System.out.print(ANSI_USER + "Du> ");
      final String unparsedSentence = System.console().readLine();
      System.out.print(ANSI_PLAIN);
      if ("q".equalsIgnoreCase(unparsedSentence)) {
        break;
      }
      final Sentence incompleteSentence =
        Sentencizer.parseIncomplete(unparsedSentence);
      final Sentence continuedSentence =
        suggestContinuation(incompleteSentence);
      history.add(continuedSentence);
      System.out.println(ANSI_PC + Config.PC_PROMPT + "> " +
                         continuedSentence.prettyPrint() + ANSI_PLAIN);
    }
    System.out.println("=> Quit");
  }

  public static void main(final String argv[])
    throws IOException, ParseException
  {
    final Respektomat respektomat = new Respektomat(Config.DATABASE_XML_FILE);
    respektomat.run();
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
