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

import java.io.Reader;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Where;

/**
 * Extends {@link ContentItem} to represent a Note item.
 */
@Entity
@DiscriminatorValue("note")
@Where(clause = "isactive=1")
public class NoteItem extends ContentItem {

    public static final QName ATTR_NOTE_BODY = new QName(
            NoteItem.class, "body");
    
    public static final QName ATTR_REMINDER_DATE = new QName(
            NoteItem.class, "reminderDate");
    
    private static final long serialVersionUID = -6100568628972081120L;
    private String icalUid = null;
    
    public NoteItem() {
    }

    // Property accessors
    @Transient
    public String getBody() {
        // body stored as TextAttribute on Item
        TextAttribute bodyAttr = (TextAttribute) getAttribute(ATTR_NOTE_BODY);
        if(bodyAttr!=null)
            return bodyAttr.getValue();
        else
            return null;
    }

    public void setBody(String body) {
        // body stored as TextAttribute on Item
        TextAttribute bodyAttr = (TextAttribute) getAttribute(ATTR_NOTE_BODY);
        if(bodyAttr==null && body!=null) {
            bodyAttr = new TextAttribute(ATTR_NOTE_BODY,body);
            addAttribute(bodyAttr);
        }
        if(body==null)
            removeAttribute(ATTR_NOTE_BODY);
        else
            bodyAttr.setValue(body);
    }
    
    @Transient
    public Date getReminderDate() {
        // reminderDate stored as TimestampAttribute on Item
        TimestampAttribute reminderAttr = (TimestampAttribute) getAttribute(ATTR_REMINDER_DATE);
        if(reminderAttr!=null)
            return reminderAttr.getValue();
        else
            return null;
    }

    public void setReminderDate(Date reminderDate) {
        // reminderDate stored as TimestampAttribute on Item
        TimestampAttribute reminderAttr = (TimestampAttribute) getAttribute(ATTR_REMINDER_DATE);
        if(reminderAttr==null && reminderDate!=null) {
            reminderAttr = new TimestampAttribute(ATTR_REMINDER_DATE, reminderDate);
            addAttribute(reminderAttr);
        }
        
        if(reminderDate==null)
            removeAttribute(ATTR_REMINDER_DATE);
        else
            reminderAttr.setValue(reminderDate);
    }

    public void setBody(Reader body) {
        // body stored as TextAttribute on Item
        TextAttribute bodyAttr = (TextAttribute) getAttribute(ATTR_NOTE_BODY);
        if(bodyAttr==null && body!=null) {
            bodyAttr = new TextAttribute(ATTR_NOTE_BODY,body);
            addAttribute(bodyAttr);
        }
        if(body==null)
            removeAttribute(ATTR_NOTE_BODY);
        else
            bodyAttr.setValue(body);
    }

    @Column(name="icaluid", length=255)
    public String getIcalUid() {
        return icalUid;
    }

    public void setIcalUid(String icalUid) {
        this.icalUid = icalUid;
    }
}
