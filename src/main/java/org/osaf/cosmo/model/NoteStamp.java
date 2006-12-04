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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;


/**
 * Represents a Note Stamp.  A note consists of a body (text)
 * and a unique icalendar uid.
 */
@Entity
@DiscriminatorValue("note")
@SecondaryTable(name="note_stamp", pkJoinColumns={
        @PrimaryKeyJoinColumn(name="stampid", referencedColumnName="id")})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NoteStamp extends Stamp implements
        java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6100568628972081120L;
    private String body = null;
    private String icalUid = null;
    
    /** default constructor */
    public NoteStamp() {
    }
    
    @Transient
    public String getType() {
        return "note";
    }
    
    // Property accessors
    @Column(table="note_stamp", name="body")
    @Type(type="text")
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Column(table="note_stamp", name="icaluid")
    public String getIcalUid() {
        return icalUid;
    }

    public void setIcalUid(String icalUid) {
        this.icalUid = icalUid;
    }
    
    /**
     * Return NoteStamp from Item
     * @param item
     * @return NoteStamp from Item
     */
    public static NoteStamp getStamp(Item item) {
        return (NoteStamp) item.getStamp(NoteStamp.class);
    }
    
    public Stamp copy() {
        NoteStamp stamp = new NoteStamp();
        stamp.body = body;
        stamp.icalUid = icalUid;
        return stamp;
    }
}
