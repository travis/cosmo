/*
 * Copyright 2004-2005 The Apache Software Foundation or its licensors,
 *                     as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.jackrabbit.query;

/**
 * Copyright 2004 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;

/**
 * Subclass of FilteredTermEnum for enumerating all terms that are similiar to
 * the specified filter term.
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
     * <code>reader</code> which share a prefix of length
     * <code>prefixLength</code> with <code>term</code> and which have a
     * fuzzy similarity &gt; <code>minSimilarity</code>.
     * 
     * @param reader
     *            Delivers terms.
     * @param term
     *            Pattern term.
     * @param minSimilarity
     *            Minimum required similarity for terms from the reader. Default
     *            value is 0.5f.
     * @param prefixLength
     *            Length of required common prefix. Default value is 0.
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
     * The termCompare method in FuzzyTermEnum uses Levenshtein distance to
     * calculate the distance between the given term and the comparing term.
     */
    protected final boolean termCompare(Term term) {
        if (field == term.field()) {

            // Try to parse term data into start/end period items, or just a
            // start (which may happen if querying a single date property)
            String termText = term.text();
            StringTokenizer tokens = new StringTokenizer(termText, "/");
            String testStart = tokens.nextToken();
            String testEnd = (tokens.hasMoreTokens() ? tokens.nextToken()
                    : null);

            // Period range compare
            if (testEnd != null) {
                // Period overlaps
                return !((testStart.compareTo(endTime) >= 0) || (testEnd
                        .compareTo(startTime) <= 0));
            } else {
                // Time inside period
                return (testStart.compareTo(startTime) >= 0)
                        && (testStart.compareTo(endTime) < 0);
            }
        }
        // endEnum = true;
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
