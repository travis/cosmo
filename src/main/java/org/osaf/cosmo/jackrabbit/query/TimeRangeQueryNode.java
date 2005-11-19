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

import java.util.List;

import javax.jcr.NamespaceException;
import javax.jcr.query.Query;

import org.apache.jackrabbit.core.query.CustomQueryNode;
import org.apache.jackrabbit.core.query.QueryNode;
import org.apache.jackrabbit.core.query.QueryNodeVisitor;
import org.apache.jackrabbit.core.query.lucene.FieldNames;
import org.apache.jackrabbit.core.query.lucene.NamespaceMappings;
import org.apache.jackrabbit.core.util.ISO9075;
import org.apache.jackrabbit.name.NamespaceResolver;
import org.apache.jackrabbit.name.NoPrefixDeclaredException;
import org.apache.jackrabbit.name.QName;
import org.apache.lucene.index.Term;

/**
 * Implements a query node that defines a time-range clause.
 */
public class TimeRangeQueryNode extends CustomQueryNode {

    public static final String TYPE_TIMERANGE = "TIMERANGE";

    /**
     * The period statement derived from the time-range clause
     */
    private final String period;

    /**
     * Limits the scope of this time-range clause to properties with this name.
     * If <code>null</code> the scope of this time-range clause is the fulltext
     * index of all properties of a node.
     */
    private QName propertyName;

    /**
     * Creates a new <code>time-rangeQueryNode</code> with a <code>parent</code>
     * and a time-range <code>query</code> statement. The scope of the query
     * is the fulltext index of the node, that contains all properties.
     *
     * @param parent the parent node of this query node.
     * @param query  the time-range statement.
     */
    public TimeRangeQueryNode(QueryNode parent, String period) {
        this(parent, period, null);
    }

    /**
     * Creates a new <code>time-rangeQueryNode</code> with a <code>parent</code>
     * and a time-range <code>query</code> statement. The scope of the query
     * is property with name <code>propertyName</code>.
     *
     * @param parent the parent node of this query node.
     * @param query  the time-range statement.
     * @param propertyName scope of the fulltext search.
     */
    public TimeRangeQueryNode(QueryNode parent, String period, QName propertyName) {
        super(parent);
        this.period = period;
        this.propertyName = propertyName;
    }

    /**
     * {@inheritDoc}
     */
    public Object accept(QueryNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public String getCustomType() {
        // This type of node behaves like a TextSearchQueryNode
        return TYPE_TIMERANGE;
    }

    /**
     * Returns the time-range statement.
     *
     * @return the time-range statement.
     */
    public String getPeriod() {
        return period;
    }

    /**
     * Returns a property name if the scope is limited to just a single property
     * or <code>null</code> if the scope is spawned across all properties of a
     * node.
     *
     * @return property name or <code>null</code>.
     */
    public QName getPropertyName() {
        return propertyName;
    }

    /**
     * Sets a new name as the search scope for this fulltext query.
     *
     * @param property the name of the property.
     */
    public void setPropertyName(QName property) {
        this.propertyName = property;
    }

    /**
     * @inheritDoc
     */
    public boolean equals(Object obj) {
        if (obj instanceof TimeRangeQueryNode) {
            TimeRangeQueryNode other = (TimeRangeQueryNode) obj;
            return (period == null ? other.period == null : period.equals(other.period))
                    && (propertyName == null ? other.propertyName == null : propertyName.equals(other.propertyName));
        }
        return false;
    }

    public Object build(Object data, NamespaceMappings nsMappings, List exceptions) {
        try {
            String fieldname;
            if (getPropertyName() == null) {
                // fulltext on node
                fieldname = FieldNames.FULLTEXT;
            } else {
                StringBuffer tmp = new StringBuffer();
                tmp.append(nsMappings.getPrefix(getPropertyName()
                        .getNamespaceURI()));
                tmp.append(":").append(FieldNames.FULLTEXT_PREFIX);
                tmp.append(getPropertyName().getLocalName());
                fieldname = tmp.toString();
            }
            return new TimeRangeQuery(new Term(fieldname, getPeriod()));
        } catch (NamespaceException e) {
            exceptions.add(e);
        }
        return null;
    }

    public void format(String type, StringBuffer buffer, NamespaceResolver resolver, List exceptions) {
        if (Query.XPATH.equals(type)) {
            formatXPath(buffer, resolver, exceptions);
        } else if (Query.SQL.equals(type)) {
            formatSQL(buffer, resolver, exceptions);
        }
    }

    private void formatXPath(StringBuffer buffer, NamespaceResolver resolver, List exceptions) {
        try {
            buffer.append(XPathTimeRangeQueryBuilder.JCR_TIMERANGE.toJCRName(resolver));
            buffer.append("(");
            if (getPropertyName() == null) {
                buffer.append(".");
            } else {
                buffer.append(ISO9075.encode(getPropertyName()).toJCRName(resolver));
            }
            buffer.append(", '");
            buffer.append(getPeriod().replaceAll("'", "''"));
            buffer.append("')");
        } catch (NoPrefixDeclaredException e) {
            exceptions.add(e);
        }
    }

    private void formatSQL(StringBuffer buffer, NamespaceResolver resolver, List exceptions) {
        // escape quote
        String period = getPeriod().replaceAll("'", "''");
        buffer.append("TIMERANGE(");
        if (getPropertyName() == null) {
            buffer.append("*");
        } else {
            try {
                boolean quote = getPropertyName().getLocalName().indexOf(' ') > -1;
                if (quote) {
                    buffer.append('"');
                }
                buffer.append(getPropertyName().toJCRName(resolver));
                if (quote) {
                    buffer.append('"');
                }
            } catch (NoPrefixDeclaredException e) {
                exceptions.add(e);
            }
        }
        buffer.append(", '");
        buffer.append(period).append("')");
    }

    public void dump(StringBuffer buffer) {
        buffer.append("+ TimeRangeQueryNode: ");
        buffer.append(" Period=").append(getPeriod());
        buffer.append("\n");
    }
}
