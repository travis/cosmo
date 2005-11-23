/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.cosmo.jackrabbit.query;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.MultiTermQuery;

import java.io.IOException;

/**
 * Implements the iCalendar time-range query.
 */
public final class TimeRangeQuery extends MultiTermQuery {

    /**
     * Default constructor.
     * 
     * @param term
     *            the time-range test to use
     * @throws IllegalArgumentException
     */
    public TimeRangeQuery(Term term)
        throws IllegalArgumentException {
        super(term);
    }

    /**
     * Creates the special time-range term enumerator for use with this query.
     * 
     * @param reader
     *            the index being searched
     */
    protected FilteredTermEnum getEnum(IndexReader reader)
        throws IOException {
        return new TimeRangeTermEnum(reader, getTerm());
    }
}
