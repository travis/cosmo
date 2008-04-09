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
package org.osaf.cosmo.dao.hibernate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import net.fortuna.ical4j.model.DateTime;

import org.osaf.cosmo.model.ItemChangeRecord;
import org.osaf.cosmo.model.event.EventLogEntry;
import org.osaf.cosmo.model.event.ItemAddedEntry;
import org.osaf.cosmo.model.event.ItemRemovedEntry;
import org.osaf.cosmo.model.event.ItemUpdatedEntry;
import org.osaf.cosmo.model.hibernate.BaseModelObject;
import org.osaf.cosmo.model.hibernate.HibCollectionItem;
import org.osaf.cosmo.model.hibernate.HibEventLogEntry;
import org.osaf.cosmo.model.hibernate.HibNoteItem;
import org.osaf.cosmo.model.hibernate.HibUser;

/**
 * Test EventLogDaoImpl
 */
public class HibernateEventLogDaoTest extends AbstractHibernateDaoTestCase {

    protected EventLogDaoImpl eventLogDao = null;

    public HibernateEventLogDaoTest() {
        super();
    }

    HibUser user;
    HibCollectionItem col1, col2;
    HibNoteItem note;
    
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        user = new HibUser();
        user.setUsername("test");
        setId(user, new Long(1));
        
        col1 = new HibCollectionItem();
        setId(col1, new Long(2));
        col1.setUid("uid1");
        
        col2 = new HibCollectionItem();
        setId(col2, new Long(3));
        col2.setUid("uid2");
        
        note = new HibNoteItem();
        setId(note, new Long(4));
        note.setUid("uid3");
        note.setLastModifiedBy("Test McTester");
        note.setDisplayName("note");
    }
    
    
    public void testEventLogDaoItemAddedEntry() throws Exception {
       ItemAddedEntry entry = new ItemAddedEntry(note, col1);
       entry.setUser(user);
       
       ArrayList<EventLogEntry> entries = new ArrayList<EventLogEntry>();
       entries.add(entry);
       
       eventLogDao.addEventLogEntries(entries);
       
       // verify
       List<HibEventLogEntry> results = session.createQuery("from HibEventLogEntry").list();
       Assert.assertEquals(1, results.size());
       HibEventLogEntry hibEntry = results.get(0);
       
       Assert.assertEquals("user", hibEntry.getAuthType());
       Assert.assertEquals(new Long(1), hibEntry.getAuthId());
       Assert.assertEquals("ItemAdded", hibEntry.getType());
       Assert.assertEquals(new Long(2), hibEntry.getId1());
       Assert.assertEquals(new Long(4), hibEntry.getId2());
       Assert.assertEquals("uid3", hibEntry.getUid1());
       Assert.assertEquals("note", hibEntry.getStrval1());
       Assert.assertEquals("Test McTester", hibEntry.getStrval2());
       
       
    }
    
    
    public void testEventLogDaoItemRemovedEntry() throws Exception {
        ItemRemovedEntry entry = new ItemRemovedEntry(note, col1);
        entry.setUser(user);
        
        ArrayList<EventLogEntry> entries = new ArrayList<EventLogEntry>();
        entries.add(entry);
        
        eventLogDao.addEventLogEntries(entries);
        
        // verify
        List<HibEventLogEntry> results = session.createQuery("from HibEventLogEntry").list();
        Assert.assertEquals(1, results.size());
        HibEventLogEntry hibEntry = results.get(0);
        
        Assert.assertEquals("user", hibEntry.getAuthType());
        Assert.assertEquals(new Long(1), hibEntry.getAuthId());
        Assert.assertEquals("ItemRemoved", hibEntry.getType());
        Assert.assertEquals(new Long(2), hibEntry.getId1());
        Assert.assertEquals(new Long(4), hibEntry.getId2());
        Assert.assertEquals("uid3", hibEntry.getUid1());
        Assert.assertEquals("note", hibEntry.getStrval1());
        Assert.assertEquals("Test McTester", hibEntry.getStrval2());
        
     }
    
    public void testEventLogDaoItemUpdatedEntry() throws Exception {
        // ensure note has multiple parents
        note.addParent(col1);
        note.addParent(col2);
        
        ItemUpdatedEntry entry1 = new ItemUpdatedEntry(note, col1);
        entry1.setUser(user);
        
        ItemUpdatedEntry entry2 = new ItemUpdatedEntry(note, col2);
        entry2.setUser(user);
        
        ArrayList<EventLogEntry> entries = new ArrayList<EventLogEntry>();
        entries.add(entry1);
        entries.add(entry2);
        
        eventLogDao.addEventLogEntries(entries);
        
        // verify
        List<HibEventLogEntry> results = session.createQuery("from HibEventLogEntry order by id1").list();
        Assert.assertEquals(2, results.size());
        HibEventLogEntry hibEntry = results.get(0);
        
        Assert.assertEquals("user", hibEntry.getAuthType());
        Assert.assertEquals(new Long(1), hibEntry.getAuthId());
        Assert.assertEquals("ItemUpdated", hibEntry.getType());
        Assert.assertEquals(new Long(2), hibEntry.getId1());
        Assert.assertEquals(new Long(4), hibEntry.getId2());
        Assert.assertEquals("uid3", hibEntry.getUid1());
        Assert.assertEquals("note", hibEntry.getStrval1());
        Assert.assertEquals("Test McTester", hibEntry.getStrval2());
        
        hibEntry = results.get(1);
        
        Assert.assertEquals("user", hibEntry.getAuthType());
        Assert.assertEquals(new Long(1), hibEntry.getAuthId());
        Assert.assertEquals("ItemUpdated", hibEntry.getType());
        Assert.assertEquals(new Long(3), hibEntry.getId1());
        Assert.assertEquals(new Long(4), hibEntry.getId2());
        Assert.assertEquals("uid3", hibEntry.getUid1());
        Assert.assertEquals("note", hibEntry.getStrval1());
        Assert.assertEquals("Test McTester", hibEntry.getStrval2());
        
     }
    
    
    public void testEventLogDaoQuery() throws Exception {
        ItemAddedEntry entry = new ItemAddedEntry(note, col1);
        entry.setUser(user);
        Date entryDate = new DateTime("20080202T100000Z");
        entry.setDate(entryDate);
        
        ArrayList<EventLogEntry> entries = new ArrayList<EventLogEntry>();
        entries.add(entry);
        
        eventLogDao.addEventLogEntries(entries);
        
        List<ItemChangeRecord> results = eventLogDao.findChangesForCollection(col1, new DateTime("20080202T090000Z"), new DateTime("20080202T110000Z"));
        Assert.assertEquals(1, results.size());
        
        ItemChangeRecord icr = results.get(0);
        Assert.assertEquals("uid3", icr.getItemUuid());
        Assert.assertEquals("note", icr.getItemDisplayName());
        Assert.assertEquals("Test McTester", icr.getModifiedBy());
        Assert.assertEquals(entryDate, icr.getDate());
        Assert.assertEquals(ItemChangeRecord.Action.ITEM_ADDED, icr.getAction());
        
        results = eventLogDao.findChangesForCollection(col1, new DateTime("20080202T090000Z"), new DateTime("20080202T095500Z"));
        Assert.assertEquals(0, results.size());
        
        results = eventLogDao.findChangesForCollection(col1, new DateTime("20080202T100001Z"), new DateTime("20080202T195500Z"));
        Assert.assertEquals(0, results.size());
        
     }
    
    private void setId(BaseModelObject bmo, Long id) throws Exception {
        Method method = BaseModelObject.class.getDeclaredMethod("setId", Long.class);
        method.setAccessible(true);
        method.invoke(bmo, id);
    }

}
