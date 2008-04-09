/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.osaf.cosmo.dao.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.dao.EventLogDao;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemChangeRecord;
import org.osaf.cosmo.model.ItemChangeRecord.Action;
import org.osaf.cosmo.model.event.EventLogEntry;
import org.osaf.cosmo.model.event.ItemAddedEntry;
import org.osaf.cosmo.model.event.ItemEntry;
import org.osaf.cosmo.model.event.ItemRemovedEntry;
import org.osaf.cosmo.model.event.ItemUpdatedEntry;

/**
 * Mock implementation of {@link UserDao} useful for testing.
 */
public class MockEventLogDao implements EventLogDao {
    private static final Log log = LogFactory.getLog(MockEventLogDao.class);

    ArrayList<EventLogEntry> allEntries = new ArrayList<EventLogEntry>();
    
    public void addEventLogEntries(List<EventLogEntry> entries) {
        for(EventLogEntry entry: entries)
            addEventLogEntry(entry);
    }

    public void addEventLogEntry(EventLogEntry entry) {
        if(entry.getDate()==null)
            entry.setDate(new Date());
        allEntries.add(entry);
    }

    public List<ItemChangeRecord> findChangesForCollection(
            CollectionItem collection, Date start, Date end) {
       ArrayList<ItemChangeRecord> records = new ArrayList<ItemChangeRecord>();
       
       for(EventLogEntry entry: allEntries) {
           
           // match date
           if(entry.getDate().before(start) ||
                   entry.getDate().after(end))
               continue;
           
           ItemEntry itemEntry = (ItemEntry) entry;
           
           // match collection
           if(!collection.equals(itemEntry.getCollection()))
               continue;
           
           
           ItemChangeRecord record = new ItemChangeRecord();
           record.setDate(entry.getDate());
           
           if(entry instanceof ItemAddedEntry) {
               record.setAction(Action.ITEM_ADDED);
           } else if(entry instanceof ItemRemovedEntry) {
               record.setAction(Action.ITEM_REMOVED);
           } else if(entry instanceof ItemUpdatedEntry) {
               record.setAction(Action.ITEM_CHANGED);
           } else {
               throw new IllegalStateException("unrecognized entry type");
           }
           
           record.setItemUuid(itemEntry.getItem().getUid());
           record.setItemDisplayName(itemEntry.getItem().getDisplayName());
           setModifiedBy(record, itemEntry);
           records.add(record);
       }
       
       return records;
    }

    private void setModifiedBy(ItemChangeRecord record, ItemEntry entry) {
        Item item = entry.getItem();
        if(item instanceof ContentItem)
            record.setModifiedBy(((ContentItem) item).getLastModifiedBy());
        
        if(record.getModifiedBy()==null) {
            if(entry.getUser()!=null)
                record.setModifiedBy(entry.getUser().getEmail());
            else
                record.setModifiedBy("ticket: anonymous");
        }
    }
    
    public void destroy() {
        // TODO Auto-generated method stub
        
    }

    public void init() {
        // TODO Auto-generated method stub
        
    }

    

}
