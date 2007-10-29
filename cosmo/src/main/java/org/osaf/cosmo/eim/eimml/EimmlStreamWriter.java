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

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Date;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.BlobField;
import org.osaf.cosmo.eim.BytesField;
import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.DateTimeField;
import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.EimRecordKey;
import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.IntegerField;
import org.osaf.cosmo.eim.TextField;

/**
 */
public class EimmlStreamWriter implements EimmlConstants, XMLStreamConstants {
    private static final Log log = LogFactory.getLog(EimmlStreamWriter.class);
    private static final XMLOutputFactory XML_OUTPUT_FACTORY =
        XMLOutputFactory.newInstance();

    private boolean writeCharacterData = false;
    private XMLStreamWriter xmlWriter;

    /**
     * Opens the writer. No data is actually written to the stream.
     */
    public EimmlStreamWriter(Writer out)
        throws IOException, EimmlStreamException {
        try {
            xmlWriter = XML_OUTPUT_FACTORY.createXMLStreamWriter(out);
        } catch (XMLStreamException e) {
            throw new EimmlStreamException("Error opening EIMML stream", e);
        }
    }

    /**
     * Writes the XML document header.
     */
    public void writeStartDocument()
        throws EimmlStreamException {
        try {
            xmlWriter.writeStartDocument();
        } catch (XMLStreamException e) {
            close();
            throw new EimmlStreamException("Error writing start document", e);
        }
    }

    /**
     * Writes the root collection element.
     */
    public void writeCollection(String uuid,
                                String name,
                                Long hue)
        throws EimmlStreamException {
        try {
            xmlWriter.setPrefix(PRE_CORE, NS_CORE);

            xmlWriter.writeStartElement(NS_CORE, EL_COLLECTION);
            xmlWriter.writeNamespace(PRE_CORE, NS_CORE);

            xmlWriter.writeAttribute(ATTR_UUID, uuid);

            if (name != null)
                xmlWriter.writeAttribute(ATTR_NAME, name);
            
            if(hue != null)
                xmlWriter.writeAttribute(ATTR_HUE, hue.toString());
        } catch (XMLStreamException e) {
            throw new EimmlStreamException("Error writing collection", e);
        }
    }

    /** */
    public void writeDeleted()
        throws EimmlStreamException {
        try {
            xmlWriter.writeAttribute(NS_CORE, ATTR_DELETED, "true");
        } catch (XMLStreamException e) {
            close();
            throw new EimmlStreamException("Error writing recordset", e);
        }
    }

    /** */
    public void writeRecordSet(EimRecordSet recordset)
        throws EimmlStreamException {
        try {
            doWriteRecordSet(recordset);
        } catch (XMLStreamException e) {
            close();
            throw new EimmlStreamException("Error writing recordset", e);
        }
    }

    /** */
    public void writeRecord(EimRecord record)
        throws EimmlStreamException {
        try {
            doWriteRecord(record);
        } catch (XMLStreamException e) {
            close();
            throw new EimmlStreamException("Error writing record", e);
        }
    }

    /** */
    public void writeKey(EimRecordKey key)
        throws EimmlStreamException {
        try {
            doWriteKey(key);
        } catch (XMLStreamException e) {
            close();
            throw new EimmlStreamException("Error writing key", e);
        }
    }

    /** */
    public void writeField(EimRecordField field)
        throws EimmlStreamException {
        try {
            doWriteField(field);
        } catch (XMLStreamException e) {
            close();
            throw new EimmlStreamException("Error writing field", e);
        }
    }

    public boolean getWriteCharacterData() {
        return writeCharacterData;
    }

    public void setWriteCharacterData(boolean flag) {
        writeCharacterData = flag;
    }

    /**
     * Closes the root collection element and ends the document.
     */
    public void close() {
        try {
            xmlWriter.writeEndElement();
            xmlWriter.writeEndDocument();
            xmlWriter.close();
        } catch (XMLStreamException e) {
            log.error("Unable to close EIM stream", e);
        }
    }

    private void doWriteRecordSet(EimRecordSet recordset)
        throws EimmlStreamException, XMLStreamException {

        xmlWriter.writeStartElement(NS_CORE, EL_RECORDSET);
        xmlWriter.writeAttribute(ATTR_UUID, recordset.getUuid());

        if (recordset.isDeleted()) {
            xmlWriter.writeAttribute(NS_CORE, ATTR_DELETED, "true");
        } else {
            for (EimRecord record : recordset.getRecords())
                writeRecord(record);
        }

        xmlWriter.writeEndElement();
    }

    private void doWriteRecord(EimRecord record)
        throws EimmlStreamException, XMLStreamException {

        xmlWriter.setPrefix(record.getPrefix(), record.getNamespace());

        xmlWriter.writeStartElement(record.getNamespace(), EL_RECORD);
        xmlWriter.writeNamespace(record.getPrefix(), record.getNamespace());

        if (record.isDeleted())
            xmlWriter.writeAttribute(NS_CORE, ATTR_DELETED, "true");

        writeKey(record.getKey());

        if (! record.isDeleted()) {
            for (EimRecordField field : record.getFields())
                writeField(field);
        }

        xmlWriter.writeEndElement();
    }

    private void doWriteKey(EimRecordKey key)
        throws EimmlStreamException, XMLStreamException {
        if (key == null)
            return;
        for (EimRecordField field : key.getFields())
            doWriteField(field, true);
    }

    private void doWriteField(EimRecordField field)
        throws EimmlStreamException, XMLStreamException {
        doWriteField(field, false);
    }

    private void doWriteField(EimRecordField field,
                              boolean isKey)
        throws EimmlStreamException, XMLStreamException {
        String value = null;
        String type = null;
        if (field instanceof BlobField) {
            value = EimmlTypeConverter.
                fromBlob(((BlobField)field).getBlob());
            type = TYPE_BLOB;
        } else if (field instanceof BytesField) {
            value = EimmlTypeConverter.
                fromBytes(((BytesField)field).getBytes());
            type = TYPE_BYTES;
        } else if (field instanceof ClobField) {
            value = EimmlTypeConverter.fromClob(((ClobField)field).getClob());
            type = TYPE_CLOB;
        } else if (field instanceof DateTimeField) {
            value = EimmlTypeConverter.
                fromDateTime(((DateTimeField)field).getCalendar());
            type = TYPE_DATETIME;
        } else if (field instanceof DecimalField) {
            DecimalField df = (DecimalField)field;
            value = EimmlTypeConverter.
                fromDecimal(df.getDecimal(), df.getDigits(),
                            df.getDecimalPlaces());
            type = TYPE_DECIMAL;
        } else if (field instanceof IntegerField) {
            value = EimmlTypeConverter.
                fromInteger(((IntegerField)field).getInteger());
            type = TYPE_INTEGER;
        } else if (field instanceof TextField) {
            // no conversion required - already a UTF-8 string
            value = ((TextField)field).getText();
            type = TYPE_TEXT;
        } else {
            throw new EimmlStreamException("Unrecognized field type");
        }

        xmlWriter.writeStartElement(field.getRecord().getNamespace(),
                                    field.getName());
        xmlWriter.writeAttribute(NS_CORE, ATTR_TYPE, type);
        if (isKey)
            xmlWriter.writeAttribute(NS_CORE, ATTR_KEY, "true");
        
        if(field.isMissing())
            xmlWriter.writeAttribute(ATTR_MISSING, "true");
        
        if (value != null) {
            if (isEmptyableType(type) && value.equals(""))
                xmlWriter.writeAttribute(ATTR_EMPTY, "true");
            else {
                if (writeCharacterData)
                    xmlWriter.writeCData(value);
                else
                    xmlWriter.writeCharacters(value);
            }
        }

        xmlWriter.writeEndElement();
    }

    private boolean isEmptyableType(String type) {
        return (type.equals(TYPE_TEXT) ||
                type.equals(TYPE_CLOB) ||
                type.equals(TYPE_BLOB));
    }
}
