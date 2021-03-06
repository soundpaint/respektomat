/*
 * @(#)ParseException.java 1.00 19/11/19
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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ParseException extends Exception
{
  private static final long serialVersionUID = 8748895015695309559L;

  private final Node location;

  public ParseException()
  {
    this((Node)null);
  }

  public ParseException(final Node location)
  {
    this.location = location;
  }

  public ParseException(final String message)
  {
    this(null, message);
  }

  public ParseException(final Node location, final String message)
  {
    super(message);
    this.location = location;
  }

  public ParseException(final String message, final Throwable cause)
  {
    this(null, message, cause);
  }

  public ParseException(final Node location,
                        final String message, final Throwable cause)
  {
    super(concatMessages(message, cause), cause);
    this.location = location;
  }

  public ParseException(final Throwable cause)
  {
    this((Node)null, cause);
  }

  public ParseException(final Node location, final Throwable cause)
  {
    super(cause.getMessage(), cause);
    this.location = location;
  }

  public Node getLocation()
  {
    return location;
  }

  private static String concatMessages(final String message,
                                       final Throwable cause)
  {
    if (cause == null) {
      return message;
    }
    if ((message == null) || message.isEmpty()) {
      return cause.getMessage();
    }
    final String causeMessage = cause.getMessage();
    if ((causeMessage == null) || causeMessage.isEmpty()) {
      return message;
    }
    return message + ": " + causeMessage;
  }

  private static String formatLocation(final Node location)
  {
    if (location == null) {
      return null;
    }
    final Document document = location.getOwnerDocument();
    final Object inputSourceInfo =
      document.getUserData(XmlParser.KEY_XML_URL);
    final Object columnInfo =
      location.getUserData(XmlParser.KEY_COLUMN_NUMBER);
    final Object lineInfo =
      location.getUserData(XmlParser.KEY_LINE_NUMBER);
    final String strInputSource =
      inputSourceInfo != null ? "" + inputSourceInfo : "";
    final String strColumn = columnInfo != null ? "column " + columnInfo : "";
    final String strLine = lineInfo != null ? "line " + lineInfo : "";
    final String strInputPosition =
      strColumn +
      ((columnInfo != null) && (lineInfo != null) ? ", " : "") +
      strLine;
    final String strLocation =
      strInputPosition + (!strInputPosition.isEmpty() ? " " : "") +
      (!strInputSource.isEmpty() ? "in " : "") +
      strInputSource;
    final String strDocumentPosition =
      "document position: " + document.compareDocumentPosition(location);
    return
      strLocation + (!strLocation.isEmpty() ? ", " : "") + strDocumentPosition;
  }

  public String getMessage()
  {
    final String superMessage = super.getMessage();
    final String formattedLocation = formatLocation(location);
    final String labelledLocation =
      (formattedLocation != null) && !formattedLocation.isEmpty() ?
      "location: " + formattedLocation : "";
    return
      superMessage +
      ((superMessage != null) && !superMessage.isEmpty() &&
       (labelledLocation != null) && !labelledLocation.isEmpty() ? ", " : "") +
      labelledLocation;
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
