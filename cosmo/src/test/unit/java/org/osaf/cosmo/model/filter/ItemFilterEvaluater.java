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
package org.osaf.cosmo.model.filter;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;

import org.osaf.cosmo.calendar.EntityConverter;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.CalendarFilterEvaluater;
import org.osaf.cosmo.calendar.query.ComponentFilter;
import org.osaf.cosmo.calendar.query.TimeRangeFilter;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.BaseEventStamp;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.TextAttribute;

/**
 * Provides api for determining if item matches given filter.
 */
public class ItemFilterEvaluater {
    
    public boolean evaulate(Item item, ItemFilter filter) {
        
        if(item==null || filter==null)
            return false;
        
        if(filter.getParent()!=null)
            if(!item.getParents().contains(filter.getParent()))
                return false;
        
        if(filter instanceof NoteItemFilter) 
            if(!handleNoteItemFilter((NoteItemFilter) filter, item))
                return false;
        
        if(filter instanceof ContentItemFilter) 
            if(!handleContentItemFilter((ContentItemFilter) filter, item))
                return false;
    
        if(filter.getDisplayName()!=null)
            if(!handleFilterCriteria(item.getDisplayName(), filter.getDisplayName()))
                return false;
        
        if(filter.getUid()!=null)
            if(!handleFilterCriteria(item.getUid(), filter.getUid()))
                return false;
        
        for(AttributeFilter af: filter.getAttributeFilters())
            if(!handleAttributeFilter(item, af))
                return false;
        
        for(StampFilter sf: filter.getStampFilters())
            if(!handleStampFilter(item, sf))
                return false;
        
        return true;
    }
    
    private boolean handleContentItemFilter(ContentItemFilter filter, Item item) {
        if(! (item instanceof ContentItem))
            return false;
        
        ContentItem content = (ContentItem) item;
        
        if(filter.getTriageStatusCode()!=null)
            if(!handleFilterCriteria(content.getTriageStatus()==null ? null : content.getTriageStatus().getCode(),  filter.getTriageStatusCode()))
                return false;
        
        return true;
            
    }
    
    private boolean handleNoteItemFilter(NoteItemFilter filter, Item item) {
        if(! (item instanceof NoteItem))
            return false;
        
        NoteItem note = (NoteItem) item;
        
        if(filter.getMasterNoteItem()!=null) {
            if(note.getModifies()!=null && !note.getModifies().equals(filter.getMasterNoteItem()))
                return false;
            if(!note.equals(filter.getMasterNoteItem()))
                return false;
        }
        
        if(filter.getIsModification()!=null) {
            if(filter.getIsModification()==true && note.getModifies()==null)
                return false;
            else if(note.getModifies()!=null)
                return false;     
        }
        
        if(filter.getHasModifications()!=null) {
            if(filter.getHasModifications()==true && note.getModifications().size()==0)
                return false;
            else if(note.getModifications().size()>0)
                return false;
        }
        
        if(filter.getIcalUid()!=null)
            if(!handleFilterCriteria(note.getIcalUid(), filter.getIcalUid()))
                return false;
        
        if(filter.getBody()!=null)
            if(!handleFilterCriteria(note.getBody(), filter.getBody()))
                return false;
        
        return true;
            
    }
    
    private boolean handleFilterCriteria(Object val, FilterCriteria criteria) {
       
        if(criteria instanceof EqualsExpression) {
            EqualsExpression exp = (EqualsExpression) criteria;
            if(val==null)
                return false;
            if(exp.isNegated() && val.equals(exp.getValue()))
                return false;
            else if(!val.equals(exp.getValue()))
                return false;
        }
        
        if(criteria instanceof NullExpression) {
            NullExpression exp = (NullExpression) criteria;
            if(exp.isNegated() && val==null)
                return false;
            else if(val!=null)
                return false;
        }
        
        if(criteria instanceof LikeExpression) {
            LikeExpression exp = (LikeExpression) criteria;
            if(val==null)
                return false;
            if(exp.isNegated() && ((String) val).contains((String) exp.getValue()))
                return false;
            else if(!((String) val).contains((String) exp.getValue()))
                return false;
        }
        
        if(criteria instanceof ILikeExpression) {
            ILikeExpression exp = (ILikeExpression) criteria;
            if(val==null)
                return false;
            
            String strVal = ((String) val).toLowerCase();
            String compareVal = ((String) exp.getValue()).toLowerCase();
            
            if(exp.isNegated() && strVal.contains(compareVal))
                return false;
            else if(!strVal.contains(compareVal))
                return false;
        }
        
        return true;
    }
    
    private boolean handleAttributeFilter(Item item, AttributeFilter af) {
        Attribute a = item.getAttribute(af.getQname());
        if(af.isMissing() && a!=null)
            return false;
        else if(af.isMissing() && a==null)
            return true;
        else if(a==null )
            return false;
        
        if(af instanceof TextAttributeFilter)
            if(!handleTextAttributeFilter(a, (TextAttributeFilter) af))
                return false;
        
        return true;
    }
    
    private boolean handleTextAttributeFilter(Attribute a, TextAttributeFilter taf) {
        if(! (a instanceof TextAttribute))
            return false;
        
        TextAttribute ta = (TextAttribute) a;
        
        if(!handleFilterCriteria(ta.getValue(), taf.getValue()))
            return false;
        
        return true;
    }
    
    private boolean handleStampFilter(Item item, StampFilter sf) {
        Stamp s = item.getStamp(sf.getStampClass());
        if(sf.isMissing() && s!=null)
            return false;
        else if(sf.isMissing() && s==null)
            return true;
        else if(s==null )
            return false;
        
        if(s instanceof EventStampFilter)
            if(!handleEventStampFilter(s, (EventStampFilter) sf))
                return false;
        
        return true;
    }
    
    private boolean handleEventStampFilter(Stamp s, EventStampFilter esf) {
        
        BaseEventStamp es = (BaseEventStamp) s;
     
        Calendar cal = EntityConverter.convertNote((NoteItem) s.getItem());
        CalendarFilter cf = getCalendarFilter(esf);
        CalendarFilterEvaluater cfe = new CalendarFilterEvaluater();
        
        if(cfe.evaluate(cal, cf)==false)
            return false;
        
        return true;
    }
    
    private CalendarFilter getCalendarFilter(EventStampFilter esf) {
        ComponentFilter eventFilter = new ComponentFilter(Component.VEVENT);
        eventFilter.setTimeRangeFilter(new TimeRangeFilter(esf.getPeriod().getStart(), esf.getPeriod().getEnd()));
        if(esf.getTimezone()!=null)
            eventFilter.getTimeRangeFilter().setTimezone(esf.getTimezone().getVTimeZone());

        ComponentFilter calFilter = new ComponentFilter(
                net.fortuna.ical4j.model.Calendar.VCALENDAR);
        calFilter.getComponentFilters().add(eventFilter);

        CalendarFilter filter = new CalendarFilter();
        filter.setFilter(calFilter);
        
        return filter;
    }
}
