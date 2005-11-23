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

import org.apache.jackrabbit.core.query.QueryTreeBuilder;
import org.apache.jackrabbit.core.query.QueryRootNode;
import org.apache.jackrabbit.name.NamespaceResolver;

import javax.jcr.query.InvalidQueryException;

/**
 * Implements the XPath query tree builder with extension for a time-range
 * function in the query syntax.
 */
public class TimeRangeQueryBuilder implements QueryTreeBuilder {

    /**
     * Creates our special XPath+time-range query.
     * 
     * @inheritDoc
     */
    public QueryRootNode createQueryTree(String statement,
                                         NamespaceResolver resolver)
        throws InvalidQueryException {
        return XPathTimeRangeQueryBuilder.createQuery(statement, resolver);
    }

    /**
     * Checks whether the special XPath+time-range extended query is available.
     * 
     * @inheritDoc
     */
    public boolean canHandle(String language) {
        return XPathTimeRangeQueryBuilder.XPATH_TIMERANGE.equals(language);
    }

    /**
     * Generates a string representation of the extended query.
     * 
     * @inheritDoc
     */
    public String toString(QueryRootNode root, NamespaceResolver resolver)
        throws InvalidQueryException {
        return XPathTimeRangeQueryBuilder.toString(root, resolver);
    }
}
