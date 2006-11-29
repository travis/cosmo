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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
 * Writes an EIM model to an output stream as an EIMML document.
 */
public class EimmlOutputter implements EimmlConstants {
    private static final Log log = LogFactory.getLog(EimmlOutputter.class);
    private static final XMLOutputFactory outputFactory =
        XMLOutputFactory.newInstance();

    /**
     * Writes the given EIM records to the specified output stream as
     * an EIMML document.
     *
     * @param records the records to write
     * @param out the output stream to write to
     *
     * @throws IOException
     */
    public final void output(List<EimRecord> records,
                             OutputStream out)
        throws IOException {
        XMLStreamWriter writer = null;
        try {
            writer = outputFactory.createXMLStreamWriter(out);
            writeAllRecords(writer, records);
        } catch (XMLStreamException e) {
            throw new EimmlOutputException("Unable to write EIM records", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (XMLStreamException e) { 
                    log.error("Unable to close XML stream", e);
                }
            }
        }
    }

    private void writeAllRecords(XMLStreamWriter writer,
                                 List<EimRecord> records)
        throws XMLStreamException {
        writer.setPrefix(PRE_CORE, NS_CORE);
        writer.setPrefix(PRE_COLLECTION, NS_COLLECTION);
        writer.setPrefix(PRE_ITEM, NS_ITEM);
        writer.setPrefix(PRE_EVENT, NS_EVENT);
        writer.setPrefix(PRE_TASK, NS_TASK);
        writer.setPrefix(PRE_MESSAGE, NS_MESSAGE);
        writer.setPrefix(PRE_NOTE, NS_NOTE);

        writer.writeStartDocument();

        writer.writeStartElement(NS_CORE, EL_RECORDS);
        writer.writeNamespace(PRE_CORE, NS_CORE);
        writer.writeNamespace(PRE_COLLECTION, NS_COLLECTION);
        writer.writeNamespace(PRE_ITEM, NS_ITEM);
        writer.writeNamespace(PRE_EVENT, NS_EVENT);
        writer.writeNamespace(PRE_TASK, NS_TASK);
        writer.writeNamespace(PRE_MESSAGE, NS_MESSAGE);
        writer.writeNamespace(PRE_NOTE, NS_NOTE);

        for (EimRecord record : records) {
            if (record instanceof CollectionRecord)
                writeRecord(writer, (CollectionRecord) record);
            else if (record instanceof ItemRecord)
                writeRecord(writer, (ItemRecord) record);
            else if (record instanceof EventRecord)
                writeRecord(writer, (EventRecord) record);
            else if (record instanceof NoteRecord)
                writeRecord(writer, (NoteRecord) record);
            else if (record instanceof TaskRecord)
                writeRecord(writer, (TaskRecord) record);
            else if (record instanceof MailMessageRecord)
                writeRecord(writer, (MailMessageRecord) record);
        }

        writer.writeEndElement();

        writer.writeEndDocument();
    }

    private void writeRecord(XMLStreamWriter writer,
                             CollectionRecord record)
        throws XMLStreamException {
        writer.writeStartElement(NS_COLLECTION, EL_RECORD);

        writer.writeStartElement(NS_CORE, EL_UUID);
        writer.writeCharacters(record.getUuid());
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeRecord(XMLStreamWriter writer,
                             ItemRecord record)
        throws XMLStreamException {
        writer.writeStartElement(NS_ITEM, EL_RECORD);

        writer.writeStartElement(NS_CORE, EL_UUID);
        writer.writeCharacters(record.getUuid());
        writer.writeEndElement();

        writer.writeStartElement(NS_ITEM, EL_TITLE);
        writer.writeCharacters(record.getTitle());
        writer.writeEndElement();

        if (record.getTriageStatus() != null) {
            writer.writeStartElement(NS_ITEM, EL_TRIAGE_STATUS);
            writer.writeCharacters(record.getTriageStatus());
            writer.writeEndElement();
        }

        if (record.getTriageStatusChanged() != null) {
            writer.writeStartElement(NS_ITEM, EL_TRIAGE_STATUS_CHANGED);
            writer.writeCharacters(formatDecimal(record.getTriageStatusChanged()));
            writer.writeEndElement();
        }

        if (record.getLastModifiedBy() != null) {
            writer.writeStartElement(NS_ITEM, EL_LAST_MODIFIED_BY);
            writer.writeCharacters(record.getLastModifiedBy());
            writer.writeEndElement();
        }

        writer.writeStartElement(NS_ITEM, EL_CREATED_ON);
        writer.writeCharacters(formatDate(record.getCreatedOn()));
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeRecord(XMLStreamWriter writer,
                             EventRecord record)
        throws XMLStreamException {
        writer.writeStartElement(EL_RECORD);

        writer.writeStartElement(NS_CORE, EL_UUID);
        writer.writeCharacters(record.getUuid());
        writer.writeEndElement();

        writer.writeStartElement(NS_EVENT, EL_DTSTART);
        writer.writeCharacters(formatDate(record.getDtStart()));
        writer.writeEndElement();

        writer.writeStartElement(NS_EVENT, EL_DTEND);
        writer.writeCharacters(formatDate(record.getDtEnd()));
        writer.writeEndElement();

        if (record.getLocation() != null) {
            writer.writeStartElement(NS_EVENT, EL_LOCATION);
            writer.writeCharacters(record.getLocation());
            writer.writeEndElement();
        }

        if (record.getRRule() != null) {
            writer.writeStartElement(NS_EVENT, EL_RRULE);
            writer.writeCharacters(record.getRRule());
            writer.writeEndElement();
        }

        if (record.getExRule() != null) {
            writer.writeStartElement(NS_EVENT, EL_EXRULE);
            writer.writeCharacters(record.getExRule());
            writer.writeEndElement();
        }

        if (record.getRDate() != null) {
            writer.writeStartElement(NS_EVENT, EL_RDATE);
            writer.writeCharacters(record.getRDate());
            writer.writeEndElement();
        }

        if (record.getExDate() != null) {
            writer.writeStartElement(NS_EVENT, EL_EXDATE);
            writer.writeCharacters(record.getExDate());
            writer.writeEndElement();
        }

        if (record.getRecurrenceId() != null) {
            writer.writeStartElement(NS_EVENT, EL_RECURRENCE_ID);
            writer.writeCharacters(formatDate(record.getRecurrenceId()));
            writer.writeEndElement();
        }

        if (record.getStatus() != null) {
            writer.writeStartElement(NS_EVENT, EL_STATUS);
            writer.writeCharacters(record.getStatus());
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private void writeRecord(XMLStreamWriter writer,
                             NoteRecord record)
        throws XMLStreamException {
        writer.writeStartElement(EL_RECORD);

        writer.writeStartElement(NS_CORE, EL_UUID);
        writer.writeCharacters(record.getUuid());
        writer.writeEndElement();

        if (record.getBody() != null) {
            writer.writeStartElement(NS_NOTE, EL_BODY);
            writer.writeCharacters(record.getBody());
            writer.writeEndElement();
        }

        if (record.getIcalUid() != null) {
            writer.writeStartElement(NS_NOTE, EL_ICAL_UID);
            writer.writeCharacters(record.getIcalUid());
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private void writeRecord(XMLStreamWriter writer,
                             TaskRecord record)
        throws XMLStreamException {
        writer.writeStartElement(EL_RECORD);

        writer.writeStartElement(NS_CORE, EL_UUID);
        writer.writeCharacters(record.getUuid());
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeRecord(XMLStreamWriter writer,
                             MailMessageRecord record)
        throws XMLStreamException {
        writer.writeStartElement(EL_RECORD);

        writer.writeStartElement(NS_CORE, EL_UUID);
        writer.writeCharacters(record.getUuid());
        writer.writeEndElement();

        if (record.getSubject() != null) {
            writer.writeStartElement(NS_MESSAGE, EL_SUBJECT);
            writer.writeCharacters(record.getSubject());
            writer.writeEndElement();
        }

        if (record.getTo() != null) {
            writer.writeStartElement(NS_MESSAGE, EL_TO);
            writer.writeCharacters(record.getTo());
            writer.writeEndElement();
        }

        if (record.getCc() != null) {
            writer.writeStartElement(NS_MESSAGE, EL_CC);
            writer.writeCharacters(record.getCc());
            writer.writeEndElement();
        }

        if (record.getBcc() != null) {
            writer.writeStartElement(NS_MESSAGE, EL_BCC);
            writer.writeCharacters(record.getBcc());
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private String formatDecimal(BigDecimal bd) {
        return DECIMAL_FORMATTER.format(bd);
    }

    private String formatDate(Date d) {
        return DateUtil.formatRfc3339Date(d);
    }
}
