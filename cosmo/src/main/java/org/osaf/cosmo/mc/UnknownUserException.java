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

/**
 * An exception that signifies that a Morse Code request specified a
 * user that does not appear in storage.
 */
public class UnknownUserException extends MorseCodeException {

    private String username = null;
    
    /** */
    public UnknownUserException(String username) {
        super(404, "user referenced by username " + username + " not found");
        this.username = username;
    }

    protected void writeContent(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeStartElement(NS_MC, "unknown-user");
        writer.writeStartElement(NS_MC, "username");
        writer.writeCharacters(username);
        writer.writeEndElement();
        writer.writeEndElement();
    }
}
