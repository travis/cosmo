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
import java.util.ArrayList;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class HCalendarParser implements CalendarParser {
    private static final Log log = LogFactory.getLog(HCalendarParser.class);
    private static final DocumentBuilderFactory BUILDER_FACTORY =
        DocumentBuilderFactory.newInstance();
    private static final XPath XPATH = XPathFactory.newInstance().newXPath();
    private static final XPathExpression XPATH_VEVENTS;

    static {
        BUILDER_FACTORY.setNamespaceAware(true);

        XPATH_VEVENTS = _compileExpression("//*[@class='vevent']");
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

    private void buildCalendar(Document d,
                               ContentHandler handler)
        throws ParserException {
        // The root class name for hCalendar is "vcalendar". An element with a
        // class name of "vcalendar" is itself called an hCalendar.
        //
        // The root class name for events is "vevent". An element with a class
        // name of "vevent" is itself called an hCalender event.
        //
        // For authoring convenience, both "vevent" and "vcalendar" are
        // treated as root class names for parsing purposes. If a document
        // contains elements with class name "vevent" but not "vcalendar", the
        // entire document has an implied "vcalendar" context.

        // XXX: We assume that the entire document has a single vcalendar
        // context. It is possible that the document contains more than one
        // vcalendar element. In this case, we should probably only process
        // that element and log a warning about skipping the others.

        handler.startCalendar();

        for (Element vevent : findElements(XPATH_VEVENTS, d))
            buildComponent(vevent, handler);

        // XXX: support other "first class components": vjournal, vtodo,
        // vfreebusy, vavailability, vvenue

        handler.endCalendar();
    }

    private void buildComponent(Element e,
                                ContentHandler handler)
        throws ParserException {
        String name = _icalName(e);

        handler.startComponent(name);

        handler.endComponent(name);
    }

    private static String _icalName(Element e) {
        // The basic format of hCalendar is to use iCalendar object/property
        // names in lower-case for class names ...
        return e.getAttribute("class").toUpperCase();
    }
}
