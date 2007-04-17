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
package org.osaf.cosmo.eim.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONWriter;
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
import org.osaf.cosmo.eim.eimml.EimmlConstants;
import org.osaf.cosmo.eim.eimml.EimmlConversionException;
import org.osaf.cosmo.eim.eimml.EimmlTypeConverter;

/**
 */
public class JsonStreamWriter implements JsonConstants, XMLStreamConstants, EimmlConstants {
    private static final Log log = LogFactory.getLog(JsonStreamWriter.class);
    private static final XMLOutputFactory XML_OUTPUT_FACTORY =
        XMLOutputFactory.newInstance();

    private boolean writeCharacterData = false;
    private JSONWriter jsonWriter;
    private OutputStreamWriter writer;


    public JsonStreamWriter(OutputStream out)
        throws IOException, JsonStreamException {
        writer = new OutputStreamWriter(out);
        jsonWriter = new JSONWriter(writer);
    }

    /** */
    public void writeRecordSet(EimRecordSet recordset)
        throws JsonStreamException {
        try {
            doWriteRecordSet(recordset);
        } catch (JSONException e) {
            close();
            throw new JsonStreamException("Error writing recordset", e);
        }
    }

    /** */
    public void writeRecord(EimRecord record)
        throws JsonStreamException {
        try {
            doWriteRecord(record);
        } catch (JSONException e) {
            close();
            throw new JsonStreamException("Error writing record", e);
        }
    }

    /** */
    public void writeKey(EimRecordKey key)
        throws JsonStreamException {
        try {
            doWriteKey(key);
        } catch (JSONException e) {
            close();
            throw new JsonStreamException("Error writing key", e);
        }
    }

    /** */
    public void writeField(EimRecordField field)
        throws JsonStreamException {
        try {
            doWriteField(field);
        } catch (JSONException e) {
            close();
            throw new JsonStreamException("Error writing field", e);
        }
    }

    public boolean getWriteCharacterData() {
        return writeCharacterData;
    }

    public void setWriteCharacterData(boolean flag) {
        writeCharacterData = flag;
    }

    /**
     * Closes the root element and ends the document.
     */
    public void close() throws JsonStreamException {
        try {
            writer.close();
        } catch (IOException ioe) {
            throw new JsonStreamException("Problem closing writer", ioe);
        }
    }

    private void doWriteRecordSet(EimRecordSet recordset)
        throws JsonStreamException, JSONException {
        
        jsonWriter.object().key(KEY_UUID).value(recordset.getUuid());

        if (recordset.isDeleted()) {
            jsonWriter.key(KEY_DELETED).value(1);
        } else {
            List<EimRecord> notDeleted = new ArrayList<EimRecord>();
            List<EimRecord> deleted = new ArrayList<EimRecord>();
            for (EimRecord record : recordset.getRecords()){
                if (record.isDeleted()){
                    deleted.add(record);
                } else {
                    notDeleted.add(record);
                }
            }
            
            if (!deleted.isEmpty()){
                jsonWriter.key(KEY_DELETED_RECORDS).array();
                for (EimRecord record : deleted){
                    writeKey(record.getKey());
                }
                jsonWriter.endArray();
            }
            
            if (!notDeleted.isEmpty()){
                jsonWriter.key(KEY_RECORDS).object();
                for (EimRecord record : notDeleted){
                    writeRecord(record);
                }
                jsonWriter.endObject();
            }
        }

        jsonWriter.endObject();
    }

    private void doWriteRecord(EimRecord record)
        throws JsonStreamException, JSONException {
        List<EimRecordField> missingFields = new ArrayList<EimRecordField>();
        List<EimRecordField> notMissingFields = new ArrayList<EimRecordField>();
        
        for (EimRecordField field : record.getFields()){
            if (field.isMissing()){
                missingFields.add(field);
            } else {
                notMissingFields.add(field);
            }
        }

        jsonWriter.key(record.getPrefix());
        
        jsonWriter.object();
        
        //write namespace
        jsonWriter.key(KEY_NS).value(record.getNamespace());

        //write key
        jsonWriter.key(KEY_KEY);
        writeKey(record.getKey());
        
        //write fields
        if (!notMissingFields.isEmpty()) {
            jsonWriter.key(KEY_FIELDS);
            jsonWriter.object();
            for (EimRecordField field : notMissingFields){
                writeField(field);
            }
            jsonWriter.endObject();
        }
        
        //write deleted fields
        if (!missingFields.isEmpty()) {
            jsonWriter.key(KEY_MISSING_FIELDS);
            jsonWriter.array();
            for (EimRecordField field : notMissingFields){
                jsonWriter.value(field.getValue());
            }
            jsonWriter.endArray();
        }
        
        jsonWriter.endObject();
            
    }

    private void doWriteKey(EimRecordKey key)
        throws JsonStreamException, JSONException {
        if (key == null){
            jsonWriter.value(null);
            return;
        }
        jsonWriter.object();
        for (EimRecordField field : key.getFields()){
            doWriteField(field, true);
        }
        jsonWriter.endObject();

    }

    private void doWriteField(EimRecordField field)
        throws JsonStreamException, JSONException {
        doWriteField(field, false);
    }

    private void doWriteField(EimRecordField field,
                              boolean isKey)
        throws JsonStreamException, JSONException {
        String value = null;
        String type = null;
        
        try {
            if (field instanceof BlobField) {
                value = EimmlTypeConverter.fromBlob(((BlobField) field)
                        .getBlob());
                type = TYPE_BLOB;
            } else if (field instanceof BytesField) {
                value = EimmlTypeConverter.fromBytes(((BytesField) field)
                        .getBytes());
                type = TYPE_BYTES;
            } else if (field instanceof ClobField) {
                value = EimmlTypeConverter.fromClob(((ClobField) field)
                        .getClob());
                type = TYPE_CLOB;
            } else if (field instanceof DateTimeField) {
                value = EimmlTypeConverter.fromDateTime(((DateTimeField) field)
                        .getCalendar());
                type = TYPE_DATETIME;
            } else if (field instanceof DecimalField) {
                DecimalField df = (DecimalField) field;
                value = EimmlTypeConverter.fromDecimal(df.getDecimal(), df
                        .getDigits(), df.getDecimalPlaces());
                type = TYPE_DECIMAL;
            } else if (field instanceof IntegerField) {
                value = EimmlTypeConverter.fromInteger(((IntegerField) field)
                        .getInteger());
                type = TYPE_INTEGER;
            } else if (field instanceof TextField) {
                // no conversion required - already a UTF-8 string
                value = ((TextField) field).getText();
                type = TYPE_TEXT;
            } else {
                throw new JsonStreamException("Unrecognized field type");
            }
        } catch (EimmlConversionException ece) {
            throw new JsonStreamException("Problem converting value", ece);
        }
        
        jsonWriter.key(field.getName());
        jsonWriter.array();
        jsonWriter.value(type);
        jsonWriter.value(value);
        jsonWriter.endArray();
    }

}
