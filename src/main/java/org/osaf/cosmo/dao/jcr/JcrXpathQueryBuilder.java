package org.osaf.cosmo.dao.jcr;

import java.util.Iterator;
import org.osaf.cosmo.util.PageCriteria;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.repository.SchemaConstants;

/**
 * This class is a helper class that generates an xpath order by query, using
 * the information stored in the <code>PageCriteria</code>'s
 * <code>SortType</code>
 * 
 * @author EdBindl
 * 
 */
public class JcrXpathQueryBuilder implements JcrQueryBuilder {
    
    private static final String ASCENDING = " ascending";
    private static final String DESCENDING = " descending";

    private static final String ORDER_BY = "order by ";
    private static final String ATTRIBUTE_EXPRESSION = "@";
    
    /**
     */
    public JcrXpathQueryBuilder() {
    }
    
    /**
     * Builds a query for all users
     */
    public StringBuffer buildUserQuery(){
        StringBuffer stmt = new StringBuffer();
        
        stmt.append("/jcr:root").append("/*/*/*[@").append(SchemaConstants.NT_USER).append("]");
        
        return stmt;
    }
    
    /**
     * Builds a query for Users having the supplied email address
     */
    public StringBuffer buildUserQueryByEmail(String email){
        StringBuffer stmt = new StringBuffer();
        stmt.append("/jcr:root").
            append("//element(*, ").
            append(SchemaConstants.NT_USER).
            append(")").
            append("[@").
            append(SchemaConstants.NP_USER_EMAIL).
            append(" = '").
            append(email).
            append("']");
        return stmt;
    }
    
    /**
     * Builds a query for users pased on the supplied PageCriteria
     */
    public StringBuffer buildUserQuery(PageCriteria pageCriteria){
        StringBuffer stmt = new StringBuffer();
        stmt.append("/jcr:root").append("/*/*/*[@").append(SchemaConstants.NT_USER).append("]");
        String sortType = pageCriteria.getSortTypeString();
        JcrSortType jcrSortType = buildJcrSortType(sortType, pageCriteria.isSortAscending());
        if (sortType != null) {
            stmt.append(buildOrderByQuery(jcrSortType));
        }
        return stmt;
    }
    
    /**
     * Builds and returns the "order by" query using the information stored in
     * the <code>PageCriteria</code>'s <code>SortType</code>
     * 
     * @return a <code>String</code> containing the xpath "order by" query
     */
    public String buildOrderByQuery( JcrSortType jcrSortType) {
        String query = new String(ORDER_BY);
        boolean sortOrder = jcrSortType.isAscending();
        Iterator<JcrSortType.SortAttribute> it = jcrSortType.getAttributes().iterator();
        while (it.hasNext()) {
            JcrSortType.SortAttribute sortAttribute = it.next();
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
    
    /**
     * Maps the sorttype from a string to a JcrSortType
     * @param sortTypeString - String that indiciates sort type
     * @param ascending whether to sort ascending or descending
     * @return JcrSortType
     */
    private JcrSortType buildJcrSortType(String sortTypeString, boolean ascending){
        
        JcrSortType sortType = new JcrSortType();
        
        sortType.setAscending(ascending);
        
        if(sortTypeString == null || "".equals(sortTypeString)) {
            sortTypeString = User.DEFAULT_SORT_STRING;
        }
        
        if(sortTypeString.equals(User.USERNAME_SORT_STRING)){
            sortType.addAttribute(SchemaConstants.NP_USER_USERNAME, true);
        }
        else if(sortTypeString.equals(User.ADMIN_SORT_STRING)){
            sortType.addAttribute(SchemaConstants.NP_USER_ADMIN, false);
            sortType.addAttribute(SchemaConstants.NP_USER_LASTNAME, true);
            sortType.addAttribute(SchemaConstants.NP_USER_FIRSTNAME, true);
        }
        else if(sortTypeString.equals(User.EMAIL_SORT_STRING)){
            sortType.addAttribute(SchemaConstants.NP_USER_EMAIL, true);
        }
        else if(sortTypeString.equals(User.CREATED_SORT_STRING)){
            sortType.addAttribute(SchemaConstants.NP_USER_DATECREATED, true);
            sortType.addAttribute(SchemaConstants.NP_USER_LASTNAME, true);
            sortType.addAttribute(SchemaConstants.NP_USER_FIRSTNAME, true);
        }
        else if(sortTypeString.equals(User.LAST_MODIFIED_SORT_STRING)){
            sortType.addAttribute(SchemaConstants.NP_USER_DATEMODIFIED, true);
            sortType.addAttribute(SchemaConstants.NP_USER_LASTNAME, true);
            sortType.addAttribute(SchemaConstants.NP_USER_FIRSTNAME, true);
        }
        else if(sortTypeString.equals(User.NAME_SORT_STRING)) {
            sortType.addAttribute(SchemaConstants.NP_USER_LASTNAME, true);
            sortType.addAttribute(SchemaConstants.NP_USER_FIRSTNAME, true);
            sortType.addAttribute(SchemaConstants.NP_USER_USERNAME, true);
        }
        else {
            throw new IllegalArgumentException();
        }
        return sortType;

    }

}
