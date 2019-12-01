/*
 * @(#)XmlParser.java 1.00 19/11/19
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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlParser
{
  private static final String KEY_LEXICAL_HANDLER =
    "http://xml.org/sax/properties/lexical-handler";

  public static final String KEY_XML_URL = "xml-url";
  public static final String KEY_COLUMN_NUMBER = "column-number";
  public static final String KEY_LINE_NUMBER = "line-number";
  public static final String KEY_PUBLIC_ID = "public-id";
  public static final String KEY_SYSTEM_ID = "system-id";

  public static void parse(final URL xmlUrl, final URL schemaUrl,
                           final DefaultHandler handler)
    throws ParseException
  {
    try {
      final InputSource inputSource = new InputSource(xmlUrl.openStream());
      parse(inputSource, schemaUrl, handler);
    } catch (final IOException e) {
      throw new ParseException("failed loading XML for validation", e);
    }
  }

  public static void parse(final String xml, final URL schemaUrl,
                           final DefaultHandler handler)
    throws ParseException
  {
    final Reader reader = new StringReader(xml);
    final InputSource inputSource = new InputSource(reader);
    parse(inputSource, schemaUrl, handler);
  }

  public static void parse(final InputSource inputSource, final URL schemaUrl,
                           final DefaultHandler handler)
    throws ParseException
  {
    final SAXParser parser = createParser(handler);
    try {
      if (schemaUrl != null) {
        setSchema(inputSource, schemaUrl);
      } else {
        System.out.println("[using no schema]");
      }
      parser.parse(inputSource, handler);
    } catch (final SAXException | IOException e) {
      throw new ParseException("failed parsing XML input", e);
    }
  }

  private static void setSchema(final InputSource inputSource,
                                final URL schemaUrl)
    throws ParseException
  {
    // TODO: Support for XSD 1.1
    // ("http://www.w3.org/XML/XMLSchema/v1.1").
    final String schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;

    final SchemaFactory schemaFactory =
      SchemaFactory.newInstance(schemaLanguage);
    final Schema schema;
    try {
      schema = schemaFactory.newSchema();
    } catch (final SAXException e) {
      throw new ParseException("generating new XSD instance failed", e);
    }
    final Validator validator = schema.newValidator();
    final XsdResourceResolver xsdResourceResolver =
      new XsdResourceResolver(schemaUrl);
    validator.setResourceResolver(xsdResourceResolver);
    final SAXSource saxSource = new SAXSource(inputSource);
    try {
      validator.validate(saxSource);
    } catch (final IOException | SAXException e) {
      throw new ParseException("failed validating XML", e);
    }
  }

  private static SAXParser createParser(final DefaultHandler handler)
    throws ParseException
  {
    final SAXParserFactory factory;
    factory = SAXParserFactory.newInstance();
    final SAXParser parser;
    try {
      parser = factory.newSAXParser();
    } catch (final SAXException | ParserConfigurationException e) {
      throw new ParseException("failed creating SAX parser", e);
    }
    try {
      parser.setProperty(KEY_LEXICAL_HANDLER, handler);
    } catch (final SAXNotRecognizedException | SAXNotSupportedException e) {
      throw new ParseException("failed configuring SAX parser", e);
    }
    return parser;
  }

  private XmlParser()
  {
    throw new UnsupportedOperationException("unsupported default constructor");
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
