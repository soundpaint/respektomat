/*
 * @(#)WikiTextHandler.java 1.00 30/11/19
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

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

public class WikiTextHandler extends DefaultHandler implements LexicalHandler
{
  private final MarkDownParser markDownParser;
  private final StringBuilder characters;
  private String title;
  private String revisionId;

  private WikiTextHandler()
  {
    throw new UnsupportedOperationException("unsupported default constructor");
  }

  public WikiTextHandler(final MarkDownParser markDownParser)
  {
    if (markDownParser == null) {
      throw new NullPointerException("markDownParser");
    }
    this.markDownParser = markDownParser;
    characters = new StringBuilder();
  }

  public void setTitle(final String title)
  {
    if (title == null) {
      throw new NullPointerException("title");
    }
    this.title = title;
  }

  public void setRevisionId(final String revisionId)
  {
    if (revisionId == null) {
      throw new NullPointerException("revisionId");
    }
    this.revisionId = revisionId;
  }

  public String getCharacters()
  {
    return characters.toString();
  }

  @Override
  public InputSource resolveEntity(final String publicId,
                                   final String systemId)
  {
    return null; // nothing yet
  }

  @Override
  public void notationDecl(final String name,
                           final String publicId,
                           final String systemId)
  {
    // nothing yet
  }

  @Override
  public void unparsedEntityDecl(final String name,
                                 final String publicId,
                                 final String systemId,
                                 final String notationName)
  {
    // nothing yet
  }

  @Override
  public void setDocumentLocator(final Locator locator)
  {
    // nothing yet
  }

  @Override
  public void startDocument()
  {
    characters.setLength(0);
  }

  @Override
  public void endDocument()
  {
    markDownParser.parseDocument(title, revisionId, characters.toString());
  }

  @Override
  public void startPrefixMapping(final String prefix,
                                 final String uri)
  {
    // nothing yet
  }

  @Override
  public void endPrefixMapping(final String prefix)
  {
    // nothing yet
  }

  @Override
  public void startElement(final String uri, final String localName,
                           final String qName,
                           final Attributes attributes)
  {
    // strip off tags
  }

  @Override
  public void endElement(final String uri, final String localName,
                         final String qName)
    throws SAXParseException
  {
    // strip off tags
  }

  @Override
  public void characters(final char ch[], final int start,
                         final int length)
  {
    characters.append(new String(ch, start, length));
  }

  @Override
  public void ignorableWhitespace(final char ch[], final int start,
                                  final int length)
  {
    // strip off ignorable white space
  }

  @Override
  public void processingInstruction(final String target,
                                    final String data)
  {
    // nothing yet
  }

  @Override
  public void skippedEntity(final String name)
  {
    // nothing yet
  }

  @Override
  public void warning(final SAXParseException e)
  {
    // nothing yet
  }

  @Override
  public void error(final SAXParseException e)
  {
    // nothing yet
  }

  @Override
  public void fatalError(final SAXParseException e)
  {
    // nothing yet
  }

  @Override
  public void startDTD(final String name,
                       final String publicId,
                       final String systemId)
  {
    // nothing yet
  }

  @Override
  public void endDTD()
  {
    // nothing yet
  }

  @Override
  public void startEntity(final String name)
  {
    // nothing yet
  }

  @Override
  public void endEntity(final String name)
  {
    // nothing yet
  }

  @Override
  public void startCDATA()
  {
    // nothing yet
  }

  @Override
  public void endCDATA()
  {
    // nothing yet
  }

  @Override
  public void comment(final char[] textChars,
                      final int start,
                      final int length)
  {
    // strip off comments
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
