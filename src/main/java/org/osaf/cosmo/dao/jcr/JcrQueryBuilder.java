package org.osaf.cosmo.dao.jcr;

import org.osaf.cosmo.util.PageCriteria;

/** 
 * An interface to build querys to query the JCR.
 * 
 * @author EdBindl
 *
 */
public interface JcrQueryBuilder {

    /**
     * Builds a query to query all Users
     * @return the query
     */
    public StringBuffer buildUserQuery();
    
    /**
     * Builds a query to query all users with the supplied email
     * @param email
     * @return the query
     */
    public StringBuffer buildUserQueryByEmail(String email);
    
    /**
     * Users the supplied PageCriteria to build a query for Users.
     * @param pageCriteria
     * @return the query
     */
    public StringBuffer buildUserQuery(PageCriteria pageCriteria);
}
