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
 * An exception signifying that data sent by a Morse Code client is
 * invalid.
 */
public class ValidationException extends MorseCodeException {
    
    String uid = null;
    
    /** */
    public ValidationException(String uid, String message) {
        super(400, message);
        this.uid = uid;
    }

    /** */
    public ValidationException(String uid, String message,
                               Throwable cause) {
        super(400, message, cause);
        this.uid = uid;
    }
    
    protected void writeContent(XMLStreamWriter writer)
            throws XMLStreamException {
        StringBuffer msgBuffer = new StringBuffer(getMessage());
        Throwable cause = getCause();
        if(cause!=null && cause.getMessage()!=null)
            msgBuffer.append(": " + cause.getMessage());
            
        writer.writeStartElement(NS_MC, "data-validation-error");
        if(uid!=null) {
            writer.writeStartElement(NS_MC, "item-uuid");
            writer.writeCharacters(uid);
            writer.writeEndElement();
        }
        writer.writeStartElement(NS_MC, "message");
        writer.writeCharacters(msgBuffer.toString());
        writer.writeEndElement();
        writer.writeEndElement();
    }
}
