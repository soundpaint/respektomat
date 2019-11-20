/*
 * @(#)WikiReaderState.java 1.00 19/11/19
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
import org.xml.sax.Locator;

public class WikiReaderState
{
  private static interface MatchStateChangeListener
  {
    void startMatching();
    void stopMatching();
  }

  private static class Rule
  {
    private List<MatchStateChangeListener> listeners;
    private String[] matchExpressions;
    private int matching;
    private int mismatching;

    private Rule()
    {
      throw
        new UnsupportedOperationException("unsupported default constructor");
    }

    public Rule(final String[] matchExpressions)
    {
      this(matchExpressions, null);
    }

    public Rule(final String[] matchExpressions,
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
      for (final String matchExpression : matchExpressions) {
        if (matchExpression == null) {
          throw new IllegalArgumentException("matchExpression is null");
        }
        if (matchExpression.isEmpty()) {
          throw new IllegalArgumentException("matchExpression is empty");
        }
      }
    }

    private boolean matches(final String matchExpression, final String qName)
    {
      return matchExpression.equals(qName);
    }

    public void enter(final String qName)
    {
      final boolean isMatching = isMatching();
      if (mismatching == 0) {
        if (matching < matchExpressions.length) {
          final String matchExpression = matchExpressions[matching];
          if (matches(matchExpression, qName)) {
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
      for (final String matchExpression : matchExpressions) {
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

  private static final int MAX_PAGES_PROCESS = 10;

  private final List<MarkDownPageListener> listeners;
  private final Stack<String> elementPath;
  private final StringBuilder title;
  private final StringBuilder text;
  private final StringBuilder revisionId;
  private boolean inTitle;
  private boolean inText;
  private boolean inRevisionId;
  private Locator locator;
  private List<Rule> rules;
  private int pageCount;

  public WikiReaderState()
  {
    listeners = new ArrayList<MarkDownPageListener>();
    elementPath = new Stack<String>();
    title = new StringBuilder();
    text = new StringBuilder();
    revisionId = new StringBuilder();
    inTitle = false;
    inText = false;
    inRevisionId = false;
    locator = null;
    rules = new ArrayList<Rule>();
    for (final Rule rule : rulesDefinitions) {
      rules.add(rule);
    }
    pageCount = 0;
  }

  public void addListener(final MarkDownPageListener listener)
  {
    listeners.add(listener);
  }

  public int getPageCount()
  {
    return pageCount;
  }

  public boolean inTitle()
  {
    return inTitle;
  }

  public void appendToTitle(final String s)
  {
    title.append(s);
  }

  public boolean inText()
  {
    return inText;
  }

  public void appendToText(final String s)
  {
    text.append(s);
  }

  public boolean inRevisionId()
  {
    return inRevisionId;
  }

  public void appendToRevisionId(final String s)
  {
    revisionId.append(s);
  }

  private void notifyListeners()
  {
    for (final MarkDownPageListener listener : listeners) {
      listener.handleMarkDownPage(title.toString(),
                                  revisionId.toString(), text.toString());
    }
  }

  private Rule[] rulesDefinitions = {
    new Rule(new String[] {"mediawiki", "page"},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   pageCount++;
                 }
                 public void stopMatching()
                 {
                   if (pageCount == MAX_PAGES_PROCESS) {
                     System.exit(0);
                   }
                 }
               }
             }),
    new Rule(new String[] {"mediawiki", "page", "title"},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   inTitle = true;
                   title.setLength(0);
                 }
                 public void stopMatching()
                 {
                   inTitle = false;
                 }
               }
             }),
    new Rule(new String[] {"mediawiki", "page", "revision", "id"},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   inRevisionId = true;
                   revisionId.setLength(0);
                 }
                 public void stopMatching()
                 {
                   //System.out.println("revision id: " + revisionId);
                   inRevisionId = false;
                 }
               }
             }),
    new Rule(new String[] {"mediawiki", "page", "revision", "text"},
             new MatchStateChangeListener[] {
               new MatchStateChangeListener() {
                 public void startMatching()
                 {
                   inText = true;
                   text.setLength(0);
                 }
                 public void stopMatching()
                 {
                   inText = false;
                   notifyListeners();
                 }
               }
             })
  };

  public void setLocator(final Locator locator)
  {
    this.locator = locator;
  }

  public Locator getLocator()
  {
    return locator;
  }

  public void startElement(final String qName)
  {
    elementPath.push(qName);
    for (final Rule rule : rules) {
      rule.enter(qName);
    }
  }

  public String endElement()
  {
    final String qName = elementPath.pop();
    for (final Rule rule : rules) {
      rule.leave();
    }
    return qName;
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
