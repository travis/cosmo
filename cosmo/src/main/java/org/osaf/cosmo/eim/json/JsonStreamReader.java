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
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.osaf.cosmo.eim.eimml.EimmlConstants;
import org.osaf.cosmo.eim.eimml.EimmlConversionException;
import org.osaf.cosmo.eim.eimml.EimmlTypeConverter;

import com.sdicons.json.model.JSONArray;
import com.sdicons.json.model.JSONInteger;
import com.sdicons.json.model.JSONNull;
import com.sdicons.json.model.JSONObject;
import com.sdicons.json.model.JSONString;
import com.sdicons.json.model.JSONValue;
import com.sdicons.json.parser.JSONParser;

public class JsonStreamReader implements JsonConstants, EimmlConstants{
    private static final Log log = LogFactory.getLog(JsonStreamReader.class);

    private JSONParser parser = null;
    private Reader in = null;
    
    public JsonStreamReader(Reader in) throws IOException,
            JsonStreamException {
        parser = new JSONParser(in);
        this.in = in;
    }
    
    public EimRecordSet nextRecordSet() throws JsonStreamException {
        try {
            return readNextRecordSet();
        } catch (Exception e) {
            close();
            if (e instanceof JsonStreamException)
                throw (JsonStreamException) e;
            throw new JsonStreamException("Error reading next recordset", e);
        }
    }
    
    private EimRecordSet readNextRecordSet() throws JsonStreamException {
        EimRecordSet recordSet = new EimRecordSet();
        
        try {
            JSONObject jrecordSet = (JSONObject) parser.nextValue();
            Map<String, JSONValue> recordSetMap = jrecordSet.getValue();
           
            //get, set the uuid
            recordSet.setUuid(getStringValue(jrecordSet.get(KEY_UUID)));
            if (recordSet.getUuid() == null)
                throw new JsonValidationException("Recordset has no uuid");
            
            //deal with records
            Map<String, JSONValue> recordsMap = getMapValue(recordSetMap.get(KEY_RECORDS));
            if (recordsMap != null){
                for (Entry<String, JSONValue> entry : recordsMap.entrySet()){
                      EimRecord record = readRecord(entry.getKey(), getMapValue(entry.getValue()));
                      recordSet.addRecord(record);
                }
            }
            
            //deal with deletedRecords
            List<JSONValue> deletedRecordsArray = getListValue(recordSetMap.get(KEY_DELETED_RECORDS));
            if(deletedRecordsArray != null){
                for (JSONValue prefixAndNameSpaceArray : deletedRecordsArray){
                    List<JSONValue> prefixAndNameSpaceList = getListValue(prefixAndNameSpaceArray);
                    String prefix = getStringValue(prefixAndNameSpaceList.get(0));
                    String nameSpace = getStringValue(prefixAndNameSpaceList.get(1));
                    EimRecord record = new EimRecord(prefix, nameSpace);
                    record.setDeleted(true);
                    recordSet.addRecord(record);
                }
            }
            
        } catch (Exception e){
            if (e instanceof JsonStreamException)
                throw (JsonStreamException) e;
            throw new JsonStreamException("Problem reading record set.", e);
        }

        return recordSet;
    }

    private EimRecord readRecord(String prefix, Map<String, JSONValue> recordMap)
            throws JsonStreamException {
        EimRecord record = new EimRecord(prefix, getStringValue(recordMap
                .get(KEY_NS)));

        //deal with key
        Map<String, JSONValue> keyMap = getMapValue(recordMap.get(KEY_KEY));
        if (keyMap != null) {
            EimRecordKey key = readKey(keyMap);
            record.setKey(key);
        }
        
        //deal with deleted fields
        List<JSONValue> missingFields = getListValue(recordMap
                .get(KEY_MISSING_FIELDS));
        if (missingFields != null) {
            for (JSONValue field : missingFields) {
                EimRecordField eimField = new TextField(getAsString(field),
                        null);
                eimField.setMissing(true);
                record.addField(eimField);
            }
        }

        //deal with fields
        Map<String, JSONValue> fieldMap = getMapValue(recordMap.get(KEY_FIELDS));
        if (fieldMap != null) {
            for (Entry<String, JSONValue> entry : fieldMap.entrySet()) {
                EimRecordField field = readField(entry.getKey(),
                        getListValue(entry.getValue()));
                record.addField(field);
            }
        }
        return record;
    }

    private EimRecordField readField(String name, List<JSONValue> listValue) throws JsonStreamException {
        if (listValue == null){
            return null;
        }
        EimRecordField field = null;
        String type = getStringValue(listValue.get(0));
        String text = getAsString(listValue.get(1));
        try {
        if (type.equals(TYPE_BYTES)) {
            byte[] value = EimmlTypeConverter.toBytes(text);
            field = new BytesField(name, value);
        } else if (type.equals(TYPE_TEXT)) {
            String value = EimmlTypeConverter.toText(text,
                                                     "UTF-8");
            field = new TextField(name, value);
        } else if (type.equals(TYPE_BLOB)) {
            InputStream value = EimmlTypeConverter.toBlob(text);
            field = new BlobField(name, value);
        } else if (type.equals(TYPE_CLOB)) {
            Reader value = EimmlTypeConverter.toClob(text);
            field = new ClobField(name, value);
        } else if (type.equals(TYPE_INTEGER)) {
            Integer value = EimmlTypeConverter.toInteger(text);
            field = new IntegerField(name, value);
        } else if (type.equals(TYPE_DATETIME)) {
            Calendar value = EimmlTypeConverter.toDateTime(text);
            field = new DateTimeField(name, value);
        } else if (type.equals(TYPE_DECIMAL)) {
            BigDecimal value = EimmlTypeConverter.toDecimal(text);
            field = new DecimalField(name, value);
        } else {
            throw new JsonValidationException("Unrecognized field type");
        }
        } catch (EimmlConversionException conversionException){
            throw new JsonValidationException("Problem converting field " + name + ": " + conversionException.getMessage(), conversionException);
        }
        return field;
   }

    private EimRecordKey readKey(Map<String, JSONValue> keyMap)
            throws JsonStreamException {
        EimRecordKey recordKey = new EimRecordKey();
        for (Entry<String, JSONValue> entry : keyMap.entrySet()) {
            EimRecordField field = readField(entry.getKey(), getListValue(entry
                    .getValue()));
            recordKey.addField(field);
        }

        return recordKey;
    }

    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            log.error("Unable to close XML stream", e);
        }
    }    

    private static List<JSONValue> getListValue(JSONValue jsonValue){
        if (jsonValue == null || jsonValue.isNull()){
            return null;
        }
        
        JSONArray jsarray = (JSONArray) jsonValue;
        return jsarray.getValue();
    }
    
    private static Map<String, JSONValue> getMapValue(JSONValue jsonValue){
        if (jsonValue == null || jsonValue.isNull()){
            return null;
        }
        
        JSONObject jsonObject = (JSONObject) jsonValue;
        return jsonObject.getValue();
    }
    
    private static String getStringValue(JSONValue jsonValue){
        if (jsonValue == null || jsonValue.isNull()){
            return null;
        }
        JSONString jsonString = (JSONString) jsonValue;
        return jsonString.getValue();
    }
    
    private String getAsString(JSONValue j){
        if (j instanceof JSONNull){
            return null;
        } else if (j instanceof JSONString){
            return ((JSONString) j).getValue();
        } else {
            return j.render(false);
        }
    }
    
    public static void main (String[] args){
        JSONInteger i = new JSONInteger(new BigInteger("123"));
        System.out.println(i.toString());
        JSONString s = new JSONString("ASDASDASD");
        System.out.println(s.render(false));
        
    }
}
