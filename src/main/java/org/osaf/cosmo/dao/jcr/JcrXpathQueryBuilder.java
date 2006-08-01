package org.osaf.cosmo.dao.jcr;

import java.util.Iterator;

import org.osaf.cosmo.util.PageCriteria;
import org.osaf.cosmo.util.PageCriteria.SortAttribute;

/**
 * This class is a helper class that generates an xpath order by query, using
 * the information stored in the <code>PageCriteria</code>'s
 * <code>SortType</code>
 * 
 * @author EdBindl
 * 
 */
public class JcrXpathQueryBuilder {
    
    private static final String ASCENDING = " ascending";
    private static final String DESCENDING = " descending";

    private static final String ORDER_BY = "order by ";
    private static final String ATTRIBUTE_EXPRESSION = "@";
    
    /**
     */
    public JcrXpathQueryBuilder() {
    }
    
    /**
     * Builds and returns the "order by" query using the information stored in
     * the <code>PageCriteria</code>'s <code>SortType</code>
     * 
     * @return a <code>String</code> containing the xpath "order by" query
     */
    public static String buildOrderByQuery( PageCriteria pageCriteria) {
        String query = new String(ORDER_BY);
        boolean sortOrder = pageCriteria.isSortAscending();
        Iterator<SortAttribute> it = pageCriteria.getSortType().getAttributes().iterator();
        while (it.hasNext()) {
            SortAttribute sortAttribute = it.next();
            if (sortOrder) {
                query += ATTRIBUTE_EXPRESSION + sortAttribute.getAttributeName()
                        + ((sortAttribute.getOrder() ? ASCENDING : DESCENDING));
            } else {
                query += ATTRIBUTE_EXPRESSION + sortAttribute.getAttributeName()
                        + ((!sortAttribute.getOrder() ? ASCENDING : DESCENDING));
            }
            if (it.hasNext()) {
                query += ", ";
            }
        }
        return query;
    }
}
