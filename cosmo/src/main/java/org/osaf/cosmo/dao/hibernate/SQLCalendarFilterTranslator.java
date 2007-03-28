/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.dao.hibernate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.ComponentFilter;
import org.osaf.cosmo.calendar.query.ParamFilter;
import org.osaf.cosmo.calendar.query.PropertyFilter;
import org.osaf.cosmo.calendar.query.TextMatchFilter;
import org.osaf.cosmo.calendar.query.TimeRangeFilter;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;

/**
 * Used to generate a SQL query based on a CalendarFilter. A CalendarFilter
 * allows calendar content to be queried by property/parameter values and time
 * range. We have to use native SQL here because as of 3.2, Hibernate does not
 * support union and testing showed that unions provided the best response time.
 */
public class SQLCalendarFilterTranslator implements CalendarFilterTranslator {

    private static final Log log = LogFactory
            .getLog(SQLCalendarFilterTranslator.class);

    private String booleanTrueValue = "1";

    public void setBooleanTrueValue(String booleanTrueValue) {
        this.booleanTrueValue = booleanTrueValue;
    }

    /**
     * Generate Hibernate query to return calendar DbItems that match the given
     * filter.
     * 
     * @param session
     *            hibernate session
     * @param parentId
     *            database id of parent calendar
     * @param filter
     *            query filter
     * @return hibernate query
     */
    public Set<ContentItem> getCalendarItems(Session session,
            CollectionItem collection, CalendarFilter filter) {
        HashMap params = new HashMap();
        SQLQuery query = session.createSQLQuery(getQueryString(filter, params));
        query.setParameter("parentid", collection.getId());
        for (Iterator it = params.entrySet().iterator(); it.hasNext();) {
            Entry entry = (Entry) it.next();
            query.setParameter((String) entry.getKey(), entry.getValue());
        }
        query.addEntity(ContentItem.class);
        List<ContentItem> allItems = query.list();
        HashSet<ContentItem> returnedItems = new HashSet<ContentItem>();
        returnedItems.addAll(allItems);
        return returnedItems;
    }

    /**
     * Generate native SQL query to return all rows from item table that match
     * the given filter. The query contains named parameters.
     * 
     * @param filter
     *            query filter
     * @param params
     *            HashMap to store query parameters
     * @return SQL query
     */
    public String getQueryString(CalendarFilter filter, HashMap params) {

        StringBuffer buf = new StringBuffer();
        ComponentFilter rootFilter = filter.getFilter();

        String prefix = "icalendar:" + rootFilter.getName().toLowerCase();

        for (Iterator it = rootFilter.getComponentFilters().iterator(); it
                .hasNext();)
            generateCompFilterSQL(buf, (ComponentFilter) it.next(), prefix, "",
                    0, "", 0, params);

        for (Iterator it = rootFilter.getPropFilters().iterator(); it.hasNext();)
            generatePropFilterSQL(buf, (PropertyFilter) it.next(), prefix, "",
                    0, "", 0, params);

        return buf.toString();
    }

    private void generateCompFilterSQL(StringBuffer sqlBuf,
            ComponentFilter filter, String prefix, String attributeWhere,
            int attributeJoins, String timeRangeWhere, int timeRangeJoins,
            HashMap params) {
        String myprefix = prefix + "-" + filter.getName().toLowerCase();

        // road ends if this is defined
        if (filter.getIsNotDefinedFilter() != null) {
            attributeJoins++;
            attributeWhere += generateIsNotDefinedSQL(myprefix, attributeJoins,
                    params);

            if (sqlBuf.length() > 0)
                sqlBuf.append(" union ");

            sqlBuf.append(generateSQL(timeRangeWhere, timeRangeJoins,
                    attributeWhere, attributeJoins));
            return;
        }

        boolean hasTimeRangeFilter = filter.getTimeRangeFilter() != null;
        boolean hasPropFilters = filter.getPropFilters().size() > 0;
        boolean hasCompFilters = filter.getComponentFilters().size() > 0;

        // generate or add to timerange where
        if (hasTimeRangeFilter) {
            timeRangeJoins++;
            if (timeRangeJoins > 1)
                timeRangeWhere += " and ";

            timeRangeWhere += " and "
                    + getTimeRangeWhere(myprefix, filter.getTimeRangeFilter(),
                            timeRangeJoins, params);
        }

        // end of road if no other filters
        if (!hasCompFilters && !hasPropFilters) {
            if (!hasTimeRangeFilter) {
                attributeJoins++;
                attributeWhere += generateIsDefinedSQL(myprefix,
                        attributeJoins, params);
            }

            if (sqlBuf.length() > 0)
                sqlBuf.append(" union ");

            sqlBuf.append(generateSQL(timeRangeWhere, timeRangeJoins,
                    attributeWhere, attributeJoins));

            return;
        }

        if (hasPropFilters) {
            for (Iterator it = filter.getPropFilters().iterator(); it.hasNext();) {
                PropertyFilter propFilter = (PropertyFilter) it.next();
                generatePropFilterSQL(sqlBuf, propFilter, myprefix,
                        attributeWhere, attributeJoins, timeRangeWhere,
                        timeRangeJoins, params);
            }
        }

        if (hasCompFilters) {
            for (Iterator it = filter.getComponentFilters().iterator(); it
                    .hasNext();) {
                ComponentFilter compFilter = (ComponentFilter) it.next();
                generateCompFilterSQL(sqlBuf, compFilter, myprefix,
                        attributeWhere, attributeJoins, timeRangeWhere,
                        timeRangeJoins, params);
            }
        }

    }

    private void generatePropFilterSQL(StringBuffer sqlBuf,
            PropertyFilter propFilter, String prefix, String attributeWhere,
            int attributeJoins, String timeRangeWhere, int timeRangeJoins,
            HashMap params) {
        String myprefix = null;
        myprefix = prefix + "_" + propFilter.getName().toLowerCase();

        boolean hasParamFilters = propFilter.getParamFilters().size() > 0;
        boolean hasIsNotDefinedFilter = propFilter.getIsNotDefinedFilter() != null;
        boolean hasTextMatchFilter = propFilter.getTextMatchFilter() != null;
        boolean hasTimeRangeFilter = propFilter.getTimeRangeFilter() != null;

        // end of road if present
        if (hasIsNotDefinedFilter) {
            attributeJoins++;
            attributeWhere += generateIsNotDefinedSQL(myprefix, attributeJoins,
                    params);

            if (sqlBuf.length() > 0)
                sqlBuf.append(" union ");

            sqlBuf.append(generateSQL(timeRangeWhere, timeRangeJoins,
                    attributeWhere, attributeJoins));
            return;
        }

        // generate or add to timerange where
        if (hasTimeRangeFilter) {
            timeRangeJoins++;
            if (timeRangeJoins > 1)
                timeRangeWhere += " and ";

            timeRangeWhere += " and "
                    + getTimeRangeWhere(myprefix, propFilter
                            .getTimeRangeFilter(), timeRangeJoins, params);
        }

        // another end of road
        if (!hasIsNotDefinedFilter && !hasTextMatchFilter && !hasParamFilters) {
            if (!hasTimeRangeFilter) {
                attributeJoins++;
                attributeWhere += generateIsDefinedSQL(myprefix,
                        attributeJoins, params);
            }

            if (sqlBuf.length() > 0)
                sqlBuf.append(" union ");

            sqlBuf.append(generateSQL(timeRangeWhere, timeRangeJoins,
                    attributeWhere, attributeJoins));

            return;
        }

        if (hasTextMatchFilter) {
            TextMatchFilter textMatch = propFilter.getTextMatchFilter();
            attributeJoins++;
            attributeWhere += generateTextMatchSQL(myprefix, attributeJoins,
                    textMatch, params);
        }

        // Deepest we can go is a param filter
        if (hasParamFilters) {
            for (Iterator it = propFilter.getParamFilters().iterator(); it
                    .hasNext();) {
                ParamFilter paramFilter = (ParamFilter) it.next();
                generateParamFilterSQL(sqlBuf, paramFilter, myprefix,
                        attributeWhere, attributeJoins, timeRangeWhere,
                        timeRangeJoins, params);
            }
            return;
        }

        // else we generate the query and return
        if (sqlBuf.length() > 0)
            sqlBuf.append(" union ");

        sqlBuf.append(generateSQL(timeRangeWhere, timeRangeJoins,
                attributeWhere, attributeJoins));
    }

    private void generateParamFilterSQL(StringBuffer sqlBuf,
            ParamFilter paramFilter, String prefix, String attributeWhere,
            int attributeJoins, String timeRangeWhere, int timeRangeJoins,
            HashMap params) {
        String myprefix = null;
        myprefix = prefix + "_" + paramFilter.getName().toLowerCase();

        boolean hasIsNotDefinedFilter = paramFilter.getIsNotDefinedFilter() != null;
        boolean hasTextMatchFilter = paramFilter.getTextMatchFilter() != null;

        if (hasIsNotDefinedFilter) {
            attributeJoins++;
            attributeWhere += generateIsNotDefinedSQL(myprefix, attributeJoins,
                    params);

            if (sqlBuf.length() > 0)
                sqlBuf.append(" union ");

            sqlBuf.append(generateSQL(timeRangeWhere, timeRangeJoins,
                    attributeWhere, attributeJoins));
            return;
        }

        if (hasTextMatchFilter) {
            TextMatchFilter textMatch = paramFilter.getTextMatchFilter();

            attributeJoins++;
            attributeWhere += generateTextMatchSQL(myprefix, attributeJoins,
                    textMatch, params);

            if (sqlBuf.length() > 0)
                sqlBuf.append(" union ");

            sqlBuf.append(generateSQL(timeRangeWhere, timeRangeJoins,
                    attributeWhere, attributeJoins));
            return;
        }

        if (!hasIsNotDefinedFilter && !hasTextMatchFilter) {
            attributeJoins++;
            attributeWhere += generateIsDefinedSQL(myprefix, attributeJoins,
                    params);

            if (sqlBuf.length() > 0)
                sqlBuf.append(" union ");

            sqlBuf.append(generateSQL(timeRangeWhere, timeRangeJoins,
                    attributeWhere, attributeJoins));
            return;
        }
    }

    private String getTimeRangeWhere(String prefix,
            TimeRangeFilter timeRangeFilter, int index, HashMap params) {
        String myprefix = prefix + "--timerange";
        StringBuffer buf = new StringBuffer();
        buf.append("(ctri" + index + ".componenttype = :param" + params.size());
        params.put("param" + params.size(), myprefix);
        buf.append(" and ((ctri" + index + ".startdate < case when ctri"
                + index + ".isfloating=" + booleanTrueValue + " then '"
                + timeRangeFilter.getFloatEnd() + "'");

        buf.append(" else :param" + params.size() + " end");
        params.put("param" + params.size(), timeRangeFilter.getUTCEnd());

        buf.append(" and ctri" + index + ".enddate > case when ctri" + index
                + ".isfloating=" + booleanTrueValue + " then '"
                + timeRangeFilter.getFloatStart() + "'");

        buf.append(" else :param" + params.size() + " end");
        params.put("param" + params.size(), timeRangeFilter.getUTCStart());

        buf.append(") or (");
        buf.append("ctri" + index + ".startdate >= case when ctri" + index
                + ".isfloating=" + booleanTrueValue + " then '"
                + timeRangeFilter.getFloatStart() + "'");

        buf.append(" else :param" + params.size() + " end");
        params.put("param" + params.size(), timeRangeFilter.getUTCStart());

        buf.append(" and ctri" + index + ".startdate < case when ctri" + index
                + ".isfloating=" + booleanTrueValue + " then '"
                + timeRangeFilter.getFloatEnd() + "'");
        // params.put("param" + paramCount, timeRangeFilter.getFloatEnd());
        // paramCount++;
        buf.append(" else :param" + params.size() + " end)))");
        params.put("param" + params.size(), timeRangeFilter.getUTCEnd());

        return buf.toString();
    }

    private String generateSQL(String timeRangeWhere, int timeRangeJoins,
            String attributeWhere, int attributeJoins) {
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct i.* from item i, collection_item ci");

        for (int i = 1; i <= attributeJoins; i++)
            buf.append(", cal_property_index cpi" + i);

        for (int i = 1; i <= timeRangeJoins; i++)
            buf.append(", cal_timerange_index ctri" + i);

        buf.append(" where ci.itemid=i.id and ci.collectionid=:parentid and i.isactive=1");

        for (int i = 1; i <= attributeJoins; i++)
            buf.append(" and cpi" + i + ".itemid=i.id");

        for (int i = 1; i <= timeRangeJoins; i++)
            buf.append(" and ctri" + i + ".itemid=i.id");

        buf.append(attributeWhere);
        buf.append(timeRangeWhere);

        return buf.toString();
    }

    private String generateIsNotDefinedSQL(String prefix, int joinIndex,
            HashMap params) {
        String sql = " and cpi" + joinIndex + ".propertyname <> :param"
                + params.size();
        params.put("param" + params.size(), prefix);
        return sql;
    }

    private String generateTextMatchSQL(String prefix, int joinIndex,
            TextMatchFilter textMatch, HashMap params) {
        String sql = " and cpi" + joinIndex + ".propertyname=:param"
                + params.size();
        params.put("param" + params.size(), prefix);

        if (textMatch.isCaseless())
            sql += " and lower(cpi" + joinIndex + ".propertyvalue) ";
        else
            sql += " and cpi" + joinIndex + ".propertyvalue ";

        if (textMatch.isNegateCondition())
            sql += "not ";

        sql += "like :param" + params.size();

        if (textMatch.isCaseless())
            params.put("param" + params.size(), "%"
                    + textMatch.getValue().toLowerCase() + "%");
        else
            params.put("param" + params.size(), "%" + textMatch.getValue()
                    + "%");

        return sql;
    }

    private String generateIsDefinedSQL(String prefix, int joinIndex,
            HashMap params) {
        String sql = " and cpi" + joinIndex + ".propertyname=:param"
                + params.size();
        params.put("param" + params.size(), prefix);
        return sql;
    }

}
