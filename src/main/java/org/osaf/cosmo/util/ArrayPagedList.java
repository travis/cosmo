package org.osaf.cosmo.util;

import java.util.List;
import java.util.ArrayList;

/**
 * Class that paginated and stores a list, the Pagination Criteria for the list
 * and the size of the total un paginated list
 * 
 * @author EdBindl
 * 
 */
public class ArrayPagedList implements PagedList {
	/**
     * The size of the total unpaginated list.
	 */
	int total;
    
    /**
     * Holds the Pagination information for the <code>List</code>.
     */
	PageCriteria pageCriteria;
	
    /**
     * The <code>List</code> that meets the pagination criteria.
     */
	List list;
	
    /**
     */
	public ArrayPagedList(){
		this.list = new ArrayList();
	}
	
    /**
     * @param pageCriteria -
     *            Pagination Criteria to paginate the list
     * @param list -
     *            the total unpaginated list to be paginated
     * 
     * @throws IllegalArgumentException
     *             if an invalid Page Number is supplied
     */
	public ArrayPagedList(PageCriteria pageCriteria, List list) {
		this.total = list.size();
		this.pageCriteria = pageCriteria;

    	int pageSize = pageCriteria.getPageSize();
    	
        int first = (pageCriteria.getPageNumber() - 1) * pageSize;
        int last = first + pageSize;
        
        if(first > total - 1){
            throw new IllegalArgumentException("Page " + 
                                              pageCriteria.getPageNumber() + 
                                              " not found.");
        }
        if(last > total){
            last = total;
        }
        
        this.list = list.subList(first, last);
    }

    /**
     */
    public PageCriteria getPageCriteria() {
        return pageCriteria;
    }

    /**
     */
    public int getTotal() {
        return total;
    }

    /**
     */
    public void setPageCriteria(PageCriteria pageCriteria) {
        this.pageCriteria = pageCriteria;
    }

    /**
     */
    public void setTotal(int total) {
        this.total = total;
    }
    
    /**
     */
    public List getList(){
        return list;
    }
    
    /**
     */
    public void setList(List items){
        this.list = items;
    }
}
