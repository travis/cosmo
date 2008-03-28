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
package org.osaf.cosmo.model.hibernate;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VToDo;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.osaf.cosmo.calendar.ICalendarUtils;
import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.hibernate.validator.Task;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.TaskStamp;


/**
 * Hibernate persistent TaskStamp.
 */
@Entity
@DiscriminatorValue("task")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibTaskStamp extends HibStamp implements
        java.io.Serializable, TaskStamp {

    /**
     * 
     */
    private static final long serialVersionUID = -6197756070431706553L;

    public static final QName ATTR_ICALENDAR = new HibQName(
            TaskStamp.class, "icalendar");
    
    /** default constructor */
    public HibTaskStamp() {
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Stamp#getType()
     */
    public String getType() {
        return "task";
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.TaskStamp#getTaskCalendar()
     */
    @Task
    public Calendar getTaskCalendar() {
        // calendar stored as ICalendarAttribute on Item
        return HibICalendarAttribute.getValue(getItem(), ATTR_ICALENDAR);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.TaskStamp#setTaskCalendar(net.fortuna.ical4j.model.Calendar)
     */
    public void setTaskCalendar(Calendar calendar) {
        // calendar stored as ICalendarAttribute on Item
        HibICalendarAttribute.setValue(getItem(), ATTR_ICALENDAR, calendar);
    }
   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.TaskStamp#getCalendar()
     */
    public Calendar getCalendar() {
        // Start with existing calendar if present
        Calendar calendar = getTaskCalendar();
        
        // otherwise, start with new calendar
        if (calendar == null)
            calendar = ICalendarUtils.createBaseCalendar(new VToDo());
        else
            // use copy when merging calendar with item properties
            calendar = CalendarUtils.copyCalendar(calendar);
        
        // merge in displayName,body
        VToDo task = (VToDo) calendar.getComponent(Component.VTODO);
        mergeCalendarProperties(task);
        
        return calendar;
    }
    
    /**
     * Return TaskStamp from Item
     * @param item
     * @return TaskStamp from Item
     */
    public static TaskStamp getStamp(Item item) {
        return (TaskStamp) item.getStamp(TaskStamp.class);
    }
    
    public Stamp copy() {
        TaskStamp stamp = new HibTaskStamp();
        return stamp;
    }
    
    private void mergeCalendarProperties(VToDo task) {
        //uid = icaluid or uid
        //summary = displayName
        //description = body
        //dtstamp = clientModifiedDate/modifiedDate
        
        NoteItem note = (NoteItem) getItem();
        
        String icalUid = note.getIcalUid();
        if(icalUid==null)
            icalUid = getItem().getUid();
        
        if(note.getClientModifiedDate()!=null)
            ICalendarUtils.setDtStamp(note.getClientModifiedDate(), task);
        else
            ICalendarUtils.setDtStamp(note.getModifiedDate(), task);
        
        ICalendarUtils.setUid(icalUid, task);
        ICalendarUtils.setSummary(note.getDisplayName(), task);
        ICalendarUtils.setDescription(note.getBody(), task);
    }
}
