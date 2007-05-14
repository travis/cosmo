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

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.regex.Pattern;

import net.fortuna.ical4j.model.Dur;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Parses and formats EIM duration text values.
 * <p>
 * EIM uses the iCalendar DURATION value type as per section 4.3.6 of
 * RFC 2445.
 * <p>
 * @see Dur
 */
public class DurationFormat extends Format {
    private static final Log log =
        LogFactory.getLog(DurationFormat.class);

    private static final Pattern PATTERN = Pattern.
        compile("^[+-]?P((\\d+W)|(\\d+D)|((\\d+D)?(T(\\d+H)?(\\d+M)?(\\d+S)?))){1}$");

    private ParseException parseException;

    private DurationFormat() {
    }

    public final String format(Dur dur) {
        return super.format(dur);
    }

    public StringBuffer format(Object obj,
                               StringBuffer toAppendTo,
                               FieldPosition pos) {
        if (obj == null)
            return toAppendTo;
        if (! (obj instanceof Dur))
            throw new IllegalArgumentException("object not a Dur");
        Dur dur = (Dur) obj;

        toAppendTo.append(dur.toString());

        return toAppendTo;
    }

    public Dur parse(String source)
        throws ParseException {
        if(source==null || "".equals(source))
            return null;
        Dur dur = (Dur) super.parseObject(source);
        if (dur != null)
            return dur;
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

        if (! PATTERN.matcher(source).matches()) {
            parseException =
                new ParseException("Invalid duration " + source, 0);
            pos.setErrorIndex(0);
            return null;
        }

        Dur dur = new Dur(source);

        pos.setIndex(source.length());

        return dur;
    }

    public static final DurationFormat getInstance() {
        return new DurationFormat();
    }
}
