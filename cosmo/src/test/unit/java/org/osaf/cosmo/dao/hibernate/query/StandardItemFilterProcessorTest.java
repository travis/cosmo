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
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;


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
    
    public void testNoteItemQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        CollectionItem parent = new CollectionItem();
        filter.setParent(parent);
        filter.setDisplayName("test");
        filter.setIcalUid("icaluid");
        filter.setBody("body");
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from NoteItem i join i.parents parent, TextAttribute ta2 where parent=:parent and i.displayName like :displayName and ta2.item=i and ta2.QName=:ta2qname and ta2.value like :ta2value and i.icalUid=:icaluid", query.getQueryString());
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
        Assert.assertEquals("select i from NoteItem i join i.parents parent, BaseEventStamp es where parent=:parent and es.item=i and ((es.timeRangeIndex.dateStart < case when es.timeRangeIndex.isFloating=true then '20070101T040000' else '20070101T100000Z' end and es.timeRangeIndex.dateEnd > case when es.timeRangeIndex.isFloating=true then '20070101T040000' else '20070101T100000Z' end) or (es.timeRangeIndex.dateStart >= case when es.timeRangeIndex.isFloating=true then '20070101T040000' else '20070101T100000Z' end and es.timeRangeIndex.dateStart < case when es.timeRangeIndex.isFloating=true then '20070201T040000' else '20070201T100000Z' end))", query.getQueryString());
    }

}
