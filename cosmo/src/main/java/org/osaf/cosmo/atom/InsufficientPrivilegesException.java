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
package org.osaf.cosmo.atom;

import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.protocol.error.Error;
import org.osaf.cosmo.model.ItemSecurityException;
import org.osaf.cosmo.security.Permission;

/**
 * An exception indicating that the principal has insufficient 
 * privileges to access an item.
 */

public class InsufficientPrivilegesException extends AtomException {

    private static final QName INSUFFICIENT_PRIVILEGES = new QName(NS_COSMO, "insufficient-privileges");
    private static final QName TARGET_UUID = new QName(NS_COSMO, "target-uuid");
    private static final QName PRIVILEGE = new QName(NS_COSMO, "privilege");
    
    
    public InsufficientPrivilegesException(ItemSecurityException ise) {
        super(403, ise);
    }

    public Document<Error> createDocument(Abdera abdera) {
        Error error = super.createDocument(abdera).getRoot();

        ExtensibleElement insufficientPrivs = error.addExtension(INSUFFICIENT_PRIVILEGES);
        insufficientPrivs.addSimpleExtension(TARGET_UUID,((ItemSecurityException) getCause()).getItem().getUid());
        insufficientPrivs.addSimpleExtension(PRIVILEGE,((ItemSecurityException) getCause()).getPermission()==Permission.READ ? "READ": "WRITE");
        return error.getDocument();
    }
}
