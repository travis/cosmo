/*
 * Copyright 2006-2008 Open Source Applications Foundation
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
 * An exception that signifies that a client request was bad.
 */
public class BadRequestException extends MorseCodeException {

    private Item item;
    
    /** */
    public BadRequestException(String msg) {
        super(400, msg);
    }
    
    /** */
    public BadRequestException(String msg, Throwable cause) {
        super(400, msg, cause);
    }
    
    /** */
    public BadRequestException(Throwable cause) {
        super(400, cause);
    }

    protected void writeContent(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeStartElement(NS_MC, "bad-request-error");
        if (getMessage() != null)
            writer.writeCharacters(getMessage());
        writer.writeEndElement();
    }
}
