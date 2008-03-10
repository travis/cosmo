/*
 * Copyright 2008 Open Source Applications Foundation
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

import org.osaf.cosmo.model.ItemSecurityException;
import org.osaf.cosmo.security.Permission;

/**
 * An exception indicating that the principal does not
 * have sufficient privileges to a resource.
 */
public class InsufficientPrivilegesException extends MorseCodeException {

    public InsufficientPrivilegesException(ItemSecurityException e) {
        super(403, e.getMessage(), e);
    }

    protected void writeContent(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.writeStartElement(NS_MC, "insufficient-privileges");
        writer.writeStartElement(NS_MC, "target-uuid");
        writer.writeCharacters(((ItemSecurityException)getCause()).getItem().getUid());
        writer.writeEndElement();
        writer.writeStartElement(NS_MC, "required-privilege");
        writer.writeCharacters(((ItemSecurityException)getCause()).getPermission()==Permission.READ ? "READ" : "WRITE");
        writer.writeEndElement();
        writer.writeEndElement();
    }
}
