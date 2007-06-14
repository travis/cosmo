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

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.Ticket;

/**
 * Parses and formats subscriptions in XHTML with a custom microformat
 * (yet to be described.)
 */
public class XhtmlSubscriptionFormat extends BaseXhtmlFormat
    implements SubscriptionFormat {
    private static final Log log =
        LogFactory.getLog(XhtmlSubscriptionFormat.class);

    public CollectionSubscription parse(String source)
        throws ParseException {
        CollectionSubscription sub = new CollectionSubscription();

        try {
            StringReader sr = new StringReader(source);
            XMLStreamReader reader = createXmlReader(sr);
            if (! reader.hasNext())
                handleParseException("Source has no XML data", reader);

            reader.nextTag();
            if (! (reader.isStartElement() && isDiv(reader) &&
                   hasClass(reader, "local-subscription")))
                handleParseException("Expected local-subscription root div", reader);
            if (log.isDebugEnabled())
                log.debug("read local-subscription div");

            while (reader.hasNext()) {
                reader.nextTag();
                if (reader.isEndElement())
                    break;

                if (isSpan(reader)) {
                    if (! hasClass(reader, "name"))
                        handleParseException("Child span of local-subscription div must have class name", reader);
                    if (log.isDebugEnabled())
                        log.debug("read name span");

                    String displayName = reader.getElementText();
                    if (StringUtils.isBlank(displayName))
                        handleParseException("Name span must have non-empty value", reader);
                    sub.setDisplayName(displayName);

                    continue;
                }

                else if (isDiv(reader)) {
                    if (hasClass(reader, "collection")) {
                        if (log.isDebugEnabled())
                            log.debug("read collection div");

                        reader.nextTag();
                        if (! reader.isStartElement() && isSpan(reader))
                            handleParseException("Expected span element within collection div", reader);

                        if (hasClass(reader, "uuid")) {
                            if (log.isDebugEnabled())
                                log.debug("read collection uuid span");

                            String uuid = reader.getElementText();
                            if (StringUtils.isBlank(uuid))
                                handleParseException("Uuid span must have non-empty value", reader);
                            sub.setCollectionUid(uuid);
                        } else {
                            handleParseException("Expected uuid span within collection div", reader);
                        }

                        // advance past </div>
                        reader.nextTag();
                        continue;
                    }

                    else if (hasClass(reader, "ticket")) {
                        if (log.isDebugEnabled())
                            log.debug("read ticket div");

                        reader.nextTag();
                        if (! reader.isStartElement() && isSpan(reader))
                            handleParseException("Expected span element within ticket div", reader);

                        if (hasClass(reader, "key")) {
                            if (log.isDebugEnabled())
                                log.debug("read ticket key span");

                            String key = reader.getElementText();
                            if (StringUtils.isBlank(key))
                                handleParseException("Key span must have non-empty value", reader);
                            sub.setTicketKey(key);
                        } else {
                            handleParseException("Expected key span within ticket div", reader);
                        }

                        // advance past </div>
                        reader.nextTag();
                        continue;
                    }
                }
            }

            reader.close();
        } catch (XMLStreamException e) {
            handleXmlException("Error reading XML", e);
        }

        return sub;
    }

    public String format(CollectionSubscription sub) {
        return format(sub, false, null, false, null);
    }

    public String format(CollectionSubscription sub,
                         CollectionItem collection,
                         Ticket ticket) {
        return format(sub, true, collection, true, ticket);
    }

    private String format(CollectionSubscription sub,
                          boolean isCollectionProvided,
                          CollectionItem collection,
                          boolean isTicketProvided,
                          Ticket ticket) {
        try {
            StringWriter sw = new StringWriter();
            XMLStreamWriter writer = createXmlWriter(sw);

            writer.writeStartElement("div");
            writer.writeAttribute("class", "local-subscription");

            if (sub.getDisplayName() != null) {
                writer.writeStartElement("span");
                writer.writeAttribute("class", "name");
                writer.writeCharacters(sub.getDisplayName());
                writer.writeEndElement();
            }

            if (sub.getCollectionUid() != null) {
                writer.writeStartElement("div");
                writer.writeAttribute("class", "collection");
                writer.writeStartElement("span");
                writer.writeAttribute("class", "uuid");
                writer.writeCharacters(sub.getCollectionUid());
                writer.writeEndElement();
                if (isCollectionProvided) {
                    writer.writeStartElement("span");
                    writer.writeAttribute("class", "exists");
                    writer.writeCharacters(Boolean.valueOf(collection != null).
                                           toString());
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }

            if (sub.getTicketKey() != null) {
                writer.writeStartElement("div");
                writer.writeAttribute("class", "ticket");
                writer.writeStartElement("span");
                writer.writeAttribute("class", "key");
                writer.writeCharacters(sub.getTicketKey());
                writer.writeEndElement();
                if (isTicketProvided) {
                    writer.writeStartElement("span");
                    writer.writeAttribute("class", "exists");
                    writer.writeCharacters(Boolean.valueOf(ticket != null).
                                           toString());
                    writer.writeEndElement();
                }
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
