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

import org.osaf.cosmo.model.IcalUidInUseException;

/**
 * An exception indicating that the UID of a submitted calendar resource is
 * already in use within the calendar collection.
 */
public class UidConflictException extends MorseCodeException {

    public UidConflictException(IcalUidInUseException e) {
        super(409, e.getMessage(), e);
    }

    protected void writeContent(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.writeStartElement(NS_MC, "no-uid-conflict");
        writer.writeStartElement(NS_MC, "existing-uuid");
        writer.writeCharacters(((IcalUidInUseException)getCause()).getExistingUid());
        writer.writeEndElement();
        writer.writeStartElement(NS_MC, "conflicting-uuid");
        writer.writeCharacters(((IcalUidInUseException)getCause()).getTestUid());
        writer.writeEndElement();
        writer.writeEndElement();
    }
}
