/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.cosmo.model;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A bean encapsulating the information about a ticket used in the
 * bodies of ticket requests and responses.
 *
 * This class does not perform any validation on the ticket info,
 * leaving that responsibility to those objects which manipulate
 * ticket info.
 *
 * Similarly, the class does not know how to convert itself to or from
 * XML.
 *
 * Open Issue: how do we support privileges other than read and write
 * (defined by WebDAV extensions or applications)?
 */
public class Ticket {

    private String id;
    private String owner;
    private String timeout;
    private Boolean read;
    private Boolean write;
    private Date dateCreated;
    private Date dateModified;

    /**
     */
    public Ticket() {
        read = Boolean.FALSE;
        write = Boolean.FALSE;
    }

    /**
     */
    public String getId() {
        return id;
    }

    /**
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     */
    public String getOwner() {
        return owner;
    }

    /**
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     */
    public String getTimeout() {
        return timeout;
    }

    /**
     */
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    /**
     */
    public Boolean isRead() {
        return read;
    }

    /**
     */
    public void setRead(Boolean read) {
        this.read = read;
    }

    /**
     */
    public Boolean isWrite() {
        return write;
    }

    /**
     */
    public void setWrite(Boolean write) {
        this.write = write;
    }

    /**
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     */
    public Date getDateModified() {
        return dateModified;
    }

    /**
     */
    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof Ticket)) {
            return false;
        }
        Ticket it = (Ticket) o;
        return new EqualsBuilder().
            append(owner, it.owner).
            append(timeout, it.timeout).
            append(read, it.read).
            append(write, it.write).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(3, 5).
            append(owner).
            append(timeout).
            append(read).
            append(write).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("id", id).
            append("owner", owner).
            append("timeout", timeout).
            append("read", read).
            append("write", write).
            append("dateCreated", dateCreated).
            append("dateModified", dateModified).
            toString();
    }
}
