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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * An exception indicating that the collection being published
 * already exists.
 */
public class CollectionExistsException extends MorseCodeException {

    private String uid = null;
    
    public CollectionExistsException(String uid) {
        super(409, "collection with uuid " + uid + " exists");
        this.uid = uid;
    }

    protected void writeContent(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.writeStartElement(NS_MC, "collection-exists");
        writer.writeStartElement(NS_MC, "existing-uuid");
        writer.writeCharacters(uid);
        writer.writeEndElement();
        writer.writeEndElement();
    }
}
