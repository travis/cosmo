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
package org.osaf.cosmo.eim.schema.text;

import java.math.BigDecimal;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.TriageStatusUtil;
import org.osaf.cosmo.eim.schema.EimValidationException;

/**
 * Parses and formats EIM triage status text values.
 * <p>
 * Triage status is formatted as such:
 * <p>
 * <pre>
 * <code> <rank> <bit>
 * </pre>
 * <p>
 * @see TriageStatus
 */
public class TriageStatusFormat extends Format {
    private static final Log log =
        LogFactory.getLog(TriageStatusFormat.class);

    public static int CODE_FIELD = 1;
    public static int RANK_FIELD = 2;
    public static int AUTOTRIAGE_FIELD = 3;

    private static String AUTOTRIAGE_ON = "1";
    private static String AUTOTRIAGE_OFF = "0";

    private ParseException parseException;
    private EntityFactory entityFactory;
    
    private TriageStatusFormat(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    public final String format(TriageStatus ts) {
        return super.format(ts);
    }

    public StringBuffer format(Object obj,
                               StringBuffer toAppendTo,
                               FieldPosition pos) {
        if (obj == null)
            return toAppendTo;
        if (! (obj instanceof TriageStatus))
            throw new IllegalArgumentException("object not a TriageStatus");
        TriageStatus ts = (TriageStatus) obj;

        int begin = -1;
        int end = -1;

        Integer code = ts.getCode();
        if (code != null)
            // validate that this is a known code; throws
            // IllegalArgumentException if not
            TriageStatusUtil.label(code);
        else
            code = new Integer(-1);

        if (pos.getField() == CODE_FIELD)
            begin = toAppendTo.length();
        toAppendTo.append(code);
        if (pos.getField() == CODE_FIELD)
            end = toAppendTo.length() - 1;

        toAppendTo.append(" ");

        BigDecimal rank = ts.getRank();
        if (rank == null)
            rank = BigDecimal.ZERO;
        rank.setScale(2);

        if (pos.getField() == RANK_FIELD)
            begin = toAppendTo.length();
        toAppendTo.append(rank);
        if (pos.getField() == RANK_FIELD)
            end = toAppendTo.length() - 1;
        
        toAppendTo.append(" ");

        String autoTriage = BooleanUtils.isTrue(ts.getAutoTriage()) ?
            AUTOTRIAGE_ON : AUTOTRIAGE_OFF;

        if (pos.getField() == AUTOTRIAGE_FIELD)
            begin = toAppendTo.length();
        toAppendTo.append(autoTriage);
        if (pos.getField() == AUTOTRIAGE_FIELD)
            end = toAppendTo.length() - 1;
        
        if (pos != null) {
            pos.setBeginIndex(begin);
            pos.setEndIndex(end);
        }

        return toAppendTo;
    }

    public TriageStatus parse(String source)
        throws ParseException {
        TriageStatus ts = (TriageStatus) super.parseObject(source);
        if (ts != null)
            return ts;
        if (parseException != null)
            throw parseException;
        if (log.isDebugEnabled())
            log.debug("Unknown error parsing " + source);
        return null;
    }

    public Object parseObject(String source,
                              ParsePosition pos) {
        if (pos.getIndex() > 0)
            return null;

        int index = 0;

        String[] chunks = source.split(" ", 3);
        if (chunks.length != 3) {
            parseException = new ParseException("Incorrect number of chunks: " + chunks.length, 0);
            pos.setErrorIndex(index);
            return null;
        }

        TriageStatus ts = entityFactory.createTriageStatus();

        try {
            pos.setIndex(index);
            Integer code = new Integer(chunks[0]);
            // validate the code as being known
            TriageStatusUtil.label(code);
            ts.setCode(code);
            index += chunks[0].length() + 1;
        } catch (Exception e) {
            parseException = new ParseException(e.getMessage(), 0);
            pos.setErrorIndex(index);
            return null;
        }

        try {
            pos.setIndex(index);
            BigDecimal rank = new BigDecimal(chunks[1]);
            if (rank.scale() != 2)
                throw new NumberFormatException("Invalid rank value " + chunks[1]);
            ts.setRank(rank);
            index += chunks[1].length() + 1;
        } catch (NumberFormatException e) {
            parseException = new ParseException(e.getMessage(), 0);
            pos.setErrorIndex(index);
            return null;
        }

        if (chunks[2].equals(AUTOTRIAGE_ON))
            ts.setAutoTriage(Boolean.TRUE);
        else if (chunks[2].equals(AUTOTRIAGE_OFF))
            ts.setAutoTriage(Boolean.FALSE);
        else {
            parseException = new ParseException("Invalid autotriage value " + chunks[2], 0);
            pos.setErrorIndex(index);
            return null;
        }
        index += chunks[2].length();

        pos.setIndex(index);

        return ts;
    }

    public static final TriageStatusFormat getInstance(EntityFactory entityFactory) {
        return new TriageStatusFormat(entityFactory);
    }
}
