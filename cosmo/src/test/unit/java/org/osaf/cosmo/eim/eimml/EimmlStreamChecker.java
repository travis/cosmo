/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.eim.eimml;

import java.io.Reader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Test Case for {@link EimmlStreamWriter}.
 */
public class EimmlStreamChecker implements EimmlConstants {
    private static final Log log =
        LogFactory.getLog(EimmlStreamChecker.class);
    private static final XMLInputFactory XML_INPUT_FACTORY =
        XMLInputFactory.newInstance();

    private XMLStreamReader reader;

    /** */
    public EimmlStreamChecker(Reader in)
        throws XMLStreamException {
        reader = XML_INPUT_FACTORY.createXMLStreamReader(in);
    }

    /** */
    public void nextTag()
        throws XMLStreamException {
        reader.nextTag();
    }

    /** */
    public boolean checkStartElement(QName qn)
        throws XMLStreamException {
        return reader.isStartElement() &&
            reader.getName().equals(qn);
    }

    /** */
    public boolean checkStartElement(String uri,
                                     String name)
        throws XMLStreamException {
        return reader.isStartElement() &&
            reader.getNamespaceURI().equals(uri) &&
            reader.getLocalName().equals(name);
    }

    /** */
    public boolean checkNamespaceCount(int count)
        throws XMLStreamException {
        return reader.getNamespaceCount() == count;
    }

    /** */
    public boolean checkNamespace(int pos,
                                  String prefix,
                                  String uri)
        throws XMLStreamException {
        return reader.isStartElement() &&
            reader.getNamespacePrefix(pos).equals(prefix) &&
            reader.getNamespaceURI(pos).equals(uri);
    }

    /** */
    public boolean checkAttributeCount(int count)
        throws XMLStreamException {
        return reader.getAttributeCount() == count;
    }

    /** */
    public boolean checkAttribute(int pos,
                                  QName qn,
                                  String value)
        throws XMLStreamException {
        return reader.isStartElement() &&
            reader.getAttributeName(pos).equals(qn) &&
            reader.getAttributeValue(pos).equals(value);
    }

    /** */
    public boolean checkAttribute(int pos,
                                  String name,
                                  String value)
        throws XMLStreamException {
        return reader.isStartElement() &&
            reader.getAttributeLocalName(pos).equals(name) &&
            reader.getAttributeValue(pos).equals(value);
    }
}
