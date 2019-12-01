/*
 * @(#)WikiTextParser.java 1.00 30/11/19
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

public class WikiTextParser
{
  private WikiTextHandler handler;

  private WikiTextParser()
  {
    throw new UnsupportedOperationException("unsupported default constructor");
  }

  public WikiTextParser(final MarkDownParser markDownParser)
  {
    handler = new WikiTextHandler(markDownParser);
  }

  public void parseText(final String title, final String revisionId,
                        final String xmlFragment)
    throws ParseException
  {
    handler.setTitle(title);
    handler.setRevisionId(revisionId);
    XmlParser.parse("<text>" + xmlFragment + "</text>", null, handler);
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
