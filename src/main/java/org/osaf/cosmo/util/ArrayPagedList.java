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
    
    
    private static final int FIRST_ELEMENT = 0;
    private static final int FIRST_PAGE = 1;
	
    /**
     */
	public ArrayPagedList(){
		this.list = new ArrayList();
	}
	
    /**
     * Creates an ArrayPaged list that adheres to the supplied PageCriteria. If
     * an invalid page number is supplied, the list will be filled with a
     * pageNumber of 1 and will keep the supplied pageSize
     * 
     * @param pageCriteria -
     *            Pagination Criteria to paginate the list
     * @param list -
     *            the total unpaginated list to be paginated
     * 
     */
	public ArrayPagedList(PageCriteria pageCriteria, List list) {
        this.total = list.size();
        this.pageCriteria = pageCriteria;

        int pageSize = pageCriteria.getPageSize();

        if (pageSize == PageCriteria.VIEW_ALL) {
            this.list = list;
        } else {

            int first = (pageCriteria.getPageNumber() - 1) * pageSize;
            int last = first + pageSize;

            if (first > total - 1) {
                first = FIRST_ELEMENT;
                last = pageSize;
                this.pageCriteria.setPageNumber(FIRST_PAGE);
            }
            if (last > total) {
                last = total;
            }
            this.list = list.subList(first, last);
        }
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
