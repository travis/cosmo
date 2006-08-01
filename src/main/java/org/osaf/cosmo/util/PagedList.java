package org.osaf.cosmo.util;

import java.util.List;

/**
 * An interface for a class that holds a <code>PageCriteria</code>,
 * <code>List</code> that meets the <code>PageCriteria</code> and the size
 * of the total unpaginated<code>List</code>.
 * 
 * @author EdBindl
 * 
 */
public interface PagedList {
    
    /**
     * Returns the Pagination criteria for the list.
     */
    public PageCriteria getPageCriteria();

    /**
     * Sets the Pagination criteria for the list.
     * 
     * @param pageCriteria
     */
    public void setPageCriteria(PageCriteria pageCriteria);

    /**
     * Returns the size of the total unpaginated list.
     */
    public int getTotal();

    /**
     * Sets the size of the total unpaginated list.
     * @param total
     */
    public void setTotal(int total);
    
    /**
     * Returns the list meeting the Pagination Criteria
     */
    public List getList();
    
    /**
     * Sets the list meeting the Pagination Criteria
     * 
     * @param items
     */
    public void setList(List items);
}
