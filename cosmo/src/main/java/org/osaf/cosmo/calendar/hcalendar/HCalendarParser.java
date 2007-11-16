/*
 * Copyright 2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.calendar.hcalendar;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.fortuna.ical4j.data.CalendarParser;
import net.fortuna.ical4j.data.ContentHandler;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A {@link CalendarParser} that parses XHTML documents that include
 * calendar data marked up with the hCalendar microformat.
 * <p>
 * The parser treats the entire document as a single "vcalendar" context,
 * ignoring any <code>vcalendar</code> elements and adding all components in
 * the document to a single generated calendar.
 * </p>
 * <p>
 * Since hCalendar does not include product information,
 * the <code>PRODID</code> property is omitted from the generated calendar.
 * The hCalendar profile is supposed to define the iCalendar version that it
 * represents, but it does not, so version 2.0 is assumed.
 * </p>
 * <h3>Supported Components</h3>
 * <p>
 * This parser recognizes only "vevent" components.
 * </p>
 * <h3>Supported Properties</h3>
 * <p>
 * This parser recognizes the following properties:
 * </p>
 * <ul>
 * <li> "dtstart" (required) </li>
 * <li> "dtend" </li>
 * <li> "summary" (required) </li>
 * <li> "uid" </li>
 * <li> "dtstamp" </li>
 * <li> "location" </li>
 * <li> "url" </li>
 * <li> "description" </li>
 * </ul>
 * <p>
 * hCalendar allows for some properties to be represented by nested
 * microformat records, including hCard, adr and geo. This parser does not
 * recognize these records. It simply accumulates the text content of any
 * child elements of the property element and uses the resulting string as
 * the property value.
 * </p>
 * <h4>Date and Date-Time Properties</h4>
 * <p>
 * hCalendar date-time values are formatted according to RFC 3339. There is no
 * representation in this specification for floating date-times. Therefore,
 * this parser will never produce floating date-time values.
 * </p>
 * <p>
 * Some examples in the wild provide date and date-time values in iCalendar
 * format rather than RFC 3339 format. Although not technically legal
 * according to spec, these values are accepted.
 * </p>
 * <p>
 * <strong>Limitations:</strong> The parser converts all date-time values to
 * UTC, losing whatever time zone might have been specified in the hCalendar
 * value.
 * </p>
 * <h3>Supported Parameters</h3>
 * <p>
 * hCalendar does not define attributes, nested elements or other information
 * elements representing parameter data. Therefore, this parser does not
 * set any property parameters except as implied by property value data
 * (e.g. VALUE=DATE-TIME or VALUE=DATE for date-time properties).
 * </p>
 */
public class HCalendarParser implements CalendarParser {
    private static final Log log = LogFactory.getLog(HCalendarParser.class);
    private static final DocumentBuilderFactory BUILDER_FACTORY =
        DocumentBuilderFactory.newInstance();
    private static final XPath XPATH = XPathFactory.newInstance().newXPath();
    private static final XPathExpression XPATH_VEVENTS;
    private static final XPathExpression XPATH_DTSTART;
    private static final XPathExpression XPATH_DTEND;
    private static final XPathExpression XPATH_SUMMARY;
    private static final XPathExpression XPATH_UID;
    private static final XPathExpression XPATH_DTSTAMP;
    private static final XPathExpression XPATH_LOCATION;
    private static final XPathExpression XPATH_URL;
    private static final XPathExpression XPATH_DESCRIPTION;
    private static final String HCAL_DATE_PATTERN = "yyyy-MM-dd";
    private static final SimpleDateFormat HCAL_DATE_FORMAT =
        new SimpleDateFormat(HCAL_DATE_PATTERN);
    private static final String HCAL_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssz";
    private static final SimpleDateFormat HCAL_DATE_TIME_FORMAT =
        new SimpleDateFormat(HCAL_DATE_TIME_PATTERN);

    static {
        BUILDER_FACTORY.setNamespaceAware(true);

        XPATH_VEVENTS = _compileExpression("//*[@class='vevent']");
        XPATH_DTSTART = _compileExpression(".//*[@class='dtstart']");
        XPATH_DTEND = _compileExpression(".//*[@class='dtend']");
        XPATH_SUMMARY = _compileExpression(".//*[@class='summary']");
        XPATH_UID = _compileExpression(".//*[@class='uid']");
        XPATH_DTSTAMP = _compileExpression(".//*[@class='dtstamp']");
        XPATH_LOCATION = _compileExpression(".//*[@class='location']");
        XPATH_URL = _compileExpression(".//*[@class='url']");
        XPATH_DESCRIPTION = _compileExpression(".//*[@class='description']");
    }

    private static XPathExpression _compileExpression(String expr) {
        try {
            return XPATH.compile(expr);
        } catch (XPathException e) {
            throw new RuntimeException("Unable to compile expression '" + expr + "'", e);
        }
    }

    public void parse(InputStream in,
                      ContentHandler handler)
        throws IOException, ParserException {
         parse(new InputSource(in), handler);
    }

    public void parse(Reader in,
                      ContentHandler handler)
        throws IOException, ParserException {
        parse(new InputSource(in), handler);
    }

    private void parse(InputSource in,
                       ContentHandler handler)
        throws IOException, ParserException {
        try {
            Document d = BUILDER_FACTORY.newDocumentBuilder().parse(in);
            buildCalendar(d, handler);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            if (e instanceof SAXParseException) {
                SAXParseException pe = (SAXParseException) e;
                throw new ParserException(e.getMessage(), pe.getLineNumber(), e);
            }
            throw new ParserException(e.getMessage(), -1, e);
        }
    }

    private static NodeList findNodes(XPathExpression expr,
                                      Object context)
        throws ParserException {
        if (log.isDebugEnabled())
            log.debug("Finding nodes for expression '" + expr + "'");
        try {
            return (NodeList) expr.evaluate(context, XPathConstants.NODESET);
        } catch (XPathException e) {
            throw new ParserException("Unable to find nodes", -1, e);
        }
    }

    private static Node findNode(XPathExpression expr,
                                 Object context)
        throws ParserException {
        if (log.isDebugEnabled())
            log.debug("Finding node for expression '" + expr + "'");
        try {
            return (Node) expr.evaluate(context, XPathConstants.NODE);
        } catch (XPathException e) {
            throw new ParserException("Unable to find node", -1, e);
        }
    }

    private static List<Element> findElements(XPathExpression expr,
                                              Object context)
        throws ParserException {
        NodeList nodes = findNodes(expr, context);
        ArrayList<Element> elements = new ArrayList<Element>();
        for (int i=0; i<nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n instanceof Element)
                elements.add((Element) n);
        }
        return elements;
    }

    private static Element findElement(XPathExpression expr,
                                       Object context)
        throws ParserException {
        Node n = findNode(expr, context);
        if (n == null || (! (n instanceof Element)))
            return null;
        return (Element) n;
    }

    private static String getTextContent(Element element)
        throws ParserException {
        try {
            return element.getTextContent().trim();
        } catch (DOMException e) {
            throw new ParserException("Unable to get text content for element " + element.getNodeName(), -1, e);
        }
    }

    private void buildCalendar(Document d,
                               ContentHandler handler)
        throws ParserException {
        // "The root class name for hCalendar is "vcalendar". An element with a
        // class name of "vcalendar" is itself called an hCalendar.
        //
        // The root class name for events is "vevent". An element with a class
        // name of "vevent" is itself called an hCalender event.
        //
        // For authoring convenience, both "vevent" and "vcalendar" are
        // treated as root class names for parsing purposes. If a document
        // contains elements with class name "vevent" but not "vcalendar", the
        // entire document has an implied "vcalendar" context."

        // XXX: We assume that the entire document has a single vcalendar
        // context. It is possible that the document contains more than one
        // vcalendar element. In this case, we should probably only process
        // that element and log a warning about skipping the others.

        handler.startCalendar();

        // no PRODID, as the using application should set that itself

        handler.startProperty(Property.VERSION);
        try { handler.propertyValue(Version.VERSION_2_0.getValue()); } catch (Exception e) {};
        handler.endProperty(Property.VERSION);

        for (Element vevent : findElements(XPATH_VEVENTS, d))
            buildComponent(vevent, handler);

        // XXX: support other "first class components": vjournal, vtodo,
        // vfreebusy, vavailability, vvenue

        handler.endCalendar();
    }

    private void buildComponent(Element element,
                                ContentHandler handler)
        throws ParserException {
        String name = _icalName(element);

        handler.startComponent(name);

        buildProperty(findElement(XPATH_DTSTART, element), Property.DTSTART, handler, true);
        buildProperty(findElement(XPATH_DTEND, element), Property.DTEND, handler);
        buildProperty(findElement(XPATH_SUMMARY, element), Property.SUMMARY, handler, true);
        buildProperty(findElement(XPATH_UID, element), Property.UID, handler);
        buildProperty(findElement(XPATH_DTSTAMP, element), Property.DTSTAMP, handler);
        buildProperty(findElement(XPATH_LOCATION, element), Property.LOCATION, handler);
        buildProperty(findElement(XPATH_URL, element), Property.URL, handler);
        buildProperty(findElement(XPATH_DESCRIPTION, element), Property.DESCRIPTION, handler);

        // XXX: duration, method, category, last-modified, status, class,
        // attendee, contact, organizer

        handler.endComponent(name);
    }

    private void buildProperty(Element element,
                               String propName,
                               ContentHandler handler)
        throws ParserException {
        buildProperty(element, propName, handler, false);
    }

    private void buildProperty(Element element,
                               String propName,
                               ContentHandler handler,
                               boolean required)
        throws ParserException {
        String elementName = _elementName(propName);

        if (element == null) {
            if (! required)
                return;
            throw new ParserException("Property element '" + elementName + "' required", -1);
        }

        String value = null;
        if (element.getLocalName().equals("abbr")) {
            // "If an <abbr> element is used for a property, then the 'title'
            // attribute of the <abbr> element is the value of the property,
            // instead of the contents of the element, which instead provide a
            // human presentable version of the value."
            value = element.getAttribute("title");
            if (StringUtils.isBlank(value))
                throw new ParserException("Abbr element '" + elementName + "' requires a non-empty title", -1);
        } else {
            value = getTextContent(element);
        }

        handler.startProperty(propName);

        // if it's a date property, we have to convert from the
        // hCalendar-formatted date (RFC 3339) to an iCalendar-formatted date
        if (isDateProperty(propName)) {
            try {
                Date date = _icalDate(value);
                value = date.toString();

                if (! (date instanceof DateTime))
                    try { handler.parameter(Parameter.VALUE, Value.DATE.getValue()); } catch (Exception e) {}
            } catch (ParseException e) {
                throw new ParserException("Malformed date value for element '" + elementName + "'", -1, e);
            }
        }

        // XXX: does hCalendar provide for parameters?

        try {
            handler.propertyValue(value);
        } catch (URISyntaxException e) {
            throw new ParserException("Malformed URI value for element '" + elementName + "'", -1, e);
        } catch (ParseException e) {
            throw new ParserException("Malformed value for element '" + elementName + "'", -1, e);
        } catch (IOException e) {
            throw new RuntimeException("Unknown error setting property value for element '" + elementName + "'", e);
        }

        handler.endProperty(propName);
    }

    // "The basic format of hCalendar is to use iCalendar object/property
    // names in lower-case for class names ..."
        
    private static String _icalName(Element element) {
        return element.getAttribute("class").toUpperCase();
    }

    private static String _elementName(String propName) {
        return propName.toLowerCase();
    }

    private static boolean isDateProperty(String name) {
        return (name.equals(Property.DTSTART) ||
                name.equals(Property.DTEND) ||
                name.equals(Property.DTSTAMP));
    }

    private static Date _icalDate(String original)
        throws ParseException {

        // in the real world, some generators use iCalendar formatted
        // date-times
        try {
            return new DateTime(original);
        } catch (Exception e2) {}

        if (original.indexOf('T') == -1)
            return new Date(parseDate(original));

        // XXX: there's no representation for floating date-times in
        // RFC 3339

        java.util.Calendar c = parseDateTime(original);
        DateTime dt = new DateTime(c.getTime());
        // XXX: map java time zone to iCal4j time zone
        dt.setUtc(true);
        return dt;
    }

    private static java.util.Date parseDate(String value)
        throws ParseException {
        return HCAL_DATE_FORMAT.parse(value);
    }

    private static Calendar parseDateTime(String value)
        throws ParseException {
        // the date-time value can represent its time zone in a few different
        // ways. we have to normalize those to match our pattern.

        String tzId = null;
        String normalized = null;

        // 2002-10-09T19:00:00Z
        if (value.charAt(value.length()-1) == 'Z') {
            tzId = "GMT-00:00";
            normalized = value.replace("Z", tzId);
        }
        // 2002-10-10T00:00:00+05:00
        else if (value.indexOf("GMT") == -1 && 
                 (value.charAt(value.length()-6) == '+' ||
                  value.charAt(value.length()-6) == '-')) {
            tzId = "GMT" + value.substring(value.length()-6);
            normalized = value.substring(0, value.length()-6) + tzId;
        }
        // 2002-10-10T00:00:00GMT+05:00
        else {
            tzId = value.substring(value.length()-9);
            normalized = value;
        }

        java.util.TimeZone tz = java.util.TimeZone.getTimeZone(tzId);
        GregorianCalendar c = new GregorianCalendar(tz);

        java.util.Date d = HCAL_DATE_TIME_FORMAT.parse(normalized);
        c.setTime(d);

        return c;
    }
}
