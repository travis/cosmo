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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.util.DateUtil;

/**
 * This class converts between EIMML primitive types and Java types.
 */
public class EimmlTypeConverter implements EimmlConstants {
    private static final Log log = LogFactory.getLog(EimmlTypeConverter.class);

    /**
     * Returns the given value as a byte array, converting it from
     * its transfer encoding.
     */
    public static byte[] toBytes(String value,
                                 String transferEncoding)
        throws EimmlConversionException {
        if (transferEncoding == null)
            throw new IllegalArgumentException("no transfer encoding specified");
        if (! transferEncoding.equals(TRANSFER_ENCODING_BASE64))
            throw new EimmlConversionException("Transfer encoding " + transferEncoding + " is not supported");
        return decodeHexedBase64String(value);
    }

    /**
     * Returns the value as a string, encoding it with the given
     * transfer encoding.
     */
    public static String fromBytes(byte[] value,
                                   String transferEncoding)
        throws EimmlConversionException {
        if (transferEncoding == null)
            throw new IllegalArgumentException("no transfer encoding specified");
        if (! transferEncoding.equals(TRANSFER_ENCODING_BASE64))
            throw new EimmlConversionException("Transfer encoding " + transferEncoding + " is not supported");

        if (value == null)
            return null;

        return encodeHexedBase64String(value);
    }

    /**
     * Returns the given value as a UTF-8 string, converting it from
     * its original encoding.
     */
    public static String toText(String value,
                                String encoding)
        throws EimmlConversionException {
        if (encoding == null)
            throw new IllegalArgumentException("no original encoding specified");
        if (encoding.equals("UTF-8"))
            return value;
        try {
            return new String(value.getBytes(encoding), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new EimmlConversionException("Original encoding " + encoding + " is not supported on this platform", e);
        }
    }

    /**
     * Returns the given value as a reader, preserving the value in
     * its original encoding.
     */
    public static Reader toClob(String value)
        throws EimmlConversionException {
        return new StringReader(value);
    }

    /**
     * Returns the given value as a string, preserving the value in
     * its original encoding.
     */
    public static String fromClob(Reader value)
        throws EimmlConversionException {
        if (value == null)
            return null;

        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(value, writer);
        } catch (IOException e) {
            throw new EimmlConversionException("Unable to convert Reader to String", e);
        }
        return writer.toString();
    }

    /**
     * Returns the given value as an input stream, converting it from
     * its transfer encoding.
     */
    public static InputStream toBlob(String value,
                                     String transferEncoding)
        throws EimmlConversionException {
        if (transferEncoding == null)
            throw new IllegalArgumentException("no transfer encoding specified");
        if (! transferEncoding.equals(TRANSFER_ENCODING_BASE64))
            throw new EimmlConversionException("Transfer encoding " + transferEncoding + " is not supported");
        return new ByteArrayInputStream(decodeHexedBase64String(value));
    }

    /**
     * Returns the given value as a string, encoding it with the given
     * transfer encoding.
     */
    public static String fromBlob(InputStream value,
                                  String transferEncoding)
        throws EimmlConversionException {
        if (transferEncoding == null)
            throw new IllegalArgumentException("no transfer encoding specified");
        if (! transferEncoding.equals(TRANSFER_ENCODING_BASE64))
            throw new EimmlConversionException("Transfer encoding " + transferEncoding + " is not supported");

        if (value == null)
            return null;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            IOUtils.copy(value, out);
        } catch (IOException e) {
            throw new EimmlConversionException("Unable to convert InputStream to String", e);
        }
        return encodeHexedBase64String(out.toByteArray());
    }

    /**
     * Returns the given value as an integer.
     */
    public static Integer toInteger(String value)
        throws EimmlConversionException {
        try {
            return new Integer(value);
        } catch (NumberFormatException e) {
            throw new EimmlConversionException("Provided value " + value + " is not a valid integer", e);
        }
    }

    /**
     * Returns the given value as a string.
     */
    public static String fromInteger(Integer value)
        throws EimmlConversionException {
        if (value == null)
            return null;
        return value.toString();
    }

    /**
     * Returns the given value as a calendar. The calendar's timezone
     * is set to the system's default timezone.
     */
    public static Calendar toDateTime(String value)
        throws EimmlConversionException {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(DateUtil.parseRfc3339Date(value));
            return cal;
        } catch (ParseException e) {
            throw new EimmlConversionException("Provided value " + value + " is not a valid RFC 3339 datetime", e);
        }
    }

    /**
     * Returns the given value as a string.
     */
    public static String fromDateTime(Calendar value)
        throws EimmlConversionException {
        if (value == null)
            return null;
        return DateUtil.formatRfc3339Date(value);
    }

    /**
     * Returns the given value as a timestamp in UTC.
     */
    public static Date toTimeStamp(String value)
        throws EimmlConversionException {
        try {
            return DateUtil.parseRfc3339TimeStamp(value);
        } catch (ParseException e) {
            throw new EimmlConversionException("Provided value " + value + " is not a valid RFC 3339 timestamp", e);
        }
    }

    /**
     * Returns the given value as a string.
     */
    public static String fromTimeStamp(Date value)
        throws EimmlConversionException {
        if (value == null)
            return null;
        return DateUtil.formatRfc3339TimeStamp(value);
    }

    /**
     * Returns the given value as a decimal.
     */
    public static BigDecimal toDecimal(String value)
        throws EimmlConversionException {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new EimmlConversionException("Provided value " + value + " is not a valid decimal", e);
        }
    }

    /**
     * Returns the given value as a string.
     *
     * @throws IllegalArgumentException if digits or decimal places
     * is not positive
     */
    public static String fromDecimal(BigDecimal value,
                                     int digits,
                                     int decimalPlaces)
        throws EimmlConversionException {
        if (digits <= 0)
            throw new IllegalArgumentException("Number of digits must be positive");
        if (decimalPlaces <= 0)
            throw new IllegalArgumentException("Number of decimal places must be positive");

        if (value == null)
            return null;

        StringBuffer pattern = new StringBuffer();
        for (int i=1; i<= digits; i++)
            pattern.append("#");
        pattern.append(".");
        for (int i=1; i<=decimalPlaces; i++)
            pattern.append("#");

        if (log.isDebugEnabled())
            log.debug("formatting decimal value " + value + " with pattern " +
                      pattern);

        return new DecimalFormat(pattern.toString()).format(value);
    }

    private static byte[] decodeHexedBase64String(String value)
        throws EimmlConversionException {
        try {
            return Base64.decodeBase64(Hex.decodeHex(value.toCharArray()));
        } catch (DecoderException e) {
            throw new EimmlConversionException("Error decoding byte array", e);
        }
    }

    private static String encodeHexedBase64String(byte[] value)
        throws EimmlConversionException {
        return new String(Hex.encodeHex(Base64.encodeBase64(value)));
    }
}
