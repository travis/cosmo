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
package org.osaf.cosmo.mc;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.osaf.cosmo.model.Item;

/**
 * An exception that signifies that an item specified in a Morse Code
 * collection request is not a collection.
 */
public class NotCollectionException extends MorseCodeException {

    private Item item;
    
    /** */
    public NotCollectionException(Item item) {
        super(412, "item with uuid " + item.getUid() + " not a collection");
        this.item = item;
    }

    protected void writeContent(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeStartElement(NS_MC, "not-collection");
        writer.writeStartElement(NS_MC, "target-uuid");
        writer.writeCharacters(item.getUid());
        writer.writeEndElement();
        writer.writeEndElement();
    }
}
