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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.MultiTermQuery;

import java.io.IOException;

/** Implements the fuzzy search query. The similiarity measurement
 * is based on the Levenshtein (edit distance) algorithm.
 */
public final class TimeRangeQuery extends MultiTermQuery {
    
  /**
     * Create a new FuzzyQuery that will match terms with a similarity of at
     * least <code>minimumSimilarity</code> to <code>term</code>. If a
     * <code>prefixLength</code> &gt; 0 is specified, a common prefix of that
     * length is also required.
     * 
     * @param term
     *            the term to search for
     * @param minimumSimilarity
     *            a value between 0 and 1 to set the required similarity between
     *            the query term and the matching terms. For example, for a
     *            <code>minimumSimilarity</code> of <code>0.5</code> a term
     *            of the same length as the query term is considered similar to
     *            the query term if the edit distance between both terms is less
     *            than <code>length(term)*0.5</code>
     * @param prefixLength
     *            length of common (non-fuzzy) prefix
     * @throws IllegalArgumentException
     *             if minimumSimilarity is &gt; 1 or &lt; 0 or if prefixLength
     *             &lt; 0 or &gt; <code>term.text().length()</code>.
     */
    public TimeRangeQuery(Term term)
        throws IllegalArgumentException {
        super(term);
    }
  
  protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
    return new TimeRangeTermEnum(reader, getTerm());
  }
}

