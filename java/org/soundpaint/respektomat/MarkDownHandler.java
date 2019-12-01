/*
 * @(#)MarkDownHandler.java 1.00 19/11/21
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

public class MarkDownHandler
{
  private final StringBuilder elementText;
  private final MarkDownParserState state;

  public MarkDownHandler()
  {
    elementText = new StringBuilder();
    state = new MarkDownParserState();
  }

  public void reset()
  {
    state.reset();
    elementText.setLength(0);
  }

  public void appendTextFragment(final String text)
  {
    elementText.append(text);
  }

  public void startElement(final MarkDownElement.Type type)
  {
    state.addText(elementText.toString());
    elementText.setLength(0);
    state.startElement(type);
  }

  public void endElement(final MarkDownElement.Type type)
  {
    state.addText(elementText.toString());
    elementText.setLength(0);
    state.endElement(type);
  }

  public String getPendingText()
  {
    return elementText.toString();
  }

  public String getRenderedText()
  {
    return state.getRenderedText();
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
