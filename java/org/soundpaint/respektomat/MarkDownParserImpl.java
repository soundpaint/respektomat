/*
 * @(#)MarkDownParserImpl.java 1.00 19/11/19
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

/**
 * This is, for now, only a partial implementation of a Markdown
 * parser, just enough to extract plain text outside of markdown block
 * elements.  Block elements like enumerations are just skipped over.
 *
 * Due to its incompleteness, this parser may fail in some corner
 * cases and, as a result, ouput garbage, e.g. a mixture of plain text
 * and messed-up markup characters.  Therefore, when splitting the
 * output of this parser further into sentences (e.g. by parsing for
 * full stops, exclamation marks or question marks), those sentences
 * that contain suspicious characters (like "=", "&lt;", "&gt;", "*",
 * "{", "}", "[" and "]") should be removed.
 */
public class MarkDownParserImpl implements MarkDownParser
{
  private enum ParseState {
    PARSE_START,
    PARSE_LINK,
    PARSE_SINGLE_LEFT_EQ,
    PARSE_H1,
    PARSE_DOUBLE_LEFT_EQ,
    PARSE_H2,
    PARSE_TRIPLE_LEFT_EQ,
    PARSE_H3,
    PARSE_QUADRUPLE_LEFT_EQ,
    PARSE_H4,
    PARSE_QUINTUPLE_LEFT_EQ,
    PARSE_RIGHT_4_EQ,
    PARSE_RIGHT_3_EQ,
    PARSE_RIGHT_2_EQ,
    PARSE_RIGHT_1_EQ,
    PARSE_BULLET,
    PARSE_SINGLE_QUOTE,
    PARSE_DOUBLE_QUOTE,
    PARSE_TRIPLE_QUOTE,
    PARSE_INTERNAL_LINK_URI_OR_LABEL,
    PARSE_INTERNAL_LINK_PROPERTY_OR_LABEL,
    PARSE_CATEGORY_LINK,
    PARSE_CATEGORY_LINK_CATEGORY,
    PARSE_CATEGORY_LINK_STOP,
    PARSE_FILE_LINK,
    PARSE_FILE_LINK_PATH,
    PARSE_FILE_LINK_PROPERTY_OR_LABEL,
    PARSE_FILE_LINK_STOP,
    PARSE_EXTERNAL_LINK_URI,
    PARSE_EXTERNAL_LINK_LABEL,
    PARSE_INTERNAL_LINK_STOP,
    PARSE_TEMPLATE_OR_TABLE,
    PARSE_TEMPLATE,
    PARSE_TEMPLATE_TYPE,
    PARSE_TEMPLATE_NAME,
    PARSE_TEMPLATE_PROPERTIES,
    PARSE_TEMPLATE_STOP,
    PARSE_TABLE_HEAD,
    PARSE_TABLE_CELL,
    PARSE_TABLE_NEXT_CELL_OR_END,
    EOF,
    ERROR
  }

  private final Sentencizer sentencizer;
  private final MarkDownHandler handler;
  private final StringBuilder textBuffer;
  private String text;
  private int pos;
  private int line;
  private int linePos;
  private ParseState state;

  private MarkDownParserImpl()
  {
    throw new UnsupportedOperationException("unsupported default constructor");
  }

  public MarkDownParserImpl(final Sentencizer sentencizer,
                            final MarkDownHandler handler)
  {
    if (sentencizer == null) {
      throw new NullPointerException("sentencizer");
    }
    this.sentencizer = sentencizer;
    if (handler == null) {
      throw new NullPointerException("handler");
    }
    this.handler = handler;
    textBuffer = new StringBuilder();
  }

  private void resetText(final String text)
  {
    this.text = text;
    pos = 0;
    line = 0;
    linePos = 0;
    state = ParseState.PARSE_START;
    textBuffer.setLength(0);
  }

  private char acceptChar()
  {
    final char ch = lookAhead();
    if (pos < text.length()) {
      pos++;
      linePos++;
    } else {
      state = ParseState.EOF;
    }
    if ((ch == '\r') || (ch == '\n')) {
      line++;
      linePos = 0;
    }
    return ch;
  }

  private char lookAhead()
  {
    if (pos < text.length()) {
      return text.charAt(pos);
    } else {
      return '\u0000';
    }
  }

  private boolean haveLookAhead(final String lookAhead)
  {
    if (lookAhead == null) {
      throw new NullPointerException("lookAhead");
    }
    if (pos >= text.length()) {
      return false;
    }
    return lookAhead.equals(text.substring(pos, pos + lookAhead.length()));
  }

  private boolean eof()
  {
    return state == ParseState.EOF;
  }

  private boolean haveEofAfter(final int charCount)
  {
    return pos + charCount >= text.length();
  }

  private void pushBack(final char ch)
  {
    pos--;
    linePos--;
    if (linePos < -1) {
      throw new IllegalStateException("push back across lines not supported");
    }
    assert ch == lookAhead();
  }

  private boolean haveError()
  {
    return state == ParseState.ERROR;
  }

  private void flushText()
  {
    handler.appendTextFragment(textBuffer.toString());
    textBuffer.setLength(0);
  }

  private void logError(final int line, final int linePos, final String message)
  {
    System.out.println("error: " + message);
    System.out.println("(line= " + line + ", pos=" + linePos + ")");
    System.out.println("latest parsed characters: " + handler.getPendingText() +
                       textBuffer);
    throw new RuntimeException("unexpected parse error at line=" + line +
                               ", pos=" + linePos + ": " +
                               message);
  }

  private void error(final String message)
  {
    state = ParseState.ERROR;
    logError(line, linePos, message);
  }

  private static boolean isWhiteSpace(final int ch)
  {
    return
      (ch == ' ') ||
      (ch == '\t') ||
      (ch == '\r') ||
      (ch == '\n');
  }

  private static final boolean SUPPORT_SINGLE_QUOTED_TEXT = false;

  private String parse(final String text)
  {
    resetText(text);
    handler.reset();
    handler.startElement(MarkDownElement.Type.Root);
    handler.startElement(MarkDownElement.Type.Text);
    while (!haveError() && !eof()) {
      final char ch = acceptChar();
      switch (state) {
      case PARSE_START:
        if (ch == '\'') {
          flushText();
          state = ParseState.PARSE_SINGLE_QUOTE;
        } else if ((ch == '=') && (linePos == 1)) {
          flushText();
          state = ParseState.PARSE_SINGLE_LEFT_EQ;
        } else if (ch == '[') {
          flushText();
          state = ParseState.PARSE_LINK;
        } else if (ch == '{') {
          flushText();
          state = ParseState.PARSE_TEMPLATE_OR_TABLE;
        } else if ((ch == '*') && (linePos == 1)) {
          flushText();
          state = ParseState.PARSE_BULLET;
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_BULLET:
        if (haveLookAhead("\r") ||
            haveLookAhead("\n") ||
            haveEofAfter(0)) {
          handler.startElement(MarkDownElement.Type.Bullet);
          flushText();
          handler.endElement(MarkDownElement.Type.Bullet);
          state = ParseState.PARSE_START;
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_SINGLE_LEFT_EQ:
        if (ch == '=') {
          state = ParseState.PARSE_DOUBLE_LEFT_EQ;
        } else {
          state = ParseState.PARSE_H1;
          textBuffer.append(ch);
          handler.startElement(MarkDownElement.Type.H1);
        }
        break;
      case PARSE_H1:
        if (ch == '=') {
          flushText();
          handler.endElement(MarkDownElement.Type.H1);
          state = ParseState.PARSE_START;
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_DOUBLE_LEFT_EQ:
        if (ch == '=') {
          state = ParseState.PARSE_TRIPLE_LEFT_EQ;
        } else {
          state = ParseState.PARSE_H2;
          textBuffer.append(ch);
          handler.startElement(MarkDownElement.Type.H2);
        }
        break;
      case PARSE_H2:
        if (ch == '=') {
          flushText();
          handler.endElement(MarkDownElement.Type.H2);
          state = ParseState.PARSE_RIGHT_1_EQ;
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_TRIPLE_LEFT_EQ:
        if (ch == '=') {
          state = ParseState.PARSE_QUADRUPLE_LEFT_EQ;
        } else {
          state = ParseState.PARSE_H3;
          textBuffer.append(ch);
          handler.startElement(MarkDownElement.Type.H3);
        }
        break;
      case PARSE_H3:
        if (ch == '=') {
          flushText();
          handler.endElement(MarkDownElement.Type.H3);
          state = ParseState.PARSE_RIGHT_2_EQ;
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_QUADRUPLE_LEFT_EQ:
        if (ch == '=') {
          state = ParseState.PARSE_QUINTUPLE_LEFT_EQ;
        } else {
          state = ParseState.PARSE_H4;
          textBuffer.append(ch);
          handler.startElement(MarkDownElement.Type.H4);
        }
        break;
      case PARSE_H4:
        if (ch == '=') {
          flushText();
          handler.endElement(MarkDownElement.Type.H4);
          state = ParseState.PARSE_RIGHT_3_EQ;
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_RIGHT_4_EQ:
        if (ch == '=') {
          state = ParseState.PARSE_RIGHT_3_EQ;
        } else {
          error("\"=\" expected");
        }
        break;
      case PARSE_RIGHT_3_EQ:
        if (ch == '=') {
          state = ParseState.PARSE_RIGHT_2_EQ;
        } else {
          error("\"=\" expected");
        }
        break;
      case PARSE_RIGHT_2_EQ:
        if (ch == '=') {
          state = ParseState.PARSE_RIGHT_1_EQ;
        } else {
          error("\"=\" expected");
        }
        break;
      case PARSE_RIGHT_1_EQ:
        if (ch == '=') {
          state = ParseState.PARSE_START;
        } else {
          error("\"=\" expected");
        }
        break;
      case PARSE_SINGLE_QUOTE:
        if (ch == '\'') {
          state = ParseState.PARSE_DOUBLE_QUOTE;
        } else if (SUPPORT_SINGLE_QUOTED_TEXT) {
          handler.startElement(MarkDownElement.Type.SingleQuote);
          handler.endElement(MarkDownElement.Type.SingleQuote);
          state = ParseState.PARSE_START;
        } else {
          pushBack(ch);
          state = ParseState.PARSE_START;
        }
        break;
      case PARSE_DOUBLE_QUOTE:
        if (ch == '\'') {
          handler.startElement(MarkDownElement.Type.TripleQuote);
          handler.endElement(MarkDownElement.Type.TripleQuote);
          state = ParseState.PARSE_START;
        } else {
          pushBack(ch);
          handler.startElement(MarkDownElement.Type.DoubleQuote);
          handler.endElement(MarkDownElement.Type.DoubleQuote);
          state = ParseState.PARSE_START;
        }
        break;
      case PARSE_TEMPLATE_OR_TABLE:
        if (ch == '{') {
          state = ParseState.PARSE_TEMPLATE;
          handler.startElement(MarkDownElement.Type.Template);
          handler.startElement(MarkDownElement.Type.TemplateType);
        } else if (ch == '|') {
          state = ParseState.PARSE_TABLE_HEAD;
          handler.startElement(MarkDownElement.Type.Table);
          handler.startElement(MarkDownElement.Type.TableHead);
        } else {
          error("'{' or '|' expected");
        }
        break;
      case PARSE_TEMPLATE:
        if (ch == '}') {
          flushText();
          handler.endElement(MarkDownElement.Type.TemplateType);
          state = ParseState.PARSE_TEMPLATE_STOP;
        } else if (isWhiteSpace(ch)) {
          flushText();
          handler.endElement(MarkDownElement.Type.TemplateType);
          state = ParseState.PARSE_TEMPLATE_PROPERTIES;
          handler.startElement(MarkDownElement.Type.TemplateProperties);
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_TEMPLATE_PROPERTIES:
        if (ch == '}') {
          flushText();
          handler.endElement(MarkDownElement.Type.TemplateProperties);
          state = ParseState.PARSE_TEMPLATE_STOP;
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_TEMPLATE_STOP:
        if (ch == '}') {
          handler.endElement(MarkDownElement.Type.Template);
          state = ParseState.PARSE_START;
        } else {
          error("'}' expected");
        }
        break;
      case PARSE_TABLE_HEAD:
        if (ch == '|') {
          flushText();
          handler.endElement(MarkDownElement.Type.TableHead);
          state = ParseState.PARSE_TABLE_NEXT_CELL_OR_END;
          handler.startElement(MarkDownElement.Type.TableBody);
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_TABLE_CELL:
        if (ch == '|') {
          flushText();
          //handler.endElement(MarkDownElement.Type.TableCell);
          state = ParseState.PARSE_TABLE_NEXT_CELL_OR_END;
          //handler.startElement(MarkDownElement.Type.TableCell);
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_TABLE_NEXT_CELL_OR_END:
        if (ch == '}') {
          flushText();
          handler.endElement(MarkDownElement.Type.TableBody);
          handler.endElement(MarkDownElement.Type.Table);
          state = ParseState.PARSE_START;
        } else {
          textBuffer.append(ch);
          //handler.endElement(MarkDownElement.Type.TableCell);
          state = ParseState.PARSE_TABLE_CELL;
          //handler.startElement(MarkDownElement.Type.TableCell);
        }
        break;
      case PARSE_LINK:
        if (ch == '[') {
          if (haveLookAhead("Kategorie:")) {
            state = ParseState.PARSE_CATEGORY_LINK;
            handler.startElement(MarkDownElement.Type.CategoryLink);
          } else if (haveLookAhead("Datei:")) {
            state = ParseState.PARSE_FILE_LINK;
            handler.startElement(MarkDownElement.Type.FileLink);
          } else {
            state = ParseState.PARSE_INTERNAL_LINK_URI_OR_LABEL;
            handler.startElement(MarkDownElement.Type.InternalLink);
            handler.startElement(MarkDownElement.Type.LinkUri);
          }
        } else {
          state = ParseState.PARSE_EXTERNAL_LINK_URI;
          handler.startElement(MarkDownElement.Type.ExternalLink);
          handler.startElement(MarkDownElement.Type.LinkUri);
          textBuffer.append(ch);
        }
        break;
      case PARSE_FILE_LINK:
        if (ch == ':') {
          state = ParseState.PARSE_FILE_LINK_PATH;
          handler.startElement(MarkDownElement.Type.LinkPath);
        } else {
        }
        break;
      case PARSE_FILE_LINK_PATH:
        if ((ch == ']') &&
            (haveLookAhead("]\r") ||
             haveLookAhead("]\n") ||
             (haveLookAhead("]") && haveEofAfter("]".length())))) {
          flushText();
          handler.endElement(MarkDownElement.Type.LinkPath);
          state = ParseState.PARSE_FILE_LINK_STOP;
        } else if (ch == '|') {
          flushText();
          handler.endElement(MarkDownElement.Type.LinkPath);
          state = ParseState.PARSE_FILE_LINK_PROPERTY_OR_LABEL;
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_FILE_LINK_PROPERTY_OR_LABEL:
        if ((ch == ']') &&
            (haveLookAhead("]\r") ||
             haveLookAhead("]\n") ||
             (haveLookAhead("]") && haveEofAfter("]".length())))) {
          handler.startElement(MarkDownElement.Type.LinkLabel);
          flushText();
          handler.endElement(MarkDownElement.Type.LinkLabel);
          state = ParseState.PARSE_FILE_LINK_STOP;
        } else if (ch == '|') {
          handler.startElement(MarkDownElement.Type.LinkProperty);
          flushText();
          handler.endElement(MarkDownElement.Type.LinkProperty);
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_FILE_LINK_STOP:
        if (ch == ']') {
          flushText();
          handler.endElement(MarkDownElement.Type.FileLink);
          state = ParseState.PARSE_START;
        } else {
          error("']' expected");
        }
        break;
      case PARSE_CATEGORY_LINK:
        if (ch == ':') {
          state = ParseState.PARSE_CATEGORY_LINK_CATEGORY;
          handler.startElement(MarkDownElement.Type.LinkCategory);
        } else {
        }
        break;
      case PARSE_CATEGORY_LINK_CATEGORY:
        if (ch == ']') {
          flushText();
          handler.endElement(MarkDownElement.Type.LinkCategory);
          state = ParseState.PARSE_CATEGORY_LINK_STOP;
        } else if (ch == '|') {
          flushText();
          handler.endElement(MarkDownElement.Type.LinkCategory);
          handler.startElement(MarkDownElement.Type.LinkCategory);
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_CATEGORY_LINK_STOP:
        if (ch == ']') {
          flushText();
          handler.endElement(MarkDownElement.Type.CategoryLink);
          state = ParseState.PARSE_START;
        } else {
          error("']' expected");
        }
        break;
      case PARSE_INTERNAL_LINK_URI_OR_LABEL:
        if (ch == '|') {
          flushText();
          handler.endElement(MarkDownElement.Type.LinkUri);
          state = ParseState.PARSE_INTERNAL_LINK_PROPERTY_OR_LABEL;
        } else if (ch == ']') {
          handler.endElement(MarkDownElement.Type.LinkUri);
          handler.startElement(MarkDownElement.Type.LinkLabel);
          flushText();
          handler.endElement(MarkDownElement.Type.LinkLabel);
          state = ParseState.PARSE_INTERNAL_LINK_STOP;
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_INTERNAL_LINK_PROPERTY_OR_LABEL:
        if (ch == ']') {
          handler.startElement(MarkDownElement.Type.LinkLabel);
          flushText();
          handler.endElement(MarkDownElement.Type.LinkLabel);
          state = ParseState.PARSE_INTERNAL_LINK_STOP;
        } else if (ch == '|') {
          handler.startElement(MarkDownElement.Type.LinkProperty);
          flushText();
          handler.endElement(MarkDownElement.Type.LinkProperty);
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_INTERNAL_LINK_STOP:
        if (ch == ']') {
          flushText();
          handler.endElement(MarkDownElement.Type.InternalLink);
          state = ParseState.PARSE_START;
        } else {
          error("']' expected");
        }
        break;
      case PARSE_EXTERNAL_LINK_URI:
        if (ch == ' ') {
          flushText();
          handler.endElement(MarkDownElement.Type.LinkUri);
          state = ParseState.PARSE_EXTERNAL_LINK_LABEL;
          handler.startElement(MarkDownElement.Type.LinkLabel);
        } else if (ch == ']') {
          flushText();
          handler.endElement(MarkDownElement.Type.LinkUri);
          handler.endElement(MarkDownElement.Type.ExternalLink);
          state = ParseState.PARSE_START;
        } else {
          textBuffer.append(ch);
        }
        break;
      case PARSE_EXTERNAL_LINK_LABEL:
        if (ch == ']') {
          flushText();
          handler.endElement(MarkDownElement.Type.LinkLabel);
          handler.endElement(MarkDownElement.Type.ExternalLink);
          state = ParseState.PARSE_START;
        } else {
          textBuffer.append(ch);
        }
        break;
      case EOF:
        break;
      case ERROR:
        throw new IllegalStateException("failed parsing MarkDown document");
      default:
        throw new IllegalStateException("unexpected parse state: " + state);
      }
    }
    if (!eof()) {
      error("found trailing chars");
    }
    handler.endElement(MarkDownElement.Type.Text);
    handler.endElement(MarkDownElement.Type.Root);
    return handler.getRenderedText();
  }

  private String squeezeWhiteSpace(final String text)
  {
    final StringBuilder squeezedText = new StringBuilder();
    boolean prevWasWhiteSpace = true;
    for (final char ch : text.toCharArray()) {
      final boolean isWS = isWhiteSpace(ch);
      if (isWS && prevWasWhiteSpace) {
        // skip this character
      } else {
        squeezedText.append(ch);
        prevWasWhiteSpace = isWS;
      }
    }
    return squeezedText.toString();
  }

  private String cleanText(final String dirtyText)
  {
    String text = dirtyText;
    text = text.replace("&nbsp;", "\u00a0");
    text = squeezeWhiteSpace(text);
    text = text.replace("\u00a0", " ");
    return text;
  }

  @Override
  public void parseDocument(final String title,
                            final String revisionId,
                            final String text)
  {
    //System.out.println("Title: " + title);
    //System.out.println("Revision: " + revisionId);
    final String renderedText = parse(text);
    sentencizer.parseText(cleanText(renderedText));
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
