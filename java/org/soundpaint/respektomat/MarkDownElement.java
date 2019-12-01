/*
 * @(#)MarkDownElement.java 1.00 19/11/24
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

import java.util.ArrayList;
import java.util.List;

public class MarkDownElement extends MarkDownNode
{
  public enum Type {
    Root(false),
    Text(true),
    H1(true),
    H2(true),
    H3(true),
    H4(true),
    H5(true),
    SingleQuote(true),
    DoubleQuote(true),
    TripleQuote(true),
    Table(false),
    TableHead(false),
    TableBody(false),
    TableCell(false),
    Template(false),
    TemplateType(true),
    TemplateName(true),
    TemplateProperties(false),
    TemplateProperty(false),
    TemplatePropertyKey(false),
    TemplatePropertyValue(false),
    Italic(true),
    Bold(true),
    ExternalLink(false),
    InternalLink(false),
    CategoryLink(false),
    LinkCategory(false),
    FileLink(false),
    LinkUri(true),
    LinkPath(true),
    LinkProperty(true),
    LinkLabel(true),
    Bullet(true);

    private final boolean preserveWhiteSpace;

    private Type()
    {
      throw new UnsupportedOperationException("unsupported default constructor");
    }

    Type(final boolean preserveWhiteSpace)
    {
      this.preserveWhiteSpace = preserveWhiteSpace;
    }

    public boolean preserveWhiteSpace()
    {
      return preserveWhiteSpace;
    }
  };

  private final Type type;
  private final List<MarkDownNode> children;

  protected MarkDownElement(final MarkDownDocument document,
                            final Type type)
  {
    super(document);
    if (type != Type.Root) {
      if (document == null) {
        throw new NullPointerException("document");
      }
    }
    this.type = type;
    children = new ArrayList<MarkDownNode>();
  }

  public void addNode(final MarkDownNode node)
  {
    if (node.document != document) {
      throw new IllegalArgumentException("node's document is not " +
                                         "this element's document");
    }
    children.add(node);
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
