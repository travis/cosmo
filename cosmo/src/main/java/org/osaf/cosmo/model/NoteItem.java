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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VJournal;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import org.osaf.cosmo.calendar.ICalendarUtils;
import org.osaf.cosmo.hibernate.validator.Journal;

/**
 * Extends {@link ContentItem} to represent a Note item.
 */
@Entity
@DiscriminatorValue("note")
public class NoteItem extends ContentItem {

    public static final QName ATTR_NOTE_BODY = new QName(
            NoteItem.class, "body");
    
    public static final QName ATTR_REMINDER_TIME = new QName(
            NoteItem.class, "reminderTime");
    
    public static final QName ATTR_ICALENDAR = new QName(
            NoteItem.class, "icalendar");
    
    private static final long serialVersionUID = -6100568628972081120L;
    private String icalUid = null;
    private Set<NoteItem> modifications = new HashSet<NoteItem>(0);
    private NoteItem modifies = null;
    
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
            return;
        }
        if(body==null)
            removeAttribute(ATTR_NOTE_BODY);
        else
            bodyAttr.setValue(body);
    }
    
    @Transient
    public Date getReminderTime() {
        // reminderDate stored as TimestampAttribute on Item
        TimestampAttribute reminderAttr = (TimestampAttribute) getAttribute(ATTR_REMINDER_TIME);
        if(reminderAttr!=null)
            return reminderAttr.getValue();
        else
            return null;
    }

    public void setReminderTime(Date reminderTime) {
        // reminderDate stored as TimestampAttribute on Item
        TimestampAttribute reminderAttr = (TimestampAttribute) getAttribute(ATTR_REMINDER_TIME);
        if(reminderAttr==null && reminderTime!=null) {
            reminderAttr = new TimestampAttribute(ATTR_REMINDER_TIME, reminderTime);
            addAttribute(reminderAttr);
        }
        
        if(reminderTime==null)
            removeAttribute(ATTR_REMINDER_TIME);
        else
            reminderAttr.setValue(reminderTime);
    }
    
    /**
     * Return the Calendar object containing a VJOURNAL component.
     * @return calendar
     */
    @Transient
    @Journal
    public Calendar getJournalCalendar() {
        // calendar stored as ICalendarAttribute on Item
        ICalendarAttribute calAttr = (ICalendarAttribute) getAttribute(ATTR_ICALENDAR);
        if(calAttr!=null)
            return calAttr.getValue();
        else
            return null;
    }
    
    /**
     * Set the Calendar object containing a VJOURNAL component.  
     * This allows non-standard icalendar properties to be stored 
     * with the note.
     * @param calendar
     */
    public void setJournalCalendar(Calendar calendar) {
        // calendar stored as ICalendarAttribute on Item
        ICalendarAttribute calAttr = (ICalendarAttribute) getAttribute(ATTR_ICALENDAR);
        if(calAttr==null && calendar!=null) {
            calAttr = new ICalendarAttribute(ATTR_ICALENDAR, calendar);
            addAttribute(calAttr);
        }
        
        if(calendar==null)
            removeAttribute(ATTR_ICALENDAR);
        else
            calAttr.setValue(calendar);
    }
    
    /**
     * Return icalendar represntation of NoteItem.  A note is serialized
     * as a VJOURNAL.
     * @return Calendar representation of NoteItem
     */
    @Transient
    public Calendar getCalendar() {
        // Start with existing calendar if present
        Calendar calendar = getJournalCalendar();
        
        // otherwise, start with new calendar
        if (calendar == null)
            calendar = ICalendarUtils.createBaseCalendar(new VJournal(),
                    getIcalUid());
        
        // merge in displayName,body
        VJournal journal = (VJournal) calendar.getComponent(Component.VJOURNAL);
        mergeCalendarProperties(journal);
        
        return calendar;
    }
    
    public void setBody(Reader body) {
        // body stored as TextAttribute on Item
        TextAttribute bodyAttr = (TextAttribute) getAttribute(ATTR_NOTE_BODY);
        if(bodyAttr==null && body!=null) {
            bodyAttr = new TextAttribute(ATTR_NOTE_BODY,body);
            addAttribute(bodyAttr);
            return;
        }
        if(body==null)
            removeAttribute(ATTR_NOTE_BODY);
        else
            bodyAttr.setValue(body);
    }

    @Column(name="icaluid", length=255)
    //@Index(name="idx_icaluid")
    public String getIcalUid() {
        return icalUid;
    }

    public void setIcalUid(String icalUid) {
        this.icalUid = icalUid;
    }
    
    public Item copy() {
        NoteItem copy = new NoteItem();
        copyToItem(copy);
        return copy;
    }
    
    @OneToMany(mappedBy = "modifies", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.DELETE} )
    @BatchSize(size=50)
    //@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    public Set<NoteItem> getModifications() {
        return modifications;
    }

    public void setModifications(Set<NoteItem> modifications) {
        this.modifications = modifications;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiesitemid")
    public NoteItem getModifies() {
        return modifies;
    }
    
    public void setModifies(NoteItem modifies) {
        this.modifies = modifies;
    }

    @Override
    protected void copyToItem(Item item) {
        
        if(!(item instanceof NoteItem))
            return;
        
        super.copyToItem(item);
        
        NoteItem noteItem = (NoteItem) item;
        noteItem.setIcalUid(getIcalUid());
    }
    
    private void mergeCalendarProperties(VJournal journal) {
        //uid = icaluid or uid
        //summary = displayName
        //description = body
        String icalUid = getIcalUid();
        if(icalUid==null)
            icalUid = getUid();
        
        ICalendarUtils.setUid(icalUid, journal);
        ICalendarUtils.setSummary(getDisplayName(), journal);
        ICalendarUtils.setDescription(getBody(), journal);
    }
}
