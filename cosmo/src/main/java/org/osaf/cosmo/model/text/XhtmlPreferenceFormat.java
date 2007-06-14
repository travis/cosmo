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
package org.osaf.cosmo.model.text;

import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.Preference;

/**
 * Parses and formats preferences in XHTML with a custom microformat
 * (yet to be described.)
 */
public class XhtmlPreferenceFormat extends BaseXhtmlFormat
    implements PreferenceFormat {
    private static final Log log =
        LogFactory.getLog(XhtmlPreferenceFormat.class);

    public Preference parse(String source)
        throws ParseException {
        Preference pref = new Preference();

        try {
            StringReader sr = new StringReader(source);
            XMLStreamReader reader = createXmlReader(sr);
            if (! reader.hasNext())
                handleParseException("Source has no XML data", reader);

            reader.nextTag();
            if (! (reader.isStartElement() && isDiv(reader) &&
                   hasClass(reader, "preference")))
                handleParseException("Expected preference root div", reader);
            if (log.isDebugEnabled())
                log.debug("read preference div");

            while (reader.hasNext()) {
                reader.nextTag();
                if (reader.isEndElement())
                    break;

                if (! isSpan(reader))
                    handleParseException("Expected span element within preference div", reader);

                if (hasClass(reader, "key")) {
                    if (log.isDebugEnabled())
                        log.debug("read key span");

                    String key = reader.getElementText();
                    if (StringUtils.isBlank(key))
                        handleParseException("Key span must have non-empty value", reader);
                    pref.setKey(key);
                } else if (hasClass(reader, "value")) {
                    if (log.isDebugEnabled())
                        log.debug("read value span");

                    String value = reader.getElementText();
                    if (StringUtils.isBlank(value))
                        value = "";
                    pref.setValue(value);
                } else {
                    handleParseException("Found span with unrecognized class '" + getClass(reader) + "' within preference div", reader);
                }
            }

            reader.close();
        } catch (XMLStreamException e) {
            handleXmlException("Error reading XML", e);
        }

        return pref;
    }

    public String format(Preference pref) {
        try {
            StringWriter sw = new StringWriter();
            XMLStreamWriter writer = createXmlWriter(sw);

            writer.writeStartElement("div");
            writer.writeAttribute("class", "preference");

            if (pref.getKey() != null) {
                writer.writeStartElement("span");
                writer.writeAttribute("class", "key");
                writer.writeCharacters(pref.getKey());
                writer.writeEndElement();
            }

            if (pref.getValue() != null) {
                writer.writeStartElement("span");
                writer.writeAttribute("class", "value");
                writer.writeCharacters(pref.getValue());
                writer.writeEndElement();
            }

            writer.writeEndElement();
            writer.close();

            return sw.toString();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Error formatting XML", e);
        }
    }
}
