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
package org.osaf.cosmo.eim.eimml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.CollectionRecord;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EventRecord;
import org.osaf.cosmo.eim.ItemRecord;
import org.osaf.cosmo.eim.MailMessageRecord;
import org.osaf.cosmo.eim.NoteRecord;
import org.osaf.cosmo.eim.TaskRecord;
import org.osaf.cosmo.util.DateUtil;

/**
 * Builds an EIM model from an EIMML document.
 */
public class EimmlBuilder implements EimmlConstants {
    private static final Log log = LogFactory.getLog(EimmlBuilder.class);
    private XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    /**
     * Parses an EIMML document from the specified input stream and
     * builds a list of EIM records.
     *
     * @param in the input stream to read from
     *
     * @return a list of EIM records
     * @throws EimmlParseException
     * @throws IOException
     */
    public List<EimRecord> build(InputStream in)
        throws IOException {
        ArrayList<EimRecord> records = new ArrayList<EimRecord>();

        XMLStreamReader reader = null;
        try {
            reader = inputFactory.createXMLStreamReader(in);
            if (! reader.hasNext())
                throw new EimmlParseException("No input data to read");

            readAllRecords(reader, records);
        } catch (XMLStreamException e) {
            throw new EimmlParseException("Unable to read EIM records", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    log.error("Unable to close XML stream", e);
                }
            }
        }

        return records;
    }

    private void readAllRecords(XMLStreamReader reader,
                                List<EimRecord> records)
        throws XMLStreamException {

        // process <records>
        reader.nextTag();
        if (! (reader.isStartElement() &&
               reader.getName().equals(QN_RECORDS)))
            throw new EimmlParseException("Root element not " + QN_RECORDS);

        while (reader.hasNext()) {
            reader.nextTag();

            // process <record>
            if (reader.isStartElement()) {
                if (! reader.getLocalName().equals(EL_RECORD))
                    throw new EimmlParseException("Root element may only contain " + EL_RECORD + " elements");

                if (reader.getNamespaceURI().equals(NS_COLLECTION))
                    records.add(readCollectionRecord(reader));
                else if (reader.getNamespaceURI().equals(NS_ITEM))
                    records.add(readItemRecord(reader));
                else if (reader.getNamespaceURI().equals(NS_EVENT))
                    records.add(readEventRecord(reader));
                else if (reader.getNamespaceURI().equals(NS_TASK))
                    records.add(readTaskRecord(reader));
                else if (reader.getNamespaceURI().equals(NS_MESSAGE))
                    records.add(readMessageRecord(reader));
                else if (reader.getNamespaceURI().equals(NS_NOTE))
                    records.add(readNoteRecord(reader));
                else
                    // XXX store unknown record types?
                    throw new EimmlParseException("Found unknown EIM record type: " + reader.getNamespaceURI());
            }

            else {
                // process </records>
                if (! reader.getName().equals(QN_RECORDS))
                    throw new EimmlParseException("Mismatched end element " + reader.getName());

                return;
            }
        }
    }

    private CollectionRecord readCollectionRecord(XMLStreamReader reader)
        throws XMLStreamException {
        CollectionRecord record = new CollectionRecord();

        while (reader.hasNext()) {
            reader.nextTag();
            if (reader.isEndElement())
                break;
            if (reader.getLocalName().equals(EL_DELETED)) {
                record.setDeleted(true);
                break;
            }
            if (reader.getLocalName().equals(EL_UUID))
                record.setUuid(reader.getElementText());
            else
                throw new EimmlParseException("Found unknown element " + reader.getName());
        }

        return record;
    }

    private ItemRecord readItemRecord(XMLStreamReader reader)
        throws XMLStreamException {
        ItemRecord record = new ItemRecord();

        while (reader.hasNext()) {
            reader.nextTag();
            if (reader.isEndElement())
                break;
            if (reader.getLocalName().equals(EL_DELETED)) {
                record.setDeleted(true);
                break;
            }
            if (reader.getLocalName().equals(EL_UUID))
                record.setUuid(reader.getElementText());
            else if (reader.getLocalName().equals(EL_TITLE))
                record.setTitle(reader.getElementText());
            else if (reader.getLocalName().equals(EL_TRIAGE_STATUS))
                record.setTriageStatus(reader.getElementText());
            else if (reader.getLocalName().equals(EL_TRIAGE_STATUS_CHANGED))
                record.setTriageStatusChanged(parseDecimal(reader.getElementText()));
            else if (reader.getLocalName().equals(EL_LAST_MODIFIED_BY))
                record.setLastModifiedBy(reader.getElementText());
            else if (reader.getLocalName().equals(EL_CREATED_ON))
                record.setCreatedOn(parseDate(reader.getElementText()));
            else
                throw new EimmlParseException("Found unknown element " + reader.getName());
        }

        return record;
    }

    private EventRecord readEventRecord(XMLStreamReader reader)
        throws XMLStreamException { 
        EventRecord record = new EventRecord();

        while (reader.hasNext()) {
            reader.nextTag();
            if (reader.isEndElement())
                break;
            if (reader.getLocalName().equals(EL_DELETED)) {
                record.setDeleted(true);
                break;
            }
            if (reader.getLocalName().equals(EL_UUID))
                record.setUuid(reader.getElementText());
            else if (reader.getLocalName().equals(EL_DTSTART))
                record.setDtStart(toICalDate(reader.getElementText()));
            else if (reader.getLocalName().equals(EL_DTEND))
                record.setDtEnd(toICalDate(reader.getElementText()));
            else if (reader.getLocalName().equals(EL_LOCATION))
                record.setLocation(reader.getElementText());
            else if (reader.getLocalName().equals(EL_RRULE))
                record.setRRules(parseICalRecurs(reader.getElementText()));
            else if (reader.getLocalName().equals(EL_EXRULE))
                record.setExRules(parseICalRecurs(reader.getElementText()));
            else if (reader.getLocalName().equals(EL_RDATE))
                record.setRDates(parseICalDates(reader.getElementText()));
            else if (reader.getLocalName().equals(EL_EXDATE))
                record.setExDates(parseICalDates(reader.getElementText()));
            else if (reader.getLocalName().equals(EL_RECURRENCE_ID))
                record.setRecurrenceId(toICalDate(reader.getElementText()));
            else if (reader.getLocalName().equals(EL_STATUS))
                record.setStatus(reader.getElementText());
            else
                throw new EimmlParseException("Found unknown element " + reader.getName());
        }

        return record;
   }

    private NoteRecord readNoteRecord(XMLStreamReader reader)
        throws XMLStreamException {
        NoteRecord record = new NoteRecord();

        while (reader.hasNext()) {
            reader.nextTag();
            if (reader.isEndElement())
                break;
            if (reader.getLocalName().equals(EL_DELETED)) {
                record.setDeleted(true);
                break;
            }
            if (reader.getLocalName().equals(EL_UUID))
                record.setUuid(reader.getElementText());
            else if (reader.getLocalName().equals(EL_BODY))
                record.setBody(reader.getElementText());
            else if (reader.getLocalName().equals(EL_ICAL_UID))
                record.setIcalUid(reader.getElementText());
            else
                throw new EimmlParseException("Found unknown element " + reader.getName());
        }

        return record;
    }

    private TaskRecord readTaskRecord(XMLStreamReader reader)
        throws XMLStreamException {
        TaskRecord record = new TaskRecord();

        while (reader.hasNext()) {
            reader.nextTag();
            if (reader.isEndElement())
                break;
            if (reader.getLocalName().equals(EL_UUID))
                record.setUuid(reader.getElementText());
            else
                throw new EimmlParseException("Found unknown element " + reader.getName());
        }

        return record;
    }

    private MailMessageRecord readMessageRecord(XMLStreamReader reader)
        throws XMLStreamException {
        MailMessageRecord record = new MailMessageRecord();

        while (reader.hasNext()) {
            reader.nextTag();
            if (reader.isEndElement())
                break;
            if (reader.getLocalName().equals(EL_DELETED)) {
                record.setDeleted(true);
                break;
            }
            if (reader.getLocalName().equals(EL_UUID))
                record.setUuid(reader.getElementText());
            else if (reader.getLocalName().equals(EL_SUBJECT))
                record.setSubject(reader.getElementText());
            else if (reader.getLocalName().equals(EL_TO))
                record.setTo(reader.getElementText());
            else if (reader.getLocalName().equals(EL_CC))
                record.setCc(reader.getElementText());
            else if (reader.getLocalName().equals(EL_BCC))
                record.setBcc(reader.getElementText());
            else
                throw new EimmlParseException("Found unknown element " + reader.getName());
        }

        return record;
    }

    private BigDecimal parseDecimal(String text) {
        return new BigDecimal(text);
    }

    private java.util.Date parseDate(String text) {
        try {
            return DateUtil.parseRfc3339Date(text);
        } catch (ParseException e) {
            throw new EimmlParseException("Invalid RFC 339 datetime " + text);
        }
    }

    private List<Recur> parseICalRecurs(String text) {
        ArrayList<Recur> recurs = new ArrayList<Recur>();
        for (String value : text.split(","))
            recurs.add(toICalRecur(value));
        return recurs;
    }

    private DateList parseICalDates(String text) {
        DateList dates = new DateList(Value.DATE_TIME);
        for (String value : text.split(","))
            dates.add(toICalDate(value));
        return dates;
    }

    private Recur toICalRecur(String text) {
        try {
            return new Recur(text);
        } catch (ParseException e) {
            throw new EimmlParseException("Invalid iCalendar recur " + text);
        }
    }

    private DateTime toICalDate(String text) {
        try {
            return new DateTime(text);
        } catch (ParseException e) {
            throw new EimmlParseException("Invalid iCalendar datetime " + text);
        }
    }
}
