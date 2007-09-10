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
package org.osaf.cosmo.dao.hibernate.query;

import junit.framework.Assert;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import org.hibernate.Query;
import org.osaf.cosmo.dao.hibernate.AbstractHibernateDaoTestCase;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.filter.AttributeFilter;
import org.osaf.cosmo.model.filter.ContentItemFilter;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.model.filter.StampFilter;


/**
 * Test StandardItemQueryBuilder.
 */
public class StandardItemFilterProcessorTest extends AbstractHibernateDaoTestCase {

    StandardItemFilterProcessor queryBuilder = new StandardItemFilterProcessor();
    TimeZoneRegistry registry =
        TimeZoneRegistryFactory.getInstance().createRegistry();
    
    public StandardItemFilterProcessorTest() {
        super();
    }

    public void testUidQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        filter.setUid("abc");
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from Item i where i.uid=:uid", query.getQueryString());
    }
    
    public void testDisplayNameQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        filter.setDisplayName("test");
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from Item i where i.displayName like :displayName", query.getQueryString());
    }
    
    public void testParentQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        CollectionItem parent = new CollectionItem();
        filter.setParent(parent);
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from Item i join i.parents parent where parent=:parent", query.getQueryString());
    }
    
    public void testDisplayNameAndParentQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        CollectionItem parent = new CollectionItem();
        filter.setParent(parent);
        filter.setDisplayName("test");
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from Item i join i.parents parent where parent=:parent and i.displayName like :displayName", query.getQueryString());
    }
    
    public void testContentItemQuery() throws Exception {
        ContentItemFilter filter = new ContentItemFilter();
        CollectionItem parent = new CollectionItem();
        filter.setParent(parent);
        filter.setTriageStatus(TriageStatus.CODE_DONE);
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from ContentItem i join i.parents parent where parent=:parent and i.triageStatus.code=:triageStatus", query.getQueryString());
    
        filter.setTriageStatus(-1);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from ContentItem i join i.parents parent where parent=:parent and i.triageStatus.code is null", query.getQueryString());
        
        filter.setTriageStatus(TriageStatus.CODE_DONE);
        filter.addOrderBy(ContentItemFilter.ORDER_BY_TRIAGE_STATUS_RANK, ItemFilter.ORDER_ASC);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from ContentItem i join i.parents parent where parent=:parent and i.triageStatus.code=:triageStatus order by i.triageStatus.rank", query.getQueryString());
    }
    
    public void testNoteItemQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        CollectionItem parent = new CollectionItem();
        filter.setParent(parent);
        filter.setDisplayName("test");
        filter.setIcalUid("icaluid");
        filter.setBody("body");
        filter.setTriageStatus(TriageStatus.CODE_DONE);
        
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from NoteItem i join i.parents parent, TextAttribute ta2 where parent=:parent and i.displayName like :displayName and ta2.item=i and ta2.QName=:ta2qname and ta2.value like :ta2value and i.triageStatus.code=:triageStatus and i.icalUid=:icaluid", query.getQueryString());
        
        filter = new NoteItemFilter();
        filter.setIsModification(true);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from NoteItem i where i.modifies is not null", query.getQueryString());
       
        filter.setIsModification(false);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from NoteItem i where i.modifies is null", query.getQueryString());
       
        filter.setIsModification(null);
        
        filter.setHasModifications(true);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from NoteItem i where size(i.modifications) > 0", query.getQueryString());
        
        filter.setHasModifications(false);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from NoteItem i where size(i.modifications) = 0", query.getQueryString());
    
        filter =  new NoteItemFilter();
        filter.setMasterNoteItem(new NoteItem());
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from NoteItem i where (i=:masterItem or i.modifies=:masterItem)", query.getQueryString());
    }
    
    public void testEventStampQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        EventStampFilter eventFilter = new EventStampFilter();
        CollectionItem parent = new CollectionItem();
        filter.setParent(parent);
        filter.setDisplayName("test");
        filter.setIcalUid("icaluid");
        filter.setBody("body");
        filter.getStampFilters().add(eventFilter);
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from NoteItem i join i.parents parent, TextAttribute ta2, BaseEventStamp es where parent=:parent and i.displayName like :displayName and ta2.item=i and ta2.QName=:ta2qname and ta2.value like :ta2value and es.item=i and i.icalUid=:icaluid", query.getQueryString());
    
        eventFilter.setIsRecurring(true);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from NoteItem i join i.parents parent, TextAttribute ta2, BaseEventStamp es where parent=:parent and i.displayName like :displayName and ta2.item=i and ta2.QName=:ta2qname and ta2.value like :ta2value and es.item=i and (es.timeRangeIndex.isRecurring=true or i.modifies is not null) and i.icalUid=:icaluid", query.getQueryString());
    }
    
    public void testEventStampTimeRangeQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        EventStampFilter eventFilter = new EventStampFilter();
        Period period = new Period(new DateTime("20070101T100000Z"), new DateTime("20070201T100000Z"));
        eventFilter.setPeriod(period);
        eventFilter.setTimezone(registry.getTimeZone("America/Chicago"));
        
        CollectionItem parent = new CollectionItem();
        filter.setParent(parent);
        filter.getStampFilters().add(eventFilter);
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from NoteItem i join i.parents parent, BaseEventStamp es where parent=:parent and es.item=i and ( (es.timeRangeIndex.isFloating=true and es.timeRangeIndex.dateStart < '20070201T040000' and es.timeRangeIndex.dateEnd > '20070101T040000') or (es.timeRangeIndex.isFloating=false and es.timeRangeIndex.dateStart < '20070201T100000Z' and es.timeRangeIndex.dateEnd > '20070101T100000Z') or (es.timeRangeIndex.dateStart=es.timeRangeIndex.dateEnd and (es.timeRangeIndex.dateStart='20070101T040000' or es.timeRangeIndex.dateStart='20070101T100000Z')))", query.getQueryString());
    }
    
    public void testBasicStampQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        StampFilter missingFilter = new StampFilter();
        missingFilter.setStampClass(EventStamp.class);
        filter.getStampFilters().add(missingFilter);
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from NoteItem i where exists (select s.id from Stamp s where s.item=i and s.class=EventStamp)", query.getQueryString());
        missingFilter.setMissing(true);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from NoteItem i where not exists (select s.id from Stamp s where s.item=i and s.class=EventStamp)", query.getQueryString());
    }
    
    public void testBasicAttributeQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        AttributeFilter missingFilter = new AttributeFilter();
        missingFilter.setQname(new QName("ns","name"));
        filter.getAttributeFilters().add(missingFilter);
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from NoteItem i where exists (select a.id from Attribute a where a.item=i and a.QName=:param0)", query.getQueryString());
        missingFilter.setMissing(true);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from NoteItem i where not exists (select a.id from Attribute a where a.item=i and a.QName=:param0)", query.getQueryString());
    }

}
