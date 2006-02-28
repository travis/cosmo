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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final Log log = LogFactory.getLog(TimeRangeTermEnum.class);

    boolean endEnum = false;

    Term searchTerm = null;
    String field = "";
    String startTime = "";
    String endTime = "";
    String startFloat = "";
    String endFloat = "";

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

        StringTokenizer periods = new StringTokenizer(searchTerm.text(), ",");
        String absPeriod = periods.nextToken();
        String floatPeriod = periods.nextToken();

        StringTokenizer tokens1 = new StringTokenizer(absPeriod, "/");
        startTime = tokens1.nextToken();
        endTime = tokens1.nextToken();

        StringTokenizer tokens2 = new StringTokenizer(floatPeriod, "/");
        startFloat = tokens2.nextToken();
        endFloat = tokens2.nextToken();

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

                // Check whether floating or fixed test required
                // bug 5254: values are lowercased so we look for z
                // instead of Z
                boolean fixed = (testStart.indexOf('z') != -1);

                if (log.isDebugEnabled()) {
                    log.debug("testing " + token + " using " +
                              (fixed ? "fixed" : "floating") + " test");
                }

                // Period range compare
                if (testEnd != null) {
                    // Period overlaps
                    if (!((testStart.compareTo(fixed ? endTime : endFloat) >= 0) || (testEnd
                            .compareTo(fixed ? startTime : startFloat) <= 0)))
                        return true;
                } else {
                    // Time inside period
                    if ((testStart.compareTo(fixed ? startTime : startFloat) >= 0)
                            && (testStart.compareTo(fixed ? endTime : endFloat) < 0))
                        return true;
                }

                // Since the period list is sorted by increasing start time, if
                // the period we just tested is past the time-range we want,
                // there is no need to test any more as they can never match.
                if (testStart.compareTo(fixed ? endTime : endFloat) >= 0)
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
