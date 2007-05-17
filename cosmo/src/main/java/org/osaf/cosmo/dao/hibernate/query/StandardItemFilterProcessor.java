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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.osaf.cosmo.calendar.InstanceList;
import org.osaf.cosmo.calendar.RecurrenceExpander;
import org.osaf.cosmo.model.BaseEventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.filter.AttributeFilter;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.ItemFilter;
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
        }
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
        
        if(filter.getIcalUid()!=null) {
            appendWhere(whereBuf, "i.icalUid=:icaluid");
            params.put("icaluid", filter.getIcalUid());
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
     * of possible events that occur in the range, and one to ensure that
     * any recurring event returned in the list actually occurs in the
     * time range.  This is required because we only index a start and end
     * for the entire recurrence series, and expansion is required to determine
     * if the event actually occurs.
     */
    private HashSet<Item> processResults(List<Item> results, ItemFilter itemFilter) {
        boolean hasTimeRangeFilter = false;
        HashSet<Item> processedResults = new HashSet<Item>();
        EventStampFilter eventFilter = (EventStampFilter) itemFilter.getStampFilter(EventStampFilter.class);
        RecurrenceExpander expander = new RecurrenceExpander();
        
        if(eventFilter!=null)
            hasTimeRangeFilter = (eventFilter.getPeriod()!=null);
        
        for(Item item: results) {
            
            // If item is not a note, then nothing to do
            if(!(item instanceof NoteItem)) {
                processedResults.add(item);
                continue;
            }
            
            NoteItem note = (NoteItem) item;
            
            // If note is a modification, then add the master note
            if(note.getModifies()!=null)
                processedResults.add(note.getModifies());
            // If filter doesn't have a timeRange, then we are done
            else if(!hasTimeRangeFilter)
                processedResults.add(note);
            else {
                BaseEventStamp eventStamp = BaseEventStamp.getStamp(note);
                // If event is not recurring, then we are done
                if(!eventStamp.isRecurring())
                    processedResults.add(note);
                else {
                    // otherwise, determine if event occurs in range
                    InstanceList instances = expander.getOcurrences(eventStamp.getEvent(), eventFilter.getPeriod().getStart(), eventFilter.getPeriod().getEnd());
                    if(instances.size()>0)
                        processedResults.add(note);
                }
            }
        }
        
        return processedResults;
    }

}
