/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dao.hibernate.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.osaf.cosmo.calendar.Instance;
import org.osaf.cosmo.calendar.InstanceList;
import org.osaf.cosmo.calendar.RecurrenceExpander;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.NoteOccurrence;
import org.osaf.cosmo.model.filter.AttributeFilter;
import org.osaf.cosmo.model.filter.ContentItemFilter;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.model.filter.MissingStampFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.model.filter.StampFilter;
import org.osaf.cosmo.model.filter.TextAttributeFilter;

/**
 * Standard Implementation of <code>ItemFilterProcessor</code>.
 * Translates filter into HQL Query, executes
 * query and processes the results.
 */
public class StandardItemFilterProcessor implements ItemFilterProcessor {
    
    private static final Log log = LogFactory.getLog(StandardItemFilterProcessor.class);
    
    public StandardItemFilterProcessor() {}
    
    
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.hibernate.query.ItemFilterProcessor#processFilter(org.hibernate.Session, org.osaf.cosmo.model.filter.ItemFilter)
     */
    public Set<Item> processFilter(Session session, ItemFilter filter) {
        Query hibQuery = buildQuery(session, filter);
        List<Item> queryResults = hibQuery.list();
        return processResults(queryResults, filter);
    }
    
    /**
     * Build Hibernate Query from ItemFilter using HQL.
     * The query returned is essentially the first pass at
     * retrieving the matched items.  A second pass is required in
     * order determine if any recurring events match a timeRange
     * in the filter.  This is due to the fact that recurring events
     * may have complicated recurrence rules that are extremely 
     * hard to match using HQL.
     * @param session session
     * @param filter item filter
     * @return hibernate query built using HQL
     */
    public Query buildQuery(Session session, ItemFilter filter) {
        StringBuffer selectBuf = new StringBuffer();
        StringBuffer whereBuf = new StringBuffer();
        HashMap<String, Object> params = new HashMap<String, Object>();
        
        if(filter instanceof NoteItemFilter)
            handleNoteItemFilter(selectBuf, whereBuf, params, (NoteItemFilter) filter);
        else if(filter instanceof ContentItemFilter)
            handleContentItemFilter(selectBuf, whereBuf, params, (ContentItemFilter) filter);
        else    
            handleItemFilter(selectBuf, whereBuf, params, filter);
        
        selectBuf.append(whereBuf);
        
        if(log.isDebugEnabled()) {
            log.debug(selectBuf.toString());
        }
        
        Query hqlQuery = session.createQuery(selectBuf.toString());
        
        for(Entry<String, Object> param: params.entrySet())
            hqlQuery.setParameter(param.getKey(), param.getValue());
        
        return hqlQuery;
    }
    
    private void handleItemFilter(StringBuffer selectBuf,
            StringBuffer whereBuf, HashMap<String, Object> params,
            ItemFilter filter) {
        
        if("".equals(selectBuf.toString()))
            selectBuf.append("select i from Item i");
        
        // filter on uid
        if(filter.getUid()!=null) {
            appendWhere(whereBuf, "i.uid=:uid");
            params.put("uid", filter.getUid());
        }
        
        // filter on parent
        if(filter.getParent()!=null) {
            selectBuf.append(" join i.parents parent");
            appendWhere(whereBuf, "parent=:parent");
            params.put("parent", filter.getParent());
        }
        
        if(filter.getDisplayName()!=null) {
            appendWhere(whereBuf, "i.displayName like :displayName");
            params.put("displayName", formatForLike(filter.getDisplayName()));
        }
        
        handleAttributeFilters(selectBuf, whereBuf, params, filter);
        handleStampFilters(selectBuf, whereBuf, params, filter);
        
    }
    
    private void handleAttributeFilters(StringBuffer selectBuf,
            StringBuffer whereBuf, HashMap<String, Object> params,
            ItemFilter filter) {
        for(AttributeFilter attrFilter: filter.getAttributeFilters()) {
            if(attrFilter instanceof TextAttributeFilter)
                handleTextAttributeFilter(selectBuf, whereBuf, params, (TextAttributeFilter) attrFilter);
        }
    }
    
    private void handleTextAttributeFilter(StringBuffer selectBuf,
            StringBuffer whereBuf, HashMap<String, Object> params,
            TextAttributeFilter filter) {
        
        String alias = "ta" + params.size();
        selectBuf.append(", TextAttribute " + alias);
        appendWhere(whereBuf, alias + ".item=i and " + alias +".QName=:" + alias + "qname and " + alias + ".value like :" + alias + "value");
        params.put(alias + "qname", filter.getQname());
        params.put(alias + "value", formatForLike(filter.getValue()));
    }
    
    private void handleStampFilters(StringBuffer selectBuf,
            StringBuffer whereBuf, HashMap<String, Object> params,
            ItemFilter filter) {
        for(StampFilter stampFilter: filter.getStampFilters()) {
            if(stampFilter instanceof EventStampFilter)
                handleEventStampFilter(selectBuf, whereBuf, params, (EventStampFilter) stampFilter);
            else if(stampFilter instanceof MissingStampFilter)
                handleMissingStampFilter(selectBuf, whereBuf, params, (MissingStampFilter) stampFilter);
        }
    }
    
    private void handleMissingStampFilter(StringBuffer selectBuf,
            StringBuffer whereBuf, HashMap<String, Object> params,
            MissingStampFilter filter) {
        
        String toAppend = "not exists (select s.id from Stamp s where s.item=i and s.class="
                + filter.getMissingStampClass().getSimpleName() + ")";
        appendWhere(whereBuf, toAppend);
    }
    
    private void handleEventStampFilter(StringBuffer selectBuf,
            StringBuffer whereBuf, HashMap<String, Object> params,
            EventStampFilter filter) {
        
        selectBuf.append(", BaseEventStamp es");
        appendWhere(whereBuf, "es.item=i");
        
        if(filter.getPeriod()!=null) {
           whereBuf.append(" and ((es.timeRangeIndex.dateStart < case when es.timeRangeIndex.isFloating=true then '" + filter.getFloatStart() + "'");
           whereBuf.append(" else '" + filter.getUTCStart() + "' end and es.timeRangeIndex.dateEnd > case when es.timeRangeIndex.isFloating=true then '" + filter.getFloatStart() + "'");
           whereBuf.append(" else '" + filter.getUTCStart() + "' end) or (es.timeRangeIndex.dateStart >= case when es.timeRangeIndex.isFloating=true then '" + filter.getFloatStart() + "'");
           whereBuf.append(" else '" + filter.getUTCStart() + "' end and es.timeRangeIndex.dateStart < case when es.timeRangeIndex.isFloating=true then '" + filter.getFloatEnd() + "'");
           whereBuf.append(" else '" + filter.getUTCEnd() + "' end))");
        }
    }
    
    private void handleNoteItemFilter(StringBuffer selectBuf,
            StringBuffer whereBuf, HashMap<String, Object> params,
            NoteItemFilter filter) {
        selectBuf.append("select i from NoteItem i");
        handleItemFilter(selectBuf, whereBuf, params, filter);
        handleContentItemFilter(selectBuf, whereBuf, params, filter);
        
        // filter by icaluid
        if(filter.getIcalUid()!=null) {
            appendWhere(whereBuf, "i.icalUid=:icaluid");
            params.put("icaluid", filter.getIcalUid());
        }
        
        // filter modifications
        if(filter.getIsModification()!=null) {
            if(filter.getIsModification().booleanValue()==true)
                appendWhere(whereBuf,"i.modifies is not null");
            else
                appendWhere(whereBuf,"i.modifies is null");
        }
        
        if(filter.getHasModifications()!=null) {
            if(filter.getHasModifications().booleanValue()==true)
                appendWhere(whereBuf,"size(i.modifications) > 0");
            else
                appendWhere(whereBuf,"size(i.modifications) = 0");
        }
    }
    
    private void handleContentItemFilter(StringBuffer selectBuf,
            StringBuffer whereBuf, HashMap<String, Object> params,
            ContentItemFilter filter) {
        
        if("".equals(selectBuf.toString())) {
            selectBuf.append("select i from ContentItem i");
            handleItemFilter(selectBuf, whereBuf, params, filter);
        }
        
        // handle triageStatus filter
        if(filter.getTriageStatus()!=null) {
            appendWhere(whereBuf,"i.triageStatus.code=:triageStatus");
            params.put("triageStatus", filter.getTriageStatus());
        }
    }
    
    
    private void appendWhere(StringBuffer whereBuf, String toAppend) {
        if("".equals(whereBuf.toString()))
            whereBuf.append(" where " + toAppend);
        else
            whereBuf.append(" and " + toAppend);
    }
    
    private String formatForLike(String toFormat) {
        return "%" + toFormat + "%";
    }
    
    /**
     * Because a timeRange query requires two passes: one to get the list
     * of possible events that occur in the range, and one 
     * to expand recurring events if necessary.
     * This is required because we only index a start and end
     * for the entire recurrence series, and expansion is required to determine
     * if the event actually occurs, and to return individual occurences.
     */
    private HashSet<Item> processResults(List<Item> results, ItemFilter itemFilter) {
        boolean hasTimeRangeFilter = false;
        HashSet<Item> processedResults = new HashSet<Item>();
        EventStampFilter eventFilter = (EventStampFilter) itemFilter.getStampFilter(EventStampFilter.class);
        
        if(eventFilter!=null)
            hasTimeRangeFilter = (eventFilter.getPeriod()!=null);
        
        for(Item item: results) {
            
            // If item is not a note, then nothing to do
            if(!(item instanceof NoteItem)) {
                processedResults.add(item);
                continue;
            }
            
            NoteItem note = (NoteItem) item;
            
            // If note is a modification then add both the modification and the 
            // master.
            if(note.getModifies()!=null) {
                processedResults.add(note);
                processedResults.add(note.getModifies());
            } 
            // If filter doesn't have a timeRange, then we are done
            else if(!hasTimeRangeFilter)
                processedResults.add(note);
            else {
                processedResults.addAll(processMasterNote(note, eventFilter));
            }
        }
        
        return processedResults;
    }
    
    private Collection<ContentItem> processMasterNote(NoteItem note,
            EventStampFilter filter) {
        EventStamp eventStamp = EventStamp.getStamp(note);
        ArrayList<ContentItem> results = new ArrayList<ContentItem>();

        // If the event is not recurring, then return the
        // master note.
        if (!eventStamp.isRecurring()) {
            results.add(note);
            return results;
        }

        // Otherwise, expand the recurring item to determine if it actually
        // occurs
        RecurrenceExpander expander = new RecurrenceExpander();
        InstanceList instances = expander.getOcurrences(eventStamp
                .getCalendar(), filter.getPeriod().getStart(), filter
                .getPeriod().getEnd());

        // If recurring event occurs in range, add master
        if (instances.size() > 0)
            results.add(note);
        
        // If were aren't expanding, then return
        if(filter.isExpandRecurringEvents() == false)
            return results;
        
        // Otherwise, add an occurence item for each occurrence
        for (Iterator<Entry<String, Instance>> it = instances.entrySet()
                .iterator(); it.hasNext();) {
            Entry<String, Instance> entry = it.next();

            // Ignore overrides as they are separate items that should have
            // already been added
            if (entry.getValue().isOverridden() == false) {
                results.add(new NoteOccurrence(entry.getValue().getRid(), note));
            }
        }

        return results;
    }

}
