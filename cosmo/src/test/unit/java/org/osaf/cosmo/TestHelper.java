/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.security.Principal;
import java.util.HashSet;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.FileItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.mock.MockEntityFactory;
import org.osaf.cosmo.security.mock.MockAnonymousPrincipal;
import org.osaf.cosmo.security.mock.MockUserPrincipal;
import org.w3c.dom.Document;

/**
 */
public class TestHelper {
    private static final Log log = LogFactory.getLog(TestHelper.class);

    protected static final DocumentBuilderFactory BUILDER_FACTORY =
        DocumentBuilderFactory.newInstance();

    protected static CalendarBuilder calendarBuilder = new CalendarBuilder();

    static int apseq = 0;
    static int cseq = 0;
    static int eseq = 0;
    static int iseq = 0;
    static int lseq = 0;
    static int pseq = 0;
    static int rseq = 0;
    static int sseq = 0;
    static int tseq = 0;
    static int useq = 0;

    private EntityFactory entityFactory = new MockEntityFactory();
    
    public TestHelper() {
    }

    public TestHelper(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    public Calendar makeDummyCalendar() {
        Calendar cal =new Calendar();

        cal.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        cal.getProperties().add(Version.VERSION_2_0);

        return cal;
    }

    public Calendar makeDummyCalendarWithEvent() {
        Calendar cal = makeDummyCalendar();

        VEvent e1 = makeDummyEvent();
        cal.getComponents().add(e1);

        VTimeZone tz1 = TimeZoneRegistryFactory.getInstance().createRegistry().
            getTimeZone(TimeZone.getDefault().getID()).getVTimeZone();
        cal.getComponents().add(tz1);

        return cal;
    }

    public VEvent makeDummyEvent() {
        String serial = new Integer(++eseq).toString();
        String summary = "dummy" + serial;

        // tomorrow
        java.util.Calendar start = java.util.Calendar.getInstance();
        start.add(java.util.Calendar.DAY_OF_MONTH, 1);
        start.set(java.util.Calendar.HOUR_OF_DAY, 9);
        start.set(java.util.Calendar.MINUTE, 30);

        // 1 hour duration
        Dur duration = new Dur(0, 1, 0, 0);
 
        VEvent event = new VEvent(new Date(start.getTime()), duration, summary);
        event.getProperties().add(new Uid(serial));
 
        // add timezone information
        VTimeZone tz = TimeZoneRegistryFactory.getInstance().createRegistry().
            getTimeZone(TimeZone.getDefault().getID()).getVTimeZone();
        String tzValue =
            tz.getProperties().getProperty(Property.TZID).getValue();
        net.fortuna.ical4j.model.parameter.TzId tzParam =
            new net.fortuna.ical4j.model.parameter.TzId(tzValue);
        event.getProperties().getProperty(Property.DTSTART).
            getParameters().add(tzParam);

        // add an alarm for 5 minutes before the event with an xparam
        // on the description
        Dur trigger = new Dur(0, 0, -5, 0);
        VAlarm alarm = new VAlarm(trigger);
        alarm.getProperties().add(Action.DISPLAY);
        Description description = new Description("Meeting at 9:30am");
        XParameter xparam = new XParameter("X-COSMO-TEST-PARAM", "deadbeef");
        description.getParameters().add(xparam);
        alarm.getProperties().add(description);
        alarm.getProperties().add(new Description("Meeting at 9:30am"));
        event.getAlarms().add(alarm);

        // add an x-property with an x-param
        XProperty xprop = new XProperty("X-COSMO-TEST-PROP", "abc123");
        xprop.getParameters().add(xparam);
        event.getProperties().add(xprop);

        return event;
    }

    /**
     */
    public Ticket makeDummyTicket(String timeout) {
        Ticket ticket = entityFactory.creatTicket();
        ticket.setTimeout(timeout);
        ticket.setPrivileges(new HashSet());
        ticket.getPrivileges().add(Ticket.PRIVILEGE_READ);
        return ticket;
    }

    /**
     */
    public Ticket makeDummyTicket(int timeout) {
        return makeDummyTicket("Second-" + timeout);
    }

    /**
     */
    public Ticket makeDummyTicket() {
        return makeDummyTicket(Ticket.TIMEOUT_INFINITE);
    }

    /**
     */
    public Ticket makeDummyTicket(User user) {
        Ticket ticket = makeDummyTicket();
        ticket.setOwner(user);
        ticket.setKey(new Integer(++tseq).toString());
        return ticket;
    }

    /** */
    public User makeDummyUser(String username,
                              String password) {
        if (username == null)
            throw new IllegalArgumentException("username required");
        if (password == null)
            throw new IllegalArgumentException("password required");

        User user = entityFactory.createUser();
        user.setUsername(username);
        user.setFirstName(username);
        user.setLastName(username);
        user.setEmail(username + "@localhost");
        user.setPassword(password);

        return user;
    }

    /** */
    public User makeDummyUser() {
        String serial = new Integer(++useq).toString();
        String username = "dummy" + serial;
        return makeDummyUser(username, username);
    }

    /** */
    public CollectionSubscription
        makeDummySubscription(CollectionItem collection,
                              Ticket ticket) {
        if (collection == null)
            throw new IllegalArgumentException("collection required");
        if (ticket == null)
            throw new IllegalArgumentException("ticket required");

        String serial = new Integer(++sseq).toString();
        String displayName = "dummy sub " + serial;

        CollectionSubscription sub = entityFactory.createCollectionSubscription();
        sub.setDisplayName(displayName);
        sub.setTicketKey(ticket.getKey());
        sub.setCollectionUid(collection.getUid());
        
        return sub;
    }

    /** */
    public CollectionSubscription
        makeDummySubscription(User user) {
        CollectionItem collection = makeDummyCollection(user);
        Ticket ticket = makeDummyTicket(user);

        CollectionSubscription sub = makeDummySubscription(collection, ticket);
        sub.setOwner(user);

        return sub;
    }

    public Preference makeDummyPreference() {
        String serial = new Integer(++pseq).toString();

        Preference pref = entityFactory.createPreference();
        pref.setKey("dummy pref " + serial);
        pref.setValue(pref.getKey());
       
        return pref;
    }

    /**
     */
    public Principal makeDummyUserPrincipal() {
        return new MockUserPrincipal(makeDummyUser());
    }

    /**
     */
    public Principal makeDummyUserPrincipal(String name,
                                            String password) {
        return new MockUserPrincipal(makeDummyUser(name, password));
    }

    /**
     */
    public Principal makeDummyAnonymousPrincipal() {
        String serial = new Integer(++apseq).toString();
        return new MockAnonymousPrincipal("dummy" + serial);
    }

    /**
     */
    public Principal makeDummyRootPrincipal() {
        User user = makeDummyUser();
        user.setAdmin(Boolean.TRUE);
        return new MockUserPrincipal(user);
    }

    /**
     */
    public Document loadXml(String name)
        throws Exception {
        InputStream in = getInputStream(name);
        BUILDER_FACTORY.setNamespaceAware(true);
        DocumentBuilder docBuilder = BUILDER_FACTORY.newDocumentBuilder();
        return docBuilder.parse(in);
    }
    
    public Calendar loadIcs(String name) throws Exception{
        InputStream in = getInputStream(name);
        return calendarBuilder.build(in);
    }

    /** */
    public ContentItem makeDummyContent(User user) {
        String serial = new Integer(++cseq).toString();
        String name = "test content " + serial;

        FileItem content = entityFactory.createFileItem();

        content.setUid(name);
        content.setName(name);
        content.setOwner(user);
        content.setContent("test!".getBytes());
        content.setContentEncoding("UTF-8");
        content.setContentLanguage("en_US");
        content.setContentType("text/plain");
        
        return content;
    }

    public NoteItem makeDummyItem(User user) {
        return makeDummyItem(user, null);
    }

    public NoteItem makeDummyItem(User user,
                                  String name) {
        String serial = new Integer(++iseq).toString();
        if (name == null)
            name = "test item " + serial;

        NoteItem note = entityFactory.createNote();

        note.setUid(name);
        note.setName(name);
        note.setOwner(user);
        note.setIcalUid(serial);
        note.setBody("This is a note. I love notes.");
       
        return note;
    }

    /** */
    public CollectionItem makeDummyCollection(User user) {
        String serial = new Integer(++lseq).toString();
        String name = "test collection " + serial;

        CollectionItem collection = entityFactory.createCollection();
        collection.setUid(serial);
        collection.setName(name);
        collection.setDisplayName(name);
        collection.setOwner(user);
       
        return collection;
    }
    
    public CollectionItem makeDummyCalendarCollection(User user) {
        return makeDummyCalendarCollection(user, null);
    }

    public CollectionItem makeDummyCalendarCollection(User user,
                                                      String name) {
        String serial = new Integer(++lseq).toString();
        if (name == null)
            name = "test calendar collection " + serial;

        CollectionItem collection = entityFactory.createCollection();
        collection.setUid(serial);
        collection.setName(name);
        collection.setDisplayName(name);
        collection.setOwner(user);
       
        collection.addStamp(entityFactory.createCalendarCollectionStamp(collection));

        return collection;
    }

    /** */
    public InputStream getInputStream(String name){
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        return in;
    }
    
    /** */
    public byte[] getBytes(String name) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(in, bos);
        return bos.toByteArray();
    }

    /** */
    public Reader getReader(String name) {
        try {
            byte[] buf = IOUtils.toByteArray(getInputStream(name));
            return new StringReader(new String(buf));
        } catch (IOException e) {
            throw new RuntimeException("error converting input stream to reader", e);
        }
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }
    
    
}
