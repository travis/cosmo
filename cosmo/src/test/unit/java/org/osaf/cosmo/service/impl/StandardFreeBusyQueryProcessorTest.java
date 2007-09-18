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
package org.osaf.cosmo.service.impl;

import java.util.Iterator;

import junit.framework.Assert;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.property.FreeBusy;

import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.dao.hibernate.AbstractHibernateDaoTestCase;
import org.osaf.cosmo.dao.hibernate.CalendarDaoImpl;
import org.osaf.cosmo.dao.hibernate.ContentDaoImpl;
import org.osaf.cosmo.dao.hibernate.UserDaoImpl;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.FreeBusyItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.User;

/**
 * Test StandardFreeBusyQueryProcessorTest
 *
 */
public class StandardFreeBusyQueryProcessorTest extends AbstractHibernateDaoTestCase {

    protected ContentDaoImpl contentDao = null;
    protected CalendarDaoImpl calendarDao = null;
    protected UserDaoImpl userDao = null;
    protected StandardFreeBusyQueryProcessor queryProcessor = null;
    
    protected final String CALENDAR_UID = "calendaruid";

    public StandardFreeBusyQueryProcessorTest() {
        super();
    }
    
    @Override
    protected void onSetUpInTransaction() throws Exception {
        // TODO Auto-generated method stub
        super.onSetUpInTransaction();
        
        queryProcessor = new StandardFreeBusyQueryProcessor();
        queryProcessor.setCalendarDao(calendarDao);
        
        CollectionItem calendar = generateCalendar("testcalendar", "testuser");
        
        calendar.setUid(CALENDAR_UID);
        
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));
        
        contentDao.createCollection(root, calendar);
        
        for (int i = 1; i <= 3; i++) {
            ContentItem event = generateEvent("test" + i + ".ics", "eventwithtimezone"
                    + i + ".ics", "testuser");
            event.setUid(CALENDAR_UID + i);
            contentDao.createContent(calendar, event);
        }
        
        FreeBusyItem fb = generateFreeBusy("test4.ics", "vfreebusy.ics", "testuser");
        fb.setUid(CALENDAR_UID + "4");
        contentDao.createContent(calendar, fb);
        
        clearSession();
    }

    public void testFreeBusyQuery() throws Exception {
        DateTime start = new DateTime("20070507T051500Z");
        DateTime end = new DateTime("200705016T051500Z");
        
        Period period = new Period(start, end);
        
        CollectionItem calendar = contentDao.findCollectionByUid(CALENDAR_UID);
        
        // verify we get resuts from VEVENTS in collection
        VFreeBusy vfb = queryProcessor.generateFreeBusy(calendar, period);
        
        verifyPeriods(vfb, null, "20070508T081500Z/20070508T091500Z,20070509T081500Z/20070509T091500Z,20070510T081500Z/20070510T091500Z,20070511T081500Z/20070511T091500Z,20070512T081500Z/20070512T091500Z,20070513T081500Z/20070513T091500Z,20070514T081500Z/20070514T091500Z,20070515T081500Z/20070515T091500Z");
        verifyPeriods(vfb, FbType.BUSY_TENTATIVE, "20070508T101500Z/20070508T111500Z,20070515T101500Z/20070515T111500Z");
       
        // verify we get resuts from VFREEBUSY in collection
        start = new DateTime("20060101T051500Z");
        end = new DateTime("20060105T051500Z");
        
        period = new Period(start, end);
        
        vfb = queryProcessor.generateFreeBusy(calendar, period);
        
        verifyPeriods(vfb, null, "20060103T100000Z/20060103T120000Z,20060104T100000Z/20060104T120000Z");
        verifyPeriods(vfb, FbType.BUSY_TENTATIVE, "20060102T100000Z/20060102T120000Z");
        verifyPeriods(vfb, FbType.BUSY_UNAVAILABLE, "20060105T010000Z/20060105T020000Z");
    }
    
    
    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    private CollectionItem generateCalendar(String name, String owner) {
        CollectionItem calendar = new CollectionItem();
        calendar.setName(name);
        calendar.setOwner(getUser(userDao, owner));
        
        CalendarCollectionStamp ccs = new CalendarCollectionStamp();
        calendar.addStamp(ccs);
        
        ccs.setDescription("test description");
        ccs.setLanguage("en");
        
        return calendar;
    }

    private NoteItem generateEvent(String name, String file,
            String owner) throws Exception {
        NoteItem event = new NoteItem();
        event.setName(name);
        event.setDisplayName(name);
        event.setOwner(getUser(userDao, owner));
       
        EventStamp evs = new EventStamp();
        event.addStamp(evs);
        evs.setCalendar(CalendarUtils.parseCalendar(helper.getBytes(baseDir + "/" + file)));
       
        return event;
    }
    
    private FreeBusyItem generateFreeBusy(String name, String file,
            String owner) throws Exception {
        FreeBusyItem fb = new FreeBusyItem();
        fb.setName(name);
        fb.setDisplayName(name);
        fb.setOwner(getUser(userDao, owner));
        fb.setFreeBusyCalendar(CalendarUtils.parseCalendar(helper.getBytes(baseDir + "/" + file)));
        
        return fb;
    }
    
    private void verifyPeriods(VFreeBusy vfb, FbType fbtype, String periods) {
        PropertyList props = vfb.getProperties(Property.FREEBUSY);
        FreeBusy fb = null;
        
        for(Iterator it = props.iterator();it.hasNext();) {
            FreeBusy next = (FreeBusy) it.next();
            FbType type = (FbType) next.getParameter(Parameter.FBTYPE);
            if(type==null && fbtype==null) {
                fb = next;
            }
            else if(type != null && type.equals(fbtype)) {
                fb = next;
            }
        }
        
        if(fb==null)
            Assert.fail("periods " + periods + " not in " + vfb.toString());
        
        Assert.assertEquals(periods, fb.getPeriods().toString());
    }
}
