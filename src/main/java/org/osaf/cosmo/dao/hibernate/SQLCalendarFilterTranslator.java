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
import java.util.Iterator;
import java.util.List;
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
import org.osaf.cosmo.model.CalendarItem;

/**
 * Used to generate a SQL query based on a CalendarFilter. A CalendarFilter
 * allows calendar content to be queried by property/parameter values and time
 * range. We have to use native SQL here because as of 3.2, Hibernate does not
 * support union and testing showed that unions provided the best response time.
 */
public class SQLCalendarFilterTranslator implements CalendarFilterTranslator {

    private static final Log log = LogFactory
            .getLog(SQLCalendarFilterTranslator.class);

    private HashMap params = new HashMap();

    private int paramCount = 0;

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
    public List<CalendarItem> getCalendarItems(Session session, Long parentId, CalendarFilter filter) {
        SQLQuery query = session.createSQLQuery(getQueryString(filter));
        query.setParameter("parentid", parentId);
        for (Iterator it = params.entrySet().iterator(); it.hasNext();) {
            Entry entry = (Entry) it.next();
            query.setParameter((String) entry.getKey(), entry.getValue());
        }
        query.addEntity(CalendarItem.class);
        return (List<CalendarItem>) query.list();
    }

    /**
     * Generate native SQL query to return all rows from item table that match
     * the given filter. The query contains named parameters.
     * 
     * @param filter
     *            query filter
     * @return SQL query
     */
    public String getQueryString(CalendarFilter filter) {
        params.clear();
        paramCount = 0;
        StringBuffer buf = new StringBuffer();
        ComponentFilter rootFilter = filter.getFilter();

        String prefix = "icalendar:" + rootFilter.getName().toLowerCase();

        for (Iterator it = rootFilter.getComponentFilters().iterator(); it
                .hasNext();)
            generateCompFilterSQL(buf, (ComponentFilter) it.next(), prefix, "",
                    0, "", 0);

        for (Iterator it = rootFilter.getPropFilters().iterator(); it.hasNext();)
            generatePropFilterSQL(buf, (PropertyFilter) it.next(), prefix, "",
                    0, "", 0);

        return buf.toString();
    }

    private void generateCompFilterSQL(StringBuffer sqlBuf,
            ComponentFilter filter, String prefix, String attributeWhere,
            int attributeJoins, String timeRangeWhere, int timeRangeJoins) {
        String myprefix = prefix + "-" + filter.getName().toLowerCase();

        // road ends if this is defined
        if (filter.getIsNotDefinedFilter() != null) {
            attributeJoins++;
            attributeWhere += generateIsNotDefinedSQL(myprefix, attributeJoins);

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
                            timeRangeJoins);
        }

        // end of road if no other filters
        if (!hasCompFilters && !hasPropFilters) {
            if (!hasTimeRangeFilter) {
                attributeJoins++;
                attributeWhere += generateIsDefinedSQL(myprefix, attributeJoins);
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
                        timeRangeJoins);
            }
        }

        if (hasCompFilters) {
            for (Iterator it = filter.getComponentFilters().iterator(); it
                    .hasNext();) {
                ComponentFilter compFilter = (ComponentFilter) it.next();
                generateCompFilterSQL(sqlBuf, compFilter, myprefix,
                        attributeWhere, attributeJoins, timeRangeWhere,
                        timeRangeJoins);
            }
        }

    }

    private void generatePropFilterSQL(StringBuffer sqlBuf,
            PropertyFilter propFilter, String prefix, String attributeWhere,
            int attributeJoins, String timeRangeWhere, int timeRangeJoins) {
        String myprefix = null;
        myprefix = prefix + "_" + propFilter.getName().toLowerCase();

        boolean hasParamFilters = propFilter.getParamFilters().size() > 0;
        boolean hasIsNotDefinedFilter = propFilter.getIsNotDefinedFilter() != null;
        boolean hasTextMatchFilter = propFilter.getTextMatchFilter() != null;
        boolean hasTimeRangeFilter = propFilter.getTimeRangeFilter() != null;

        // end of road if present
        if (hasIsNotDefinedFilter) {
            attributeJoins++;
            attributeWhere += generateIsNotDefinedSQL(myprefix, attributeJoins);

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
                            .getTimeRangeFilter(), timeRangeJoins);
        }

        // another end of road
        if (!hasIsNotDefinedFilter && !hasTextMatchFilter && !hasParamFilters) {
            if (!hasTimeRangeFilter) {
                attributeJoins++;
                attributeWhere += generateIsDefinedSQL(myprefix, attributeJoins);
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
                    textMatch);
        }

        if (hasParamFilters) {
            for (Iterator it = propFilter.getParamFilters().iterator(); it
                    .hasNext();) {
                ParamFilter paramFilter = (ParamFilter) it.next();
                generateParamFilterSQL(sqlBuf, paramFilter, myprefix,
                        attributeWhere, attributeJoins, timeRangeWhere,
                        timeRangeJoins);
            }
        }

        // else we return
        if (sqlBuf.length() > 0)
            sqlBuf.append(" union ");

        sqlBuf.append(generateSQL(timeRangeWhere, timeRangeJoins,
                attributeWhere, attributeJoins));
    }

    private void generateParamFilterSQL(StringBuffer sqlBuf,
            ParamFilter paramFilter, String prefix, String attributeWhere,
            int attributeJoins, String timeRangeWhere, int timeRangeJoins) {
        String myprefix = null;
        myprefix = prefix + "_" + paramFilter.getName().toLowerCase();

        boolean hasIsNotDefinedFilter = paramFilter.getIsNotDefinedFilter() != null;
        boolean hasTextMatchFilter = paramFilter.getTextMatchFilter() != null;

        if (hasIsNotDefinedFilter) {
            attributeJoins++;
            attributeWhere += generateIsNotDefinedSQL(myprefix, attributeJoins);

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
                    textMatch);

            if (sqlBuf.length() > 0)
                sqlBuf.append(" union ");

            sqlBuf.append(generateSQL(timeRangeWhere, timeRangeJoins,
                    attributeWhere, attributeJoins));
            return;
        }

        if (!hasIsNotDefinedFilter && !hasTextMatchFilter) {
            attributeJoins++;
            attributeWhere += generateIsDefinedSQL(myprefix, attributeJoins);

            if (sqlBuf.length() > 0)
                sqlBuf.append(" union ");

            sqlBuf.append(generateSQL(timeRangeWhere, timeRangeJoins,
                    attributeWhere, attributeJoins));
            return;
        }
    }

    private String getTimeRangeWhere(String prefix,
            TimeRangeFilter timeRangeFilter, int index) {
        String myprefix = prefix + "--timerange";
        StringBuffer buf = new StringBuffer();
        buf.append("(ctri" + index + ".componenttype = :param" + paramCount);
        params.put("param" + paramCount, myprefix);
        paramCount++;
        buf.append(" and ((ctri" + index + ".startdate <= case when ctri" + index
                + ".isfloating=1 then '" + timeRangeFilter.getFloatEnd() + "'");
        // params.put("param" + paramCount, timeRangeFilter.getFloatEnd());
        // paramCount++;
        buf.append(" else :param" + paramCount + " end");
        params.put("param" + paramCount, timeRangeFilter.getUTCEnd());
        paramCount++;
        buf.append(" and ctri" + index + ".enddate >= case when ctri" + index
                + ".isfloating=1 then '" + timeRangeFilter.getFloatStart()
                + "'");
        // params.put("param" + paramCount, timeRangeFilter.getFloatStart());
        // paramCount++;
        buf.append(" else :param" + paramCount + " end");
        params.put("param" + paramCount, timeRangeFilter.getUTCStart());
        paramCount++;
        buf.append(") or (");
        buf.append("ctri" + index + ".startdate >= case when ctri" + index
                + ".isfloating=1 then '" + timeRangeFilter.getFloatStart()
                + "'");
        // params.put("param" + paramCount, timeRangeFilter.getFloatStart());
        // paramCount++;
        buf.append(" else :param" + paramCount + " end");
        params.put("param" + paramCount, timeRangeFilter.getUTCStart());
        paramCount++;
        buf.append(" and ctri" + index + ".startdate < case when ctri" + index
                + ".isfloating=1 then '" + timeRangeFilter.getFloatEnd() + "'");
        // params.put("param" + paramCount, timeRangeFilter.getFloatEnd());
        // paramCount++;
        buf.append(" else :param" + paramCount + " end)))");
        params.put("param" + paramCount, timeRangeFilter.getUTCEnd());
        paramCount++;

        return buf.toString();
    }

    private String generateSQL(String timeRangeWhere, int timeRangeJoins,
            String attributeWhere, int attributeJoins) {
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct i.* from item i");

        for (int i = 1; i <= attributeJoins; i++)
            buf.append(", cal_property_index cpi" + i);

        for (int i = 1; i <= timeRangeJoins; i++)
            buf.append(", cal_timerange_index ctri" + i);

        buf.append(" where i.parentid=:parentid");

        for (int i = 1; i <= attributeJoins; i++)
            buf.append(" and cpi" + i + ".itemid=i.id");
            
        for (int i = 1; i <= timeRangeJoins; i++)
            buf.append(" and ctri" + i + ".itemid=i.id");

        buf.append(attributeWhere);
        buf.append(timeRangeWhere);

        return buf.toString();
    }

    private String generateIsNotDefinedSQL(String prefix, int joinIndex) {
        String sql = " and cpi" + joinIndex + ".propertyname <> :param"
                + paramCount;
        params.put("param" + paramCount, prefix);
        paramCount++;
        return sql;
    }

    private String generateTextMatchSQL(String prefix, int joinIndex,
            TextMatchFilter textMatch) {
        String sql = " and cpi" + joinIndex + ".propertyname=:param"
                + paramCount;
        params.put("param" + paramCount, prefix);
        paramCount++;

        if (textMatch.isCaseless())
            sql += " and lower(cpi" + joinIndex + ".propertyvalue) ";
        else
            sql += " and cpi" + joinIndex + ".propertyvalue ";

        if (textMatch.isNegateCondition())
            sql += "not ";

        sql += "like :param" + paramCount;

        if (textMatch.isCaseless())
            params.put("param" + paramCount, "%"
                    + textMatch.getValue().toLowerCase() + "%");
        else
            params.put("param" + paramCount, "%" + textMatch.getValue() + "%");

        paramCount++;

        return sql;
    }

    private String generateIsDefinedSQL(String prefix, int joinIndex) {
        String sql = " and cpi" + joinIndex + ".propertyname=:param"
                + paramCount;
        params.put("param" + paramCount, prefix);
        return sql;
    }

}
