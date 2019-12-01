/*
 * @(#)WikiReader.java 1.00 19/11/19
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

import java.net.URL;
import java.net.MalformedURLException;

public class WikiReader
{
  private WikiReaderHandler handler;

  private WikiReader()
  {
    throw new UnsupportedOperationException("unsupported default constructor");
  }

  public WikiReader(final URL sourceUrl, final URL schemaUrl,
                    final MarkDownParser markDownParser)
    throws ParseException
  {
    handler = new WikiReaderHandler(markDownParser);
    XmlParser.parse(sourceUrl, schemaUrl, handler);
  }

  private static final String TEST_WIKI_URL =
    "file:dewiki-20191020-pages-articles3.xml-p2595941p2920047";

  /**
   * For testing purposes only.
   */
  public static void main(final String argv[])
    throws MalformedURLException, ParseException
  {
    final URL testWikiUrl = new URL(TEST_WIKI_URL);
    final Sentencizer sentencizer = new DummySentencizer();
    final MarkDownHandler markDownHandler = new MarkDownHandler();
    final MarkDownParser markDownParser =
      new MarkDownParserImpl(sentencizer, markDownHandler);
    final WikiReader reader = new WikiReader(testWikiUrl, null, markDownParser);
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
