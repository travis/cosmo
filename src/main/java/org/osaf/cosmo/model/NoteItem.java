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

import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

/**
 * Extends {@link ContentItem} to represent a Note item.
 */
@Entity
@DiscriminatorValue("note")
@Where(clause = "isactive=1")
public class NoteItem extends ContentItem {

    /**
     * 
     */
    private static final long serialVersionUID = -6100568628972081120L;
    private String body = null;
    private String icalUid = null;
    
    public NoteItem() {
    }

    // Property accessors
    @Column(name="body")
    @Type(type="text")
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Column(name="icaluid")
    public String getIcalUid() {
        return icalUid;
    }

    public void setIcalUid(String icalUid) {
        this.icalUid = icalUid;
    }

}
