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
package org.osaf.cosmo.mc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.security.CosmoSecurityContext;

/**
 * Provides basic information about the collections in a user's home
 * directory. Represents the Morse Code <em>collection service
 * document</em>.
 *
 * @see CollectionItem
 */
public class CollectionService implements MorseCodeConstants {
    private static final Log log = LogFactory.getLog(CollectionService.class);
    private static final XMLOutputFactory XML_OUTPUT_FACTORY =
        XMLOutputFactory.newInstance();

    private HashSet<CollectionItem> collections;
    private ServiceLocator locator;
    private CosmoSecurityContext securityContext;

    public CollectionService(HomeCollectionItem home,
                             ServiceLocator locator,
                             CosmoSecurityContext securityContext) {
        this.collections = new HashSet<CollectionItem>();
        this.locator = locator;
        this.securityContext = securityContext;

        for (Item child : home.getChildren()) {
            if (child instanceof CollectionItem)
                collections.add((CollectionItem)child);
        }
    }

    public Set<CollectionItem> getCollections() {
        return collections;
    }

    public void writeTo(OutputStream out)
        throws IOException, XMLStreamException {
        XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(out);
        writer.setPrefix(PRE_XML, NS_XML);
        writer.setDefaultNamespace(NS_MC);

        try {
            writer.writeStartDocument();
            writer.writeStartElement(EL_MC_SERVICE);
            writer.writeDefaultNamespace(NS_MC);
            writer.writeAttribute(NS_XML, EL_XML_BASE,
                                  locator.getMorseCodeBase());

            for (CollectionItem collection : collections) {
                writer.writeStartElement(EL_MC_COLLECTION);
                writer.writeAttribute(ATTR_MC_UUID, collection.getUid());
                writer.writeAttribute(ATTR_MC_HREF, href(collection));

                writer.writeStartElement(EL_MC_NAME);
                writer.writeCharacters(collection.getDisplayName());
                writer.writeEndElement();

                for (Ticket ticket : visibleTickets(collection)) {
                    writer.writeStartElement(EL_MC_TICKET);
                    writer.writeAttribute(ATTR_MC_TYPE,
                                          ticket.getType().toString());
                    writer.writeCharacters(ticket.getKey());
                    writer.writeEndElement();
                }

                writer.writeEndElement();
            }

            writer.writeEndElement();
            writer.writeEndDocument();
        } finally {
            writer.close();
        }
    }

    private Set<Ticket> visibleTickets(CollectionItem collection) {
        return securityContext.findVisibleTickets(collection);
    }

    private String href(CollectionItem collection) {
        return locator.getMorseCodeUrl(collection, false);
    }
}
