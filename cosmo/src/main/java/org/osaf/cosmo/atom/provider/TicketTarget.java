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
import org.apache.abdera.protocol.server.impl.SimpleTarget;
import org.apache.abdera.util.EntityTag;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Ticket;

/**
 * <p>
 * A target that identifies a particular user preference.
 * </p>
 */
public class TicketTarget extends SimpleTarget implements AuditableTarget{

    private Ticket ticket;
    private CollectionItem collection;

    public TicketTarget(RequestContext request,
                            CollectionItem collection,
                            Ticket ticket) {
        super(TargetType.TYPE_ENTRY, request);
        this.ticket = ticket;
        this.collection = collection;
    }

    // AuditableTarget methods

    public EntityTag getEntityTag() {
        if (ticket == null)
            return null;
        return new EntityTag(ticket.getEntityTag());
    }

    public Date getLastModified() {
        if (ticket == null)
            return null;
        return ticket.getModifiedDate();
    }

    // our methods

    public Ticket getTicket() {
        return ticket;
    }

    public CollectionItem getCollection() {
        return collection;
    }
}
