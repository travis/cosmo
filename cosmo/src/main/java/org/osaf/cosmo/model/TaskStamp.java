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


/**
 * Represents a Task Stamp.
 */
@Entity
@DiscriminatorValue("task")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TaskStamp extends Stamp implements
        java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6197756070431706553L;

    public static final QName ATTR_ICALENDAR = new QName(
            TaskStamp.class, "icalendar");
    
    /** default constructor */
    public TaskStamp() {
    }

    public String getType() {
        return "task";
    }
    
    /**
     * Return the Calendar object containing a VTODO component.
     * @return calendar
     */
    @Task
    public Calendar getTaskCalendar() {
        // calendar stored as ICalendarAttribute on Item
        ICalendarAttribute calAttr = (ICalendarAttribute) getAttribute(ATTR_ICALENDAR);
        if (calAttr != null)
            return calAttr.getValue();
        else
            return null;
    }
    
    /**
     * Set the Calendar object containing a VOTODO component.
     * This allows non-standard icalendar properties to be stored 
     * with the task.
     * @param calendar
     */
    public void setTaskCalendar(Calendar calendar) {
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
     * Return icalendar representation of task.  A task is serialized
     * as a VOTODO.
     * @return Calendar representation of task
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
        TaskStamp stamp = new TaskStamp();
        return stamp;
    }
    
    private void mergeCalendarProperties(VToDo task) {
        //uid = icaluid or uid
        //summary = displayName
        //description = body
        String icalUid = ((NoteItem)getItem()).getIcalUid();
        if(icalUid==null)
            icalUid = getItem().getUid();
        
        ICalendarUtils.setUid(icalUid, task);
        ICalendarUtils.setSummary(getItem().getDisplayName(), task);
        ICalendarUtils.setDescription(((NoteItem)getItem()).getBody(), task);
    }
}
