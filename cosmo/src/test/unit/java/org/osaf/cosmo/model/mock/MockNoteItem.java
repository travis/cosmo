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
package org.osaf.cosmo.model.mock;

import java.io.Reader;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.fortuna.ical4j.model.Calendar;

import org.osaf.cosmo.model.ICalendarItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.QName;

/**
 * Extends {@link ICalendarItem} to represent a Note item.
 */
public class MockNoteItem extends MockICalendarItem implements NoteItem {

    public static final QName ATTR_NOTE_BODY = new MockQName(
            NoteItem.class, "body");
    
    public static final QName ATTR_REMINDER_TIME = new MockQName(
            NoteItem.class, "reminderTime");
    
    private static final long serialVersionUID = -6100568628972081120L;
    
    private static final Set<NoteItem> EMPTY_MODS = Collections
            .unmodifiableSet(new HashSet<NoteItem>(0));

    
    private Set<NoteItem> modifications = new HashSet<NoteItem>(0);
    
    
    private NoteItem modifies = null;
    
    private boolean hasModifications = false;
    
    public MockNoteItem() {
    }

    // Property accessors
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceNoteItem#getBody()
     */
    public String getBody() {
        return MockTextAttribute.getValue(this, ATTR_NOTE_BODY);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceNoteItem#setBody(java.lang.String)
     */
    public void setBody(String body) {
        // body stored as TextAttribute on Item
        MockTextAttribute.setValue(this, ATTR_NOTE_BODY, body);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceNoteItem#setBody(java.io.Reader)
     */
    public void setBody(Reader body) {
        // body stored as TextAttribute on Item
        MockTextAttribute.setValue(this, ATTR_NOTE_BODY, body);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceNoteItem#getReminderTime()
     */
    public Date getReminderTime() {
        return MockTimestampAttribute.getValue(this, ATTR_REMINDER_TIME);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceNoteItem#setReminderTime(java.util.Date)
     */
    public void setReminderTime(Date reminderTime) {
        // reminderDate stored as TimestampAttribute on Item
        MockTimestampAttribute.setValue(this, ATTR_REMINDER_TIME, reminderTime);
    }
    
  
    public Calendar getTaskCalendar() {
        // calendar stored as ICalendarAttribute on Item
        return MockICalendarAttribute.getValue(this, ATTR_ICALENDAR);
    }
    
   
    public void setTaskCalendar(Calendar calendar) {
        // calendar stored as ICalendarAttribute on Item
        MockICalendarAttribute.setValue(this, ATTR_ICALENDAR, calendar);
    }
    
    
    public Item copy() {
        NoteItem copy = new MockNoteItem();
        copyToItem(copy);
        return copy;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceNoteItem#getModifications()
     */
    public Set<NoteItem> getModifications() {
        if(hasModifications)
            return Collections.unmodifiableSet(modifications);
        else
            return EMPTY_MODS;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceNoteItem#addModification(org.osaf.cosmo.model.copy.NoteItem)
     */
    public void addModification(NoteItem mod) {
        modifications.add(mod);
        hasModifications = true;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceNoteItem#removeModification(org.osaf.cosmo.model.copy.NoteItem)
     */
    public boolean removeModification(NoteItem mod) {
        boolean removed = modifications.remove(mod);
        hasModifications = modifications.size()!=0;
        return removed;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceNoteItem#removeAllModifications()
     */
    public void removeAllModifications() {
        modifications.clear();
        hasModifications = false;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceNoteItem#getModifies()
     */
    public NoteItem getModifies() {
        return modifies;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceNoteItem#setModifies(org.osaf.cosmo.model.copy.NoteItem)
     */
    public void setModifies(NoteItem modifies) {
        this.modifies = modifies;
    }
    
    @Override
    public String calculateEntityTag() {
        String uid = getUid() != null ? getUid() : "-";
        String modTime = getModifiedDate() != null ?
            new Long(getModifiedDate().getTime()).toString() : "-";
         
        StringBuffer etag = new StringBuffer(uid + ":" + modTime);
        
        // etag is constructed from self plus modifications
        if(modifies==null) {
            for(NoteItem mod: getModifications()) {
                uid = mod.getUid() != null ? mod.getUid() : "-";
                modTime = mod.getModifiedDate() != null ?
                        new Long(mod.getModifiedDate().getTime()).toString() : "-";
                etag.append("," + uid + ":" + modTime);
            }
        }
      
        return encodeEntityTag(etag.toString().getBytes());
    }
}
