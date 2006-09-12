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
     * Creates an <code>ArrayPagedList</code> based on the supplied
     * sublist. The sublist is assumed to have already been paged, and
     * the supplied page criteria is not used.
     * 
     * @param pageCriteria - criteria for pagination of the list
     * @param sublist - the elements of the original list that have
     *            been deemed by some external code to be part of the
     *            page indicated by the page criteria
     * @param total the total number of elements in the original list
     * 
     */
	public ArrayPagedList(PageCriteria pageCriteria,
                              List sublist,
                              int total) {
        this.pageCriteria = pageCriteria;
        this.list = sublist;
        this.total = total;
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
