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
package org.osaf.cosmo.model.mock;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Calendar;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.uuid.VersionFourGenerator;
import org.osaf.cosmo.model.AvailabilityItem;
import org.osaf.cosmo.model.BinaryAttribute;
import org.osaf.cosmo.model.CalendarAttribute;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.DecimalAttribute;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.FileItem;
import org.osaf.cosmo.model.FreeBusyItem;
import org.osaf.cosmo.model.IntegerAttribute;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.PasswordRecovery;
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.StringAttribute;
import org.osaf.cosmo.model.TaskStamp;
import org.osaf.cosmo.model.TextAttribute;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.TicketType;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.XmlAttribute;
import org.w3c.dom.Element;

/**
 * EntityFactory implementation that uses mock objects.
 */
public class MockEntityFactory implements EntityFactory {

    private IdentifierGenerator idGenerator = new VersionFourGenerator();
    
    public CollectionItem createCollection() {
        return new MockCollectionItem();
    }

    public NoteItem createNote() {
        return new MockNoteItem();
    }

    public AvailabilityItem createAvailability() {
        return new MockAvailabilityItem();
    }

    public BinaryAttribute createBinaryAttribute(QName qname, byte[] bytes) {
        return new MockBinaryAttribute(qname, bytes);
    }

    public BinaryAttribute createBinaryAttribute(QName qname, InputStream is) {
        return new MockBinaryAttribute(qname, is);
    }

    public CalendarAttribute createCalendarAttribute(QName qname, Calendar cal) {
        return new MockCalendarAttribute(qname, cal);
    }

    public CalendarCollectionStamp createCalendarCollectionStamp(CollectionItem col) {
        return new MockCalendarCollectionStamp(col);
    }

    public CollectionSubscription createCollectionSubscription() {
        return new MockCollectionSubscription();
    }

    public DecimalAttribute createDecimalAttribute(QName qname, BigDecimal bd) {
        return new MockDecimalAttribute(qname, bd);
    }
    
    public XmlAttribute createXMLAttribute(QName qname, Element e) {
        return new MockXmlAttribute(qname, e);
    }

    public EventExceptionStamp createEventExceptionStamp(NoteItem note) {
        return new MockEventExceptionStamp(note);
    }

    public EventStamp createEventStamp(NoteItem note) {
        return new MockEventStamp(note);
    }

    public FileItem createFileItem() {
        return new MockFileItem();
    }

    public FreeBusyItem createFreeBusy() {
        return new MockFreeBusyItem();
    }

    public IntegerAttribute createIntegerAttribute(QName qname, Long longVal) {
        return new MockIntegerAttribute(qname, longVal);
    }

    public MessageStamp createMessageStamp() {
        return new MockMessageStamp();
    }

    public PasswordRecovery createPasswordRecovery(User user, String key) {
        return new MockPasswordRecovery(user, key);
    }

    public Preference createPreference() {
        return new MockPreference();
    }

    public Preference createPreference(String key, String value) {
        return new MockPreference(key, value);
    }

    public QName createQName(String namespace, String localname) {
        return new MockQName(namespace, localname);
    }

    public StringAttribute createStringAttribute(QName qname, String str) {
        return new MockStringAttribute(qname, str);
    }

    public TaskStamp createTaskStamp() {
        return new MockTaskStamp();
    }

    public TextAttribute createTextAttribute(QName qname, Reader reader) {
        return new MockTextAttribute(qname, reader);
    }

    public Ticket createTicket(TicketType type) {
        return new MockTicket(type);
    }

    public TriageStatus createTriageStatus() {
        return new MockTriageStatus();
    }

    public User createUser() {
        return new MockUser();
    }

    public Ticket creatTicket() {
        return new MockTicket();
    }

    public String generateUid() {
        return idGenerator.nextIdentifier().toString();
    }

}
