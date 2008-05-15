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
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Test Case for {@link EimmlTypeConverter}.
 */
public class EimmlTypeConverterTest extends TestCase
    implements EimmlConstants {
    private static final Log log =
        LogFactory.getLog(EimmlTypeConverterTest.class);

    /** */
    public void testToBytes() throws Exception {
        String testString = "this is a test string";

        byte[] testBytes = testString.getBytes();
        String testEncoded = new String(Base64.encodeBase64(testBytes));
        byte[] resultBytes = EimmlTypeConverter.toBytes(testEncoded);

        for (int i=0; i<resultBytes.length; i++)
            assertEquals("Byte " + i + " does not match", testBytes[i],
                         resultBytes[i]);
    }

    /** */
    public void testfromBytes() throws Exception {
        String testString = "this is a test string";
        byte[] testBytes = testString.getBytes();

        String resultEncoded = EimmlTypeConverter.fromBytes(testBytes);

        assertEquals("Encoded string does not match",
                     new String(Base64.encodeBase64(testBytes)),
                     resultEncoded);
    }

    /** */
    public void testToText() throws Exception {
        String testString = "this is a test string";

        try {
            EimmlTypeConverter.toText(testString, null);
            fail("Converted to text with a null original encoding");
        } catch (IllegalArgumentException e) {}

        try {
            EimmlTypeConverter.toText(testString, "bogus-encoding");
            fail("Converted to text with a bogus original encoding");
        } catch (EimmlConversionException e) {}

        String resultString = EimmlTypeConverter.toText(testString, "UTF-8");
        assertEquals("Result UTF-8 string does not match", testString,
                     resultString);

        // XXX: encode testString with a non UTF-8 encoding 
    }

    /** */
    public void testToClob() throws Exception {
        String testString = "this is a test string";
        byte[] testBytes = testString.getBytes();

        Reader resultReader = EimmlTypeConverter.toClob(testString);

        for (int i=0; i<testBytes.length; i++) {
            int rv = resultReader.read();
            if  (rv == -1)
                fail("Result reader is shorter than test string");
            assertEquals("Byte " + i + " does not match", testBytes[i], rv);
        }
    }

    /** */
    public void testFromClob() throws Exception {
        String testString = "this is a test string";
        StringReader testReader = new StringReader(testString);

        String resultString = EimmlTypeConverter.fromClob(testReader);

        assertEquals("Result string does not match", testString, resultString);
    }

    /** */
    public void testToBlob() throws Exception {
        String testString = "this is a test string";

        byte[] testBytes = testString.getBytes();
        String testEncoded = new String(Base64.encodeBase64(testBytes));
        InputStream resultStream = EimmlTypeConverter.toBlob(testEncoded);

        for (int i=0; i<testBytes.length; i++) {
            int rv = resultStream.read();
            if  (rv == -1)
                fail("Result stream is shorter than test string");
            assertEquals("Byte " + i + " does not match", testBytes[i], rv);
        }
    }

    /** */
    public void testFromBlob() throws Exception {
        String testString = "this is a test string";
        byte[] testBytes = testString.getBytes();
        String testEncoded =
            new String(Base64.encodeBase64(testBytes));
        ByteArrayInputStream testStream = new ByteArrayInputStream(testBytes);

        String resultEncoded = EimmlTypeConverter.fromBlob(testStream);

        assertEquals("Result string does not match", testEncoded,
                     resultEncoded);
    }

    /** */
    public void testToInteger() throws Exception {
        String testString = "42";

        Integer resultInteger = EimmlTypeConverter.toInteger(testString);

        assertEquals("Result integer does not match", new Integer(testString),
                     resultInteger);
    }

    /** */
    public void testFromInteger() throws Exception {
        String testString = "42";
        Integer testInteger = new Integer(testString);

        String resultString = EimmlTypeConverter.fromInteger(testInteger);

        assertEquals("Result string does not match", testString, resultString);
    }

    /** */
    public void testToDateTime() throws Exception {
        String testString = "1996-12-19T16:39:57-08:00";

        Calendar resultCalendar = EimmlTypeConverter.toDateTime(testString);

        // since the test string's timezone is not actually parsed,
        // set the result calendar's timezone explicitly so that we
        // don't get variable numbers based on the system default
        // timezone
        resultCalendar.setTimeZone(TimeZone.getTimeZone("GMT-8"));

        assertEquals("Result year does not match", 1996,
                     resultCalendar.get(Calendar.YEAR));
        assertEquals("Result month does not match", 11,
                     resultCalendar.get(Calendar.MONTH));
        assertEquals("Result day of week does not match", 19,
                     resultCalendar.get(Calendar.DAY_OF_MONTH));
        assertEquals("Result hour of day does not match", 16,
                     resultCalendar.get(Calendar.HOUR_OF_DAY));
        assertEquals("Result minute does not match", 39,
                     resultCalendar.get(Calendar.MINUTE));
        assertEquals("Result second does not match", 57,
                     resultCalendar.get(Calendar.SECOND));
    }

    /** */
    public void testFromDateTime() throws Exception {
        String testString = "1996-12-19T16:39:57-08:00";

        Calendar testCalendar =
            Calendar.getInstance(TimeZone.getTimeZone("GMT-8"));
        testCalendar.set(Calendar.YEAR, 1996);
        testCalendar.set(Calendar.MONTH, 11);
        testCalendar.set(Calendar.DAY_OF_MONTH, 19);
        testCalendar.set(Calendar.HOUR_OF_DAY, 16);
        testCalendar.set(Calendar.MINUTE, 39);
        testCalendar.set(Calendar.SECOND, 57);

        String resultString = EimmlTypeConverter.fromDateTime(testCalendar);

        assertEquals("Result string does not match", testString, resultString);
    }

    /** */
    public void testToDecimal() throws Exception {
        try {
            EimmlTypeConverter.toDecimal("deadbeef");
            fail("converted to decimal with a bogus string");
        } catch (EimmlConversionException e) {}

        String testString = "3.14159";
        BigDecimal resultDecimal = EimmlTypeConverter.toDecimal(testString);

        assertEquals("Result decimal does not match",
                     resultDecimal.toString(), testString);
    }

    /** */
    public void testFromDecimal() throws Exception {
        String testString = "3.14159";
        BigDecimal testDecimal = new BigDecimal(testString);

        try {
            EimmlTypeConverter.fromDecimal(testDecimal, -1, 1);
            fail("converted to decimal with negative digits");
        } catch (IllegalArgumentException e) {}

        try {
            EimmlTypeConverter.fromDecimal(testDecimal, 1, -1);
            fail("converted to decimal with negative decimal places");
        } catch (IllegalArgumentException e) {}

        String resultString =
            EimmlTypeConverter.fromDecimal(testDecimal, 1, 5);

        assertEquals("Result string does not match", testString, resultString);
    }
}
