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

import junit.framework.TestCase;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DurationFormatTest extends TestCase {
    private static final Log log = LogFactory.getLog(DurationFormatTest.class);

    public void testFormat() throws Exception {
        DurationFormat df = DurationFormat.getInstance();
        Dur dur = null;

        dur = makeDur("20070512", "20070513");
        assertEquals("P1D", df.format(dur));

        dur = makeDur("20070512T103000", "20070513T103000");
        assertEquals("P1D", df.format(dur));

        dur = makeDur("20070512T103000", "20070512T113000");
        assertEquals("PT1H", df.format(dur));

        dur = makeDur("20070512T103000", "20070512T103500");
        assertEquals("PT5M", df.format(dur));

        dur = makeDur("20070512T103000", "20070512T113500");
        assertEquals("PT1H5M", df.format(dur));

        dur = makeDur("20070512T103000", "20070512T103030");
        assertEquals("PT30S", df.format(dur));
    }

    private Dur makeDur(String start,
                        String end)
        throws Exception {
        if (start.indexOf("T") > 0)
            return new Dur(new DateTime(start), new DateTime(end));
        return new Dur(new Date(start), new Date(end));
    }
}
