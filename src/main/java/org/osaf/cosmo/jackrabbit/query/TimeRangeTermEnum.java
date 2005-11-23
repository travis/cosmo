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

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;

/**
 * Subclass of FilteredTermEnum for enumerating all terms that overlap the
 * specified period.
 * 
 * <p>
 * Term enumerations are always ordered by Term.compareTo(). Each term in the
 * enumeration is greater than all that precede it.
 */
public final class TimeRangeTermEnum extends FilteredTermEnum {
    boolean endEnum = false;

    Term searchTerm = null;
    String field = "";
    String startTime = "";
    String endTime = "";

    /**
     * Constructor for enumeration of all terms from specified
     * <code>reader</code> which overlap the specified period.
     * 
     * @param reader
     *            Delivers terms.
     * @param term
     *            Period details term.
     * @throws IOException
     */
    public TimeRangeTermEnum(IndexReader reader, Term term)
        throws IOException {
        super();
        searchTerm = term;
        field = searchTerm.field();
        StringTokenizer tokens = new StringTokenizer(searchTerm.text(), "/");
        startTime = tokens.nextToken();
        endTime = tokens.nextToken();
        setEnum(reader.terms(new Term(searchTerm.field(), "")));
    }

    /**
     * The termCompare method checks to see if there is overlap between the
     * index term's date or period values (could be multiple) and the specified
     * period for the enum.
     */
    protected final boolean termCompare(Term term) {
        if (field == term.field()) {

            // Index may consist of a comma-separated list of items (e.g.
            // recurring events)
            String termText = term.text();
            StringTokenizer periodTokens = new StringTokenizer(termText, ",");
            while (periodTokens.hasMoreTokens()) {
                // Try to parse term data into start/end period items, or just a
                // start (which may happen if querying a single date property)
                String token = periodTokens.nextToken();
                int slashPos = token.indexOf('/');
                String testStart = (slashPos != -1) ? token.substring(0,
                        slashPos) : token;
                String testEnd = (slashPos != -1) ? token
                        .substring(slashPos + 1) : null;

                // Period range compare
                if (testEnd != null) {
                    // Period overlaps
                    if (!((testStart.compareTo(endTime) >= 0) || (testEnd
                            .compareTo(startTime) <= 0)))
                        return true;
                } else {
                    // Time inside period
                    if ((testStart.compareTo(startTime) >= 0)
                            && (testStart.compareTo(endTime) < 0))
                        return true;
                }

                // Since the period list is sorted by increasing start time, if
                // the period we just tested is past the time-range we want,
                // there is no need to test any more as they can never match.
                if (testStart.compareTo(endTime) >= 0)
                    break;
            }

            return false;

        }
        endEnum = true;
        return false;
    }

    protected final float difference() {
        return 1.0f;
    }

    public final boolean endEnum() {
        return endEnum;
    }

    public void close()
        throws IOException {
        super.close();
        searchTerm = null;
        field = null;
        startTime = null;
        endTime = null;
    }
}
