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
package org.osaf.cosmo.atom.provider;

import java.util.Date;

import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.util.EntityTag;

import org.osaf.cosmo.model.NoteItem;

public class DetachedItemTarget extends BaseItemTarget {

    private NoteItem master;
    private NoteItem occurrence;

    public DetachedItemTarget(RequestContext request,
                              NoteItem master,
                              NoteItem occurrence) {
        this(request, master, occurrence, null, null);
    }

    public DetachedItemTarget(RequestContext request,
                              NoteItem master,
                              NoteItem occurrence,
                              String projection,
                              String format) {
        super(TargetType.TYPE_COLLECTION, request, projection, format);
        this.master = master;
        this.occurrence = occurrence;
    }

    // AuditableTarget methods

    public EntityTag getEntityTag() {
        return master != null ? new EntityTag(master.getEntityTag()) : null;
    }

    public Date getLastModified() {
        return master != null ? master.getModifiedDate() : null;
    }

    // our methods

    public NoteItem getMaster() {
        return master;
    }

    public NoteItem getOccurrence() {
        return occurrence;
    }
}
