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
package org.osaf.cosmo.atom;

import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.protocol.error.Error;

import org.osaf.cosmo.model.IcalUidInUseException;

/**
 * An exception indicating that the iCalendar UID of a submitted resource is
 * already in use within the targetted collection.
 */

public class UidConflictException extends AtomException {

    private static final QName NO_UID_CONFLICT = new QName(NS_COSMO, "no-uid-conflict");
    private static final QName EXISTING_UUID = new QName(NS_COSMO, "existing-uuid");
    private static final QName CONFLICTING_UUID = new QName(NS_COSMO, "conflicting-uuid");

    public UidConflictException(IcalUidInUseException cause) {
        super(409, cause);
    }

    public Document<Error> createDocument(Abdera abdera) {
        IcalUidInUseException ie = (IcalUidInUseException) getCause();
        Error error = super.createDocument(abdera).getRoot();

        ExtensibleElement noUidConflict = error.addExtension(NO_UID_CONFLICT);
        noUidConflict.addSimpleExtension(EXISTING_UUID, ie.getExistingUid());
        noUidConflict.addSimpleExtension(CONFLICTING_UUID, ie.getTestUid());
        
        return error.getDocument();
    }
}
