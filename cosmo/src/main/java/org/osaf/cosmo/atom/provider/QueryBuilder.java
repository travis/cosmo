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
package org.osaf.cosmo.atom.provider;

import java.text.ParseException;
import java.util.Date;

import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.util.DateUtil;

/**
 * A utility class for constructing query information based on a
 * request context.
 *
 * @see ItemFilter
 * @see RequestContext
 */
public class QueryBuilder {
    private static final Log log = LogFactory.getLog(QueryBuilder.class);

    /**
     * <p>
     * Constructs an item filter based on the information provided
     * in the request context.
     * </p>
     * <p>
     * The returned filter specifies all events that
     * begin with the time range specified by the
     * <code>start</code> and <code>end</code> request
     * parameters. These parameter values are both required and must
     * be in RFC 3339 format.
     *
     * @param context the request context
     * @throws InvalidQueryException if the required parameters are
     * not found or cannot be parsed
     */
    public static NoteItemFilter buildFilter(RequestContext request)
        throws InvalidQueryException {
        String isostart = request.getParameter("start");
        if (StringUtils.isBlank(isostart))
            isostart = null;
        String isoend = request.getParameter("end");
        if (StringUtils.isBlank(isoend))
            isoend = null;

        if (isostart == null && isoend == null)
            return null;
        if (isostart == null || isoend == null)
            throw new InvalidQueryException("Both start and end parameters must be provided");

        Date start = parseDateParameter(isostart);
        Date end = parseDateParameter(isoend);
        
        NoteItemFilter itemFilter = new NoteItemFilter();
        EventStampFilter eventFilter = new EventStampFilter();
        eventFilter.setTimeRange(start, end);
        eventFilter.setExpandRecurringEvents(true);

        itemFilter.getStampFilters().add(eventFilter);
        
        return itemFilter;
    }
  
    private static Date parseDateParameter(String value)
        throws InvalidQueryException {
        try {
            return DateUtil.parseRfc3339Calendar(value).getTime();
        } catch (ParseException e) {
            throw new InvalidQueryException("Unable to parse date value " + value, e);
        }
    }
}
