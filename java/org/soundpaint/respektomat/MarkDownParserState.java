/*
 * @(#)MarkDownParserState.java 1.00 19/11/23
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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MarkDownParserState
{
  private static interface MatchStateChangeListener
  {
    void startMatching();
    void stopMatching();
  }

  private static class Rule
  {
    private final List<MatchStateChangeListener> listeners;
    private final MarkDownElement.Type[] matchExpressions;
    private int matching;
    private int mismatching;

    private Rule()
    {
      throw
        new UnsupportedOperationException("unsupported default constructor");
    }

    public Rule(final MarkDownElement.Type[] matchExpressions)
    {
      this(matchExpressions, null);
    }

    public Rule(final MarkDownElement.Type[] matchExpressions,
                final MatchStateChangeListener[] listeners)
    {
      this.matchExpressions =
        Arrays.copyOf(matchExpressions, matchExpressions.length);
      this.listeners = new ArrayList<MatchStateChangeListener>();
      if (listeners != null) {
        for (final MatchStateChangeListener listener : listeners) {
          addListener(listener);
        }
      }
      matching = 0;
      mismatching = 0;
      checkMatchExpressions();
    }

    public void addListener(final MatchStateChangeListener listener)
    {
      if (listener == null) {
        throw new NullPointerException("listener");
      }
      listeners.add(listener);
    }

    public void removeListener(final MatchStateChangeListener listener)
    {
      if (listener == null) {
        throw new NullPointerException("listener");
      }
      listeners.remove(listener);
    }

    private void announceMatch()
    {
      for (final MatchStateChangeListener listener : listeners) {
        listener.startMatching();
      }
    }

    private void deannounceMatch()
    {
      for (final MatchStateChangeListener listener : listeners) {
        listener.stopMatching();
      }
    }

    private void checkMatchExpressions()
    {
      for (final MarkDownElement.Type matchExpression : matchExpressions) {
        if (matchExpression == null) {
          throw new IllegalArgumentException("matchExpression is null");
        }
      }
    }

    private boolean matches(final MarkDownElement.Type matchExpression,
                            final MarkDownElement.Type type)
    {
      return matchExpression.equals(type);
    }

    public void enter(final MarkDownElement.Type type)
    {
      final boolean isMatching = isMatching();
      if (mismatching == 0) {
        if (matching < matchExpressions.length) {
          final MarkDownElement.Type matchExpression =
            matchExpressions[matching];
          if (matches(matchExpression, type)) {
            matching++;
          } else {
            mismatching++;
          }
        } else {
          mismatching++;
        }
      } else {
        mismatching++;
      }
      if (!isMatching && isMatching()) {
        announceMatch();
      }
    }

    public void leave()
    {
      final boolean isMatching = isMatching();
      if (mismatching > 0) {
        mismatching--;
      } else if (matching > 0) {
        matching--;
      } else {
        throw new IllegalStateException("already at top");
      }
      if (isMatching && !isMatching()) {
        deannounceMatch();
      }
    }

    public boolean isMatching()
    {
      return (mismatching == 0) && (matching == matchExpressions.length);
    }

    public String toString()
    {
      final StringBuilder sb = new StringBuilder();
      int depth = 0;
      for (final MarkDownElement.Type matchExpression : matchExpressions) {
        if (sb.length() > 0) {
          sb.append(" → ");
        }
        if (depth >= matching) {
          sb.append("[");
        }
        sb.append(matchExpression);
        if (depth >= matching) {
          sb.append("]");
        }
        depth++;
      }
      for (int i = 0; i < mismatching; i++) {
        sb.append(" → …");
      }
      return sb.toString();
    }
  }

  private static final String LINE_SEPARATOR = System.lineSeparator();
  private final MarkDownDocument document;
  private final List<MarkDownHandler> listeners;
  private final Stack<MarkDownElement.Type> path;
  private final StringBuilder text;
  private final StringBuilder href;
  private final StringBuilder renderedText;
  private List<Rule> rules;
  private int inTemplate;
  private int inTable;
  private int inUri;
  private int inLinkCategory;
  private int inFileLink;
  private int inH1, inH2, inH3, inH4, inH5;
  private int inBullet;

  public MarkDownParserState()
  {
    document = new MarkDownDocument();
    listeners = new ArrayList<MarkDownHandler>();
    path = new Stack<MarkDownElement.Type>();
    text = new StringBuilder();
    href = new StringBuilder();
    renderedText = new StringBuilder();
    rules = new ArrayList<Rule>();
    for (final Rule rule : rulesDefinitions) {
      rules.add(rule);
    }
    reset();
  }

  public void reset()
  {
    path.clear();
    text.setLength(0);
    href.setLength(0);
    renderedText.setLength(0);
    inTemplate = 0;
    inTable = 0;
    inUri = 0;
    inLinkCategory = 0;
    inFileLink = 0;
    inH1 = inH2 = inH3 = inH4 = inH5 = 0;
    inBullet = 0;
  }

  public String getRenderedText()
  {
    return renderedText.toString();
  }

  public void addListener(final MarkDownHandler listener)
  {
    listeners.add(listener);
  }

  private Rule[] rulesDefinitions = {
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                 }
                 public void stopMatching()
                 {
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.H1},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   //renderedText.append("<h1>");
                   inH1++;
                 }
                 public void stopMatching()
                 {
                   //renderedText.append("</h1>");
                   inH1--;
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.H2},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   //renderedText.append("<h2>");
                   inH2++;
                 }
                 public void stopMatching()
                 {
                   //renderedText.append("</h2>");
                   inH2--;
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.H3},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   //renderedText.append("<h3>");
                   inH3++;
                 }
                 public void stopMatching()
                 {
                   //renderedText.append("</h3>");
                   inH3--;
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.H4},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   //renderedText.append("<h4>");
                   inH4++;
                 }
                 public void stopMatching()
                 {
                   //renderedText.append("</h4>");
                   inH4--;
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.H5},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   //renderedText.append("<h5>");
                   inH5++;
                 }
                 public void stopMatching()
                 {
                   //renderedText.append("</h5>");
                   inH5--;
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.Bullet},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   //renderedText.append("<li>");
                   inBullet++;
                 }
                 public void stopMatching()
                 {
                   //renderedText.append("</li>");
                   inBullet--;
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.Italic},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   //renderedText.append("<i>");
                 }
                 public void stopMatching()
                 {
                   //renderedText.append("</i>");
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.Bold},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   //renderedText.append("<b>");
                 }
                 public void stopMatching()
                 {
                   //renderedText.append("</b>");
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.ExternalLink},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   href.setLength(0);
                 }
                 public void stopMatching()
                 {
                   href.append(text);
                   //renderedText.append("<a href=\"" + href + "\">");
                   //renderedText.append(text);
                   //renderedText.append("</a>");
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.ExternalLink,
                                         MarkDownElement.Type.LinkUri},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   inUri++;
                 }
                 public void stopMatching()
                 {
                   inUri--;
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.ExternalLink,
                                         MarkDownElement.Type.LinkLabel},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                 }
                 public void stopMatching()
                 {
                   renderedText.append(text);
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.InternalLink},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   href.setLength(0);
                 }
                 public void stopMatching()
                 {
                   href.append(text);
                   //renderedText.append("<a href=\"" + href + "\">");
                   //renderedText.append(text);
                   //renderedText.append("</a>");
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.InternalLink,
                                         MarkDownElement.Type.LinkUri},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   inUri++;
                 }
                 public void stopMatching()
                 {
                   inUri--;
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.InternalLink,
                                         MarkDownElement.Type.LinkLabel},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                 }
                 public void stopMatching()
                 {
                   renderedText.append(text);
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.CategoryLink,
                                         MarkDownElement.Type.LinkCategory},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   inLinkCategory++;
                 }
                 public void stopMatching()
                 {
                   inLinkCategory--;
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.FileLink},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   inFileLink++;
                 }
                 public void stopMatching()
                 {
                   inFileLink--;
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.Template},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   //renderedText.append("{{");
                   inTemplate++;
                 }
                 public void stopMatching()
                 {
                   //renderedText.append("}}");
                   inTemplate--;
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.Table},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   //renderedText.append("{|");
                   inTable++;
                 }
                 public void stopMatching()
                 {
                   //renderedText.append("|}");
                   inTable--;
                 }
               }
             }),
    new Rule(new MarkDownElement.Type[] {MarkDownElement.Type.Root,
                                         MarkDownElement.Type.Text,
                                         MarkDownElement.Type.Bullet},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   renderedText.append(" ");
                   //renderedText.append(" *");
                 }
                 public void stopMatching()
                 {
                   renderedText.append(" ");
                   //renderedText.append(LINE_SEPARATOR);
                 }
               }
             })
  };

  public void startElement(final MarkDownElement.Type type)
  {
    if (!type.preserveWhiteSpace()) {
      //renderedText.append(LINE_SEPARATOR);
    }
    //renderedText.append("<" + type + ">");
    path.push(type);
    for (final Rule rule : rules) {
      rule.enter(type);
    }
  }

  public MarkDownElement.Type endElement(final MarkDownElement.Type type)
  {
    final MarkDownElement.Type expectedType = path.pop();
    if (expectedType != type) {
      throw new IllegalStateException("got closing " + type
                                      + ", but expected " + expectedType);
    }
    for (final Rule rule : rules) {
      rule.leave();
    }
    if (!type.preserveWhiteSpace()) {
      //renderedText.append(LINE_SEPARATOR);
    }
    //renderedText.append("</" + type + ">");
    return type;
  }

  public void addText(final String text)
  {
    if (inTemplate > 0) {
      return;
    }
    if (inTable > 0) {
      return;
    }
    if (inUri > 0) {
      return;
    }
    if (inLinkCategory > 0) {
      return;
    }
    if (inFileLink > 0) {
      return;
    }
    if ((inH1 > 0) || (inH2 > 0) || (inH3 > 0) || (inH4 > 0) || (inH5 > 0)) {
      return;
    }
    if (inBullet > 0) {
      return;
    }
    renderedText.append(text);
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
