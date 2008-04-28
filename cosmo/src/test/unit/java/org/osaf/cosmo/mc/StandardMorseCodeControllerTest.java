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
package org.osaf.cosmo.mc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.id.random.SessionIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.EimRecordSetIterator;
import org.osaf.cosmo.eim.IntegerField;
import org.osaf.cosmo.eim.SimpleEimRecordSetIterator;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.EimSchemaConstants;
import org.osaf.cosmo.eim.schema.contentitem.ContentItemConstants;
import org.osaf.cosmo.eim.schema.text.TriageStatusFormat;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionItemDetails;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.TicketType;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.mock.MockCollectionItem;
import org.osaf.cosmo.model.mock.MockEntityFactory;
import org.osaf.cosmo.model.mock.MockTriageStatus;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.service.impl.StandardContentService;
import org.osaf.cosmo.service.impl.StandardTriageStatusQueryProcessor;
import org.osaf.cosmo.service.impl.StandardUserService;
import org.osaf.cosmo.service.lock.SingleVMLockManager;

/**
 * Test Case for <code>StandardMorseCodeController/code> which uses mock
 * data access objects.
 */
public class StandardMorseCodeControllerTest extends TestCase {
    private static final Log log =
        LogFactory.getLog(StandardMorseCodeControllerTest.class);

    private MockSecurityManager securityManager;
    private StandardContentService contentService;
    private StandardUserService userService;
    private MockCalendarDao calendarDao;
    private MockContentDao contentDao;
    private MockDaoStorage storage;
    private MockUserDao userDao;
    private SingleVMLockManager lockManager;
    private TestHelper testHelper;
    private StandardMorseCodeController mcController;
    private MockEntityFactory entityFactory;
    
    protected String baseDir = "src/test/unit/resources/testdata/";

    /** */
    protected void setUp() throws Exception {
        securityManager = new MockSecurityManager();
        testHelper = new TestHelper();
        storage = new MockDaoStorage();
        calendarDao = new MockCalendarDao(storage);
        contentDao = new MockContentDao(storage);
        contentService = new StandardContentService();
        userDao = new MockUserDao(storage);
        userService = new StandardUserService();
        lockManager = new SingleVMLockManager();
        entityFactory = new MockEntityFactory();
        
        contentService.setCalendarDao(calendarDao);
        contentService.setContentDao(contentDao);
        contentService.setLockManager(lockManager);
        contentService.setTriageStatusQueryProcessor(new StandardTriageStatusQueryProcessor());
        contentService.init();
        
        userService.setContentDao(contentDao);
        userService.setUserDao(userDao);
        userService.setPasswordGenerator(new SessionIdGenerator());
        userService.init();
        
        mcController = new StandardMorseCodeController();
        mcController.setContentService(contentService);
        mcController.setUserService(userService);
        mcController.setSecurityManager(securityManager);
        mcController.setEntityFactory(entityFactory);
    }

    /** */
    public void testPublish() throws Exception {
        User user = testHelper.makeDummyUser("mcuser","password");
        userService.createUser(user);
        
        securityManager.initiateSecurityContext("mcuser","password");
        
        Set<TicketType> types = new HashSet<TicketType>();
        types.add(TicketType.READ_ONLY);
        types.add(TicketType.READ_WRITE);
        
        // Make two recordsets
        List<EimRecordSet> recordSets = new ArrayList<EimRecordSet>();
        
        EimRecordSet recordSet = makeTestRecordSet("1");
        recordSet.addRecord(makeTestRecord("title1"));
        recordSets.add(recordSet);
        
        recordSet = makeTestRecordSet("2");
        recordSet.addRecord(makeTestRecord("title2"));
        recordSets.add(recordSet);
        
        PubRecords pubRecords = new PubRecords(new SimpleEimRecordSetIterator(recordSets.iterator()), "test",null);
        
        PubCollection pubCol = mcController.publishCollection("col1", null, pubRecords, types);
        SyncToken token = pubCol.getToken();
        
        CollectionItem col = (CollectionItem) contentDao.findItemByUid("col1");
        Assert.assertNotNull(col);
        Assert.assertTrue(token.isValid(col));
        Assert.assertFalse(token.hasItemChanged(col));
        Assert.assertEquals(2, col.getChildren().size());
        Assert.assertNotNull(contentDao.findItemByUid("1"));
        Assert.assertNotNull(contentDao.findItemByUid("2"));
        
        Assert.assertEquals(col.getTickets().size(), 2);
    }
    
    public void testUpdate() throws Exception {
        User user = testHelper.makeDummyUser("mcuser","password");
        userService.createUser(user);
        
        HomeCollectionItem root = contentDao.getRootItem(user);
        
        CollectionItem collection = testHelper.makeDummyCollection(user);
        collection.setUid("col1");
        
        contentDao.createCollection(root, collection);
        
        NoteItem note = testHelper.makeDummyItem(user);
        note.setUid("1");
        
        contentDao.createContent(collection, note);
        
        securityManager.initiateSecurityContext("mcuser","password");
        
        // Make two recordsets
        List<EimRecordSet> recordSets = new ArrayList<EimRecordSet>();
        
        EimRecordSet recordSet = makeTestRecordSet("1");
        recordSet.addRecord(makeTestRecord("title1"));
        recordSets.add(recordSet);
        
        recordSet = makeTestRecordSet("2");
        recordSet.addRecord(makeTestRecord("title2"));
        recordSets.add(recordSet);
        
        PubRecords pubRecords = new PubRecords(new SimpleEimRecordSetIterator(recordSets.iterator()), "test",null);
        
        // generate dummy collection to generate dummy sync token
        CollectionItem dummy = testHelper.makeDummyCollection(user);
        ((MockCollectionItem) dummy).setModifiedDate(new Date(1));
        ((MockCollectionItem) dummy).setVersion(1);
        
        try {
            mcController.updateCollection("col1", SyncToken.generate(dummy) , pubRecords);
            Assert.fail("able to update with invalid token");
        } catch (StaleCollectionException col1) {
        }
        
        mcController.updateCollection("col1", SyncToken.generate(collection) , pubRecords);
        CollectionItem col = (CollectionItem) contentDao.findItemByUid("col1");
        Assert.assertNotNull(col);
        Assert.assertEquals(2, col.getChildren().size());
        Assert.assertNotNull(contentDao.findItemByUid("1"));
        Assert.assertNotNull(contentDao.findItemByUid("2"));
    }
    
    public void testSubscribe() throws Exception {
        User user = testHelper.makeDummyUser("mcuser","password");
        userService.createUser(user);
        
        HomeCollectionItem root = contentDao.getRootItem(user);
        
        CollectionItem collection = testHelper.makeDummyCollection(user);
        collection.setUid("col1");
        
        contentDao.createCollection(root, collection);
        
        NoteItem note = testHelper.makeDummyItem(user);
        note.setUid("1");
        
        contentDao.createContent(collection, note);
        
        SubRecords records = mcController.subscribeToCollection("col1");
        
        Assert.assertEquals("col1", records.getUid());
        Assert.assertTrue(records.getToken().isValid(collection));
        
        EimRecordSetIterator it = records.getItemRecordSets();
        
        Assert.assertTrue(it.hasNext());
        
        EimRecordSet recordset = it.next();
        
        Assert.assertEquals("1", recordset.getUuid());
        Assert.assertFalse(it.hasNext());
    }
    
    public void testSync() throws Exception {
        User user = testHelper.makeDummyUser("mcuser","password");
        userService.createUser(user);
        
        HomeCollectionItem root = contentDao.getRootItem(user);
        
        CollectionItem collection = testHelper.makeDummyCollection(user);
        collection.setUid("col1");
        
        contentService.createCollection(root, collection);
        
        NoteItem note = testHelper.makeDummyItem(user);
        note.setUid("1");
        
        contentService.createContent(collection, note);
        
        SyncToken token1 = SyncToken.generate(collection);
        
        // give 100 ms wait to ensure updated lastModified dates
        Thread.sleep(100);
        
        NoteItem note2 = testHelper.makeDummyItem(user);
        note2.setUid("2");
        
        contentService.createContent(collection, note2);
       
        SyncToken token2 = SyncToken.generate(collection);
       
        // Using token1, we should only get 1 recordset for item 2
        SubRecords records = mcController.synchronizeCollection("col1", token1);
        Assert.assertEquals("col1", records.getUid());
        
        EimRecordSetIterator it = records.getItemRecordSets();
        
        Assert.assertTrue(it.hasNext());
        
        EimRecordSet recordset = it.next();
        
        Assert.assertEquals("2", recordset.getUuid());
        Assert.assertFalse(it.hasNext());
        
        // Using token2, we should get no updates
        records = mcController.synchronizeCollection("col1", token2);
        Assert.assertEquals("col1", records.getUid());
        it = records.getItemRecordSets();
        Assert.assertFalse(it.hasNext());
    }
    
    /**
     * Test full cycle (publish->sync-->update) 
     * */
    public void testPublishSyncUpdate() throws Exception {
        User user = testHelper.makeDummyUser("mcuser","password");
        userService.createUser(user);
        
        securityManager.initiateSecurityContext("mcuser","password");
        
        Set<TicketType> types = new HashSet<TicketType>();
        types.add(TicketType.READ_ONLY);
        types.add(TicketType.READ_WRITE);
        
        // Make two recordsets
        List<EimRecordSet> recordSets = new ArrayList<EimRecordSet>();
        
        EimRecordSet recordSet = makeTestRecordSet("1");
        recordSet.addRecord(makeTestRecord("title1"));
        recordSets.add(recordSet);
        
        recordSet = makeTestRecordSet("2");
        recordSet.addRecord(makeTestRecord("title2"));
        recordSets.add(recordSet);
        
        PubRecords pubRecords = new PubRecords(new SimpleEimRecordSetIterator(recordSets.iterator()), "test",null);
        
        PubCollection pubCol = mcController.publishCollection("col1", null, pubRecords, types);
        SyncToken token = pubCol.getToken();
       
        // Now update item outside of morse code
        // Need to let some time pass for timestamps to be different
        Thread.sleep(100);
        Item content = contentDao.findItemByUid("1");
        content.setName("titel_changed");
        contentService.updateContent((ContentItem) content);
        
        // Now sync
        SubRecords subRecords = mcController.synchronizeCollection("col1", token);
        // Should be a single recordset
        Assert.assertTrue(subRecords.getItemRecordSets().hasNext());
        EimRecordSet rs = subRecords.getItemRecordSets().next();
        Assert.assertEquals("1", rs.getUuid());
        
        // Now try to update
        recordSets.clear();
        recordSet = makeTestRecordSet("2");
        recordSet.addRecord(makeTestRecord("title2changed"));
        recordSets.add(recordSet);
        pubRecords = new PubRecords(new SimpleEimRecordSetIterator(recordSets.iterator()), "test",null);
        
        // try with stale token
        try {
            mcController.updateCollection("col1", token, pubRecords);
            Assert.fail("able to update with stale token");
        } catch (StaleCollectionException e) {
            //expected
        }
        
        // try with current token
        // Need to let some time pass for timestamps to be different
        Thread.sleep(100);
        pubCol = mcController.updateCollection("col1", subRecords.getToken(), pubRecords);
        // verify item 2 was updated
        Item content2 = contentDao.findItemByUid("2");
        Assert.assertEquals("title2changed", content2.getDisplayName());
        Assert.assertTrue(content2.getModifiedDate().after(content.getModifiedDate()));
        
        // sync with old token, should get both recordsets
        subRecords = mcController.synchronizeCollection("col1", token);
        Map<String, EimRecordSet> rsMap = getRecordSetMap(subRecords.getItemRecordSets());
        Assert.assertEquals(2, rsMap.size());
        
        // sync with new token, should get no recordsets
        subRecords = mcController.synchronizeCollection("col1", subRecords.getToken());
        Assert.assertFalse(subRecords.getItemRecordSets().hasNext());
    }
    
    /**
     * Usecase:
     * 1. User 1 publishes item x in col 1
     * 2. User 1 publishes item x in col 2
     * Result: x should be editable in both collections
     */
    public void testItemsInMultipleCollectionsNoTicketsSameOwner() throws Exception {
        User user = testHelper.makeDummyUser("mcuser","password");
        userService.createUser(user);
        
        securityManager.initiateSecurityContext("mcuser","password");
        
        Set<TicketType> types = new HashSet<TicketType>();
        types.add(TicketType.READ_ONLY);
        types.add(TicketType.READ_WRITE);
        
        // Make two recordsets
        List<EimRecordSet> recordSets = new ArrayList<EimRecordSet>();
        
        EimRecordSet recordSet = makeTestRecordSet("1");
        recordSet.addRecord(makeTestRecord("title1"));
        recordSets.add(recordSet);
        
        recordSet = makeTestRecordSet("2");
        recordSet.addRecord(makeTestRecord("title2"));
        recordSets.add(recordSet);
        
        // publish col1
        PubRecords pubRecords = new PubRecords(new SimpleEimRecordSetIterator(recordSets.iterator()), "test",null);
        mcController.publishCollection("col1", null, pubRecords, types);
       
        
        // publish col2
        pubRecords = new PubRecords(new SimpleEimRecordSetIterator(recordSets.iterator()), "test",null);
        mcController.publishCollection("col2", null, pubRecords, types);
        
        CollectionItem col = (CollectionItem) contentDao.findItemByUid("col2");
        Assert.assertNotNull(col);
        Assert.assertEquals(2, col.getChildren().size());
        NoteItem n = (NoteItem) col.getChild("1");
        Assert.assertNotNull(n);
        Assert.assertEquals(2, n.getParents().size());
        
        CollectionItemDetails cid = n.getParentDetails(col);
        Assert.assertNotNull(cid);
    }
    
    /**
     * Usecase:
     * 1. User 1 publishes item x in col 1
     * 2. User 2 publishes item x in col 2
     * Result: x should be editable in col1, but since no
     * ticket was provided for col1 when User 2 publishes,
     * SecurityException should be thrown
     */
    public void testItemsInMultipleCollectionsNoTicketsDifferentOwner() throws Exception {
        User user1 = testHelper.makeDummyUser("mcuser1","password");
        userService.createUser(user1);
        
        User user2 = testHelper.makeDummyUser("mcuser2","password");
        userService.createUser(user2);
        
        securityManager.initiateSecurityContext("mcuser1","password");
        
        Set<TicketType> types = new HashSet<TicketType>();
        types.add(TicketType.READ_ONLY);
        types.add(TicketType.READ_WRITE);
        
        // Make two recordsets
        List<EimRecordSet> recordSets = new ArrayList<EimRecordSet>();
        
        EimRecordSet recordSet = makeTestRecordSet("1");
        recordSet.addRecord(makeTestRecord("title1"));
        recordSets.add(recordSet);
        
        recordSet = makeTestRecordSet("2");
        recordSet.addRecord(makeTestRecord("title2"));
        recordSets.add(recordSet);
        
        // publish col1
        PubRecords pubRecords = new PubRecords(new SimpleEimRecordSetIterator(recordSets.iterator()), "test",null);
        mcController.publishCollection("col1", null, pubRecords, types);
        
        Item item = contentDao.findItemByUid("1");
    }
    
    /**
     * Usecase:
     * 1. User 1 publishes item x in col 1
     * 2. User 2 publishes item x in col 2, incluedes RW ticket for col1
     * Result: item x will be editable in col2, with RW ticket associated
     */
    public void testItemsInMultipleCollectionsWithTicketsDifferentOwner() throws Exception {
        User user1 = testHelper.makeDummyUser("mcuser1","password");
        userService.createUser(user1);
        
        User user2 = testHelper.makeDummyUser("mcuser2","password");
        userService.createUser(user2);
        
        User user3 = testHelper.makeDummyUser("mcuser3","password");
        userService.createUser(user3);
        
        securityManager.initiateSecurityContext("mcuser1","password");
        
        Set<TicketType> types = new HashSet<TicketType>();
        types.add(TicketType.READ_ONLY);
        types.add(TicketType.READ_WRITE);
        
        // Make two recordsets
        List<EimRecordSet> recordSets = new ArrayList<EimRecordSet>();
        
        EimRecordSet recordSet = makeTestRecordSet("1");
        recordSet.addRecord(makeTestRecord("title1"));
        recordSets.add(recordSet);
        
        recordSet = makeTestRecordSet("2");
        recordSet.addRecord(makeTestRecord("title2"));
        recordSets.add(recordSet);
        
        // publish col1
        PubRecords pubRecords = new PubRecords(new SimpleEimRecordSetIterator(recordSets.iterator()), "test",null);
        mcController.publishCollection("col1", null, pubRecords, types);
        CollectionItem col1 = (CollectionItem) contentDao.findItemByUid("col1");
        
        Ticket rwTicket = getTicket(col1, false);
        Assert.assertNotNull(rwTicket);
        Set<String> tickets = new HashSet<String>();
        tickets.add(rwTicket.getKey());
        
        // publish col2 using different user, but pass rw ticket
        securityManager.initiateSecurityContext("mcuser2","password");
        pubRecords = new PubRecords(new SimpleEimRecordSetIterator(recordSets.iterator()), "test",null);
        mcController.publishCollection("col2", null, pubRecords, types);
        
        CollectionItem col2 = (CollectionItem) contentDao.findItemByUid("col2");
        Assert.assertNotNull(col2);
        Assert.assertEquals(2, col2.getChildren().size());
        NoteItem n = (NoteItem) col2.getChild("1");
        Assert.assertNotNull(n);
        Assert.assertEquals(2, n.getParents().size());
        
        CollectionItemDetails cid = n.getParentDetails(col2);
        Assert.assertNotNull(cid);
    }
    
    
    /**
     * Usecase:
     * 1. User 1 publishes item x in col 1
     * 2. User 2 publishes item x in col 2, incluedes RW ticket T1 for col1
     * 3. User 3 publishes item x in col 3, includes RW ticket T2 for col2
     * Result: item x will be editable in col2
     *         item x will be editable in col3
     */
    public void testItemsInMultipleCollectionsWithTicketsDifferentOwnerMulitLevelRW()
            throws Exception {
        User user1 = testHelper.makeDummyUser("mcuser1", "password");
        userService.createUser(user1);

        User user2 = testHelper.makeDummyUser("mcuser2", "password");
        userService.createUser(user2);
        
        User user3 = testHelper.makeDummyUser("mcuser3","password");
        userService.createUser(user3);
        
        securityManager.initiateSecurityContext("mcuser1","password");
        
        Set<TicketType> types = new HashSet<TicketType>();
        types.add(TicketType.READ_ONLY);
        types.add(TicketType.READ_WRITE);
        
        // Make two recordsets
        List<EimRecordSet> recordSets = new ArrayList<EimRecordSet>();
        
        EimRecordSet recordSet = makeTestRecordSet("1");
        recordSet.addRecord(makeTestRecord("title1"));
        recordSets.add(recordSet);
        
        recordSet = makeTestRecordSet("2");
        recordSet.addRecord(makeTestRecord("title2"));
        recordSets.add(recordSet);
        
        // publish col1
        PubRecords pubRecords = new PubRecords(new SimpleEimRecordSetIterator(recordSets.iterator()), "test",null);
        mcController.publishCollection("col1", null, pubRecords, types);
        CollectionItem col1 = (CollectionItem) contentDao.findItemByUid("col1");
        
        Ticket rwTicket = getTicket(col1, false);
        Assert.assertNotNull(rwTicket);
        Set<String> tickets = new HashSet<String>();
        tickets.add(rwTicket.getKey());
        
        // publish col2 using different user, but pass rw ticket
        securityManager.initiateSecurityContext("mcuser2","password");
        pubRecords = new PubRecords(new SimpleEimRecordSetIterator(recordSets.iterator()), "test",null);
        mcController.publishCollection("col2", null, pubRecords, types);
        
        CollectionItem col2 = (CollectionItem) contentDao.findItemByUid("col2");
        Assert.assertNotNull(col2);
        Assert.assertEquals(2, col2.getChildren().size());
        NoteItem n = (NoteItem) col2.getChild("1");
        Assert.assertNotNull(n);
        Assert.assertEquals(2, n.getParents().size());
        
        CollectionItemDetails cid = n.getParentDetails(col2);
        Assert.assertNotNull(cid);
       
        // publish col3 using different user, but pass rw ticket for col2
        Ticket rwTicket2 = getTicket(col2, false);
        Assert.assertNotNull(rwTicket2);
        tickets.clear();
        tickets.add(rwTicket2.getKey());
        
        securityManager.initiateSecurityContext("mcuser3","password");
        pubRecords = new PubRecords(new SimpleEimRecordSetIterator(recordSets.iterator()), "test",null);
        mcController.publishCollection("col3", null, pubRecords, types);
        
        CollectionItem col3 = (CollectionItem) contentDao.findItemByUid("col3");
        Assert.assertNotNull(col3);
        Assert.assertEquals(2, col3.getChildren().size());
        n = (NoteItem) col3.getChild("1");
        Assert.assertNotNull(n);
        Assert.assertEquals(3, n.getParents().size());
        
        cid = n.getParentDetails(col3);
        Assert.assertNotNull(cid);
    }
    
    private EimRecordSet makeTestRecordSet(String uuid) {
        EimRecordSet recordSet = new EimRecordSet();
        recordSet.setUuid(uuid);
        return recordSet;
    }
    
    private EimRecordSet makeTestDeletedRecordSet(String uuid) {
        EimRecordSet recordSet = new EimRecordSet();
        recordSet.setUuid(uuid);
        recordSet.setDeleted(true);
        return recordSet;
    }
    
    private EimRecord makeTestRecord(String title) {
        EimRecord record = new EimRecord(EimSchemaConstants.PREFIX_ITEM, EimSchemaConstants.NS_ITEM);

        record.addField(new TextField(ContentItemConstants.FIELD_TITLE, title));

        TriageStatus ts = new MockTriageStatus();
        ts.setCode(TriageStatus.CODE_DONE);
        ts.setRank(new BigDecimal("-12345.67"));
        ts.setAutoTriage(Boolean.TRUE);
        record.addField(new TextField(ContentItemConstants.FIELD_TRIAGE,
                                      TriageStatusFormat.getInstance(new MockEntityFactory()).
                                      format(ts)));

        record.addField(new IntegerField(ContentItemConstants.FIELD_HAS_BEEN_SENT, new Integer(1)));
        record.addField(new IntegerField(ContentItemConstants.FIELD_NEEDS_REPLY, new Integer(0)));
        BigDecimal createdOn =
            new BigDecimal(Calendar.getInstance().getTime().getTime());
        record.addField(new DecimalField(ContentItemConstants.FIELD_CREATED_ON, createdOn));
        record.addField(new TextField("Phish", "The Lizzards"));

        return record;
    }
    
    // get either read-only or read-write ticket from item
    private Ticket getTicket(Item item, boolean readOnly) {
        for(Ticket t: item.getTickets()) {
            if(t.isReadOnly() && readOnly)
                return t;
            else if(!readOnly && t.isReadWrite())
                return t;
        }
        return null;
    }
    
    private Map<String, EimRecordSet> getRecordSetMap(EimRecordSetIterator it) throws Exception {
        HashMap<String, EimRecordSet> map = new HashMap<String, EimRecordSet>();
        while(it.hasNext()) {
            EimRecordSet rs = it.next();
            map.put(rs.getUuid(), rs);
        }
        return map;
    }
}
