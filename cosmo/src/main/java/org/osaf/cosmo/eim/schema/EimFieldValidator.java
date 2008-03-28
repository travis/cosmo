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
package org.osaf.cosmo.eim.schema;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.osaf.cosmo.eim.BlobField;
import org.osaf.cosmo.eim.BytesField;
import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.DateTimeField;
import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.IntegerField;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.TextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides methods for validating EIM field values.
 */
public class EimFieldValidator implements EimSchemaConstants {
    private static final Log log =
        LogFactory.getLog(EimFieldValidator.class);

    private EimFieldValidator() {}

    /**
     * Validates and returns a boolean from an integer field value.
     *
     * @throws EimValidationException if the value is invalid
     */
    public static Boolean validateBoolean(EimRecordField field)
        throws EimValidationException {
        Integer value = validateInteger(field);
        if (value.intValue() == 0)
            return Boolean.FALSE;
        if (value.intValue() == 1)
            return Boolean.TRUE;
        throw new EimValidationException("Field " + field.getName() + " contains an invalid boolean value " + value);
    }

    /**
     * Validates and returns a clob field value.
     *
     * @throws EimValidationException if the value is invalid
     */
    public static Reader validateClob(EimRecordField field)
        throws EimValidationException {
        if (! (field instanceof ClobField))
            throw new EimValidationException("Field " + field.getName() + " is not a clob field");
        Reader value = ((ClobField)field).getClob();
        return value;
    }

    /**
     * Validates and returns a datetime field value.
     *
     * @throws EimValidationException if the value is invalid
     */
    public static Calendar validateDateTime(EimRecordField field)
        throws EimValidationException {
        if (! (field instanceof DateTimeField))
            throw new EimValidationException("Field " + field.getName() + " is not a datetime field");
        Calendar value = ((DateTimeField)field).getCalendar();
        return value;
    }

    /**
     * Validates and returns a decimal field value.
     *
     * @throws EimValidationException if the value is invalid
     */
    public static BigDecimal validateDecimal(EimRecordField field,
                                             int numDigits,
                                             int numDecimalPlaces)
        throws EimValidationException {
        if (! (field instanceof DecimalField))
            throw new EimValidationException("Field " + field.getName() + " is not a decimal field");
        BigDecimal value = ((DecimalField)field).getDecimal();
        if (value == null)
            return value;
        if (numDigits > 0) {
            if (value.precision() > numDigits)
                throw new EimValidationException("Field " + field.getName() + " decimal value has " + value.precision() + " digits which is more than the maximum of " + numDigits);
        }
        if (numDecimalPlaces > 0) {
            if (value.scale() > numDecimalPlaces)
                throw new EimValidationException("Field " + field.getName() + " decimal value has " + value.scale() + " decimal places which is more than the maximum of " + numDecimalPlaces);
        }
        return value;
    }

    /**
     * Validates and returns a text field value.
     *
     * @throws EimValidationException if the value is invalid
     */
    public static String validateText(EimRecordField field,
                                      int maxLength)
        throws EimValidationException {
        if (! (field instanceof TextField))
            throw new EimValidationException("Field " + field.getName() + " is not a text field");
        String value = ((TextField)field).getText();
        if (value == null)
            return value;
        if (maxLength > 0) {
            try {
                int len = value.getBytes("UTF-8").length;
                if (len > maxLength)
                    throw new EimValidationException("Field " + field.getName() + " text value is " + len + " bytes which is greater than the allowable length of " + maxLength + " bytes");
            } catch (UnsupportedEncodingException e) {
                // will never happen
            }
        }
        return value;
    }

    /**
     * Validates and returns a timestamp field value.
     *
     * @throws EimValidationException if the value is invalid
     */
    public static Date validateTimeStamp(EimRecordField field)
        throws EimValidationException {
        BigDecimal bd =
            validateDecimal(field, DIGITS_TIMESTAMP, DEC_TIMESTAMP);
        if (bd == null)
            return null;
        // a unix timestamp is the number of seconds since the epoch,
        // but Date wants milliseconds
        Date value = new Date(bd.longValue() * 1000);
        return value;
    }

    /**
     * Validates and returns a integer field value.
     *
     * @throws EimValidationException if the value is invalid
     */
    public static Integer validateInteger(EimRecordField field)
        throws EimValidationException {
        if (! (field instanceof IntegerField))
            throw new EimValidationException("Field " + field.getName() + " is not a integer field");
        Integer value = ((IntegerField)field).getInteger();
        return value;
    }
}
