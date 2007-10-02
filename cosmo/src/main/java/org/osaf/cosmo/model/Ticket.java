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
package org.osaf.cosmo.model;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

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
 */
@Entity
@Table(name="tickets")
public class Ticket extends BaseModelObject implements Comparable<Ticket> {

    /**
     * 
     */
    private static final long serialVersionUID = -3333589463226954251L;
    /** */
    public static final String TIMEOUT_INFINITE = "Infinite";
    /** */
    public static final String PRIVILEGE_READ = "read";
    /** */
    public static final String PRIVILEGE_WRITE = "write";
    /** */
    public static final String PRIVILEGE_FREEBUSY = "freebusy";

    private String key;
    private String timeout;
    private Set<String> privileges;
    private Date created;
    private User owner;
    private Item item;

    /**
     */
    public Ticket() {
        privileges = new HashSet<String>();
    }

    /**
     */
    public Ticket(Ticket.Type type) {
        this();
        setTypePrivileges(type);
    }

    @Column(name = "ticketkey", unique = true, nullable = false, length = 255)
    @NotNull
    @Index(name = "idx_ticketkey")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     */
    @Column(name = "tickettimeout", nullable = false, length=255)
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
    public void setTimeout(Integer timeout) {
        this.timeout = "Second-" + timeout;
    }

    /**
     */
    @CollectionOfElements
    @JoinTable(
            name="ticket_privilege",
            joinColumns = @JoinColumn(name="ticketid")
    )
    @Fetch(FetchMode.JOIN)
    @Column(name="privilege", nullable=false, length=255)
    public Set<String> getPrivileges() {
        return privileges;
    }

    /**
     */
    public void setPrivileges(Set<String> privileges) {
        this.privileges = privileges;
    }

    /**
     * Returns the ticket type if the ticket's privileges match up
     * with one of the predefined types, or <code>null</code>
     * otherwise.
     */
    @Transient
    public Ticket.Type getType() {
        if (privileges.contains(PRIVILEGE_READ)) {
            if (privileges.contains(PRIVILEGE_WRITE))
                return Ticket.Type.READ_WRITE;
            else
                return Ticket.Type.READ_ONLY;
        }
        if (privileges.contains(PRIVILEGE_FREEBUSY))
            return Ticket.Type.FREE_BUSY;
        return null;
    }

    @Transient
    private void setTypePrivileges(Ticket.Type type) {
        for (String p : type.getPrivileges())
            privileges.add(p);
    }

    /**
     */
    @Column(name = "creationdate")
    @org.hibernate.annotations.Type(type="timestamp")
    public Date getCreated() {
        return created;
    }

    /**
     */
    public void setCreated(Date created) {
        this.created = created;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ownerid")
    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     */
    public boolean hasTimedOut() {
        if (timeout == null || timeout.equals(TIMEOUT_INFINITE)) {
            return false;
        }

        int seconds = Integer.parseInt(timeout.substring(7));

        Calendar expiry = Calendar.getInstance();
        expiry.setTime(created);
        expiry.add(Calendar.SECOND, seconds);

        return Calendar.getInstance().after(expiry);
    }

    /**
     * Determines whether or not the ticket is granted on the given
     * item or one of its ancestors.
     */
    public boolean isGranted(Item item) {
        
        if(item==null)
            return false;
        
        for (Ticket ticket : item.getTickets()) {
            if (ticket.equals(this))
                return true;
        }
        
        for(Item parent: item.getParents()) {
            if(isGranted(parent))
                return true;
        }
            
        return false;
    }

    @Transient
    public boolean isReadOnly() {
        Type type = getType();
        return type != null && type.equals(Type.READ_ONLY);
    }

    @Transient
    public boolean isReadWrite() {
        Type type = getType();
        return type != null && type.equals(Type.READ_WRITE);
    }

    @Transient
    public boolean isFreeBusy() {
        Type type = getType();
        return type != null && type.equals(Type.FREE_BUSY);
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof Ticket)) {
            return false;
        }
        Ticket it = (Ticket) o;
        return new EqualsBuilder().
            append(key, it.key).
            append(timeout, it.timeout).
            append(privileges, it.privileges).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(3, 5).
            append(key).
            append(timeout).
            append(privileges).
            toHashCode();
    }

    /**
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(key);
        Type type = getType();
        if (type != null)
            buf.append(" (").append(type).append(")");
        return buf.toString();
    }

    public int compareTo(Ticket t) {
        return key.compareTo(t.getKey());
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="itemid")
    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    /**
     * Represents the security classification of a ticket. A ticket is
     * typed according to its privileges. There are three predefined
     * ticket types: read-only, read-write and free-busy.
     */
    public static class Type {
        public static final String ID_READ_ONLY = "read-only";
        public static final Type READ_ONLY =
            new Type(ID_READ_ONLY,
                     new String[] { PRIVILEGE_READ, PRIVILEGE_FREEBUSY });
        public static final String ID_READ_WRITE = "read-write";
        public static final Type READ_WRITE =
            new Type(ID_READ_WRITE,
                     new String[] { PRIVILEGE_READ, PRIVILEGE_WRITE,
                                    PRIVILEGE_FREEBUSY });
        public static final String ID_FREE_BUSY = "free-busy";
        public static final Type FREE_BUSY =
            new Type(ID_FREE_BUSY, new String[] { PRIVILEGE_FREEBUSY });

        private String id;
        private Set<String> privileges;

        public Type(String id) {
            this.id = id;
            this.privileges = new HashSet<String>();
        }

        public Type(String id,
                    String[] privileges) {
            this(id);
            for (String p : privileges)
                this.privileges.add(p);
        }

        public String getId() {
            return id;
        }

        public Set<String> getPrivileges() {
            return privileges;
        }

        public String toString() {
            return id;
        }

        public boolean equals(Object o) {
            if (! (o instanceof Type))
                return false;
            return id.equals(((Type)o).id);
        }

        public static Ticket.Type createInstance(String id) {
            if (id.equals(ID_READ_ONLY))
                return READ_ONLY;
            if (id.equals(ID_READ_WRITE))
                return READ_WRITE;
            if (id.equals(ID_FREE_BUSY))
                return FREE_BUSY;
            return new Type(id);
        }

        public static boolean isKnownType(String id) {
            return (id.equals(ID_READ_ONLY) ||
                    id.equals(ID_READ_WRITE) ||
                    id.equals(ID_FREE_BUSY));
        }
    }
}
