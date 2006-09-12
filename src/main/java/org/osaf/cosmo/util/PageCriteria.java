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

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the criteria for pagination. Holds the Page Number, Page
 * Size, Sort Type and Sort Order
 * 
 * @author EdBindl
 * 
 */
public class PageCriteria {

    /**
     * The constant to view all.
     */
    public static final int VIEW_ALL = -1;
    
    /**
     * The Default Page Number
     */
    public static final int DEFAULT_PAGENUMBER = 1;
    /**
     * The Default Pagesize
     */
    public static final int DEFAULT_PAGESIZE = 25;
    /**
     * The Default sort ascending
     */
    public static final Boolean DEFAULT_SORTASCENDING = true;

    
	/**
	 * Number of the Page (1 Based)
	 */
	private int pageNumber;

    /**
     * Size of each page. <code>PageCriteria.VIEW_ALL</code> holds the constant for
     * viewing all
     */
    private int pageSize;

    /**
     * Indicates the sort order to sort the Sort Type, <code>true</code> for
     * ascending, <code>false</code> for descending
     */
    private Boolean sortAscending;

    /**
     * Describes the way the data is sorted. If a SortOrder is
     * <code>false</code> (descending) the precendence of the
     * <code>SortType</code>'s <code>SortAttribute</code> will be preserved
     * but will each be sorted in the opposite order. Because sorting is very
     * dependant on the users choice of repository it is recommended that this
     * class be extended to handle the sortType
     */
    private String sortTypeString;

    /**
     */
    public PageCriteria(){
        initialize();
    }
    
    public PageCriteria(PageCriteria pageCriteria) {
        this.pageNumber = pageCriteria.getPageNumber();
        this.pageSize = pageCriteria.getPageSize();
        this.sortAscending = pageCriteria.isSortAscending();
        this.sortTypeString = pageCriteria.getSortTypeString();
    }


    /**
     */
    public PageCriteria(Integer pageNumber, Integer pageSize, boolean sortAscending, String sortTypeString) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sortAscending = sortAscending;
        this.sortTypeString = sortTypeString;
    }

    /**
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     */
    public String getSortTypeString() {
        return sortTypeString;
    }

    /**
     */
    public void setSortTypeString(String sortTypeString) {
        this.sortTypeString = sortTypeString;
    }

    /**
     * 
     * @return <code>true</code> for Ascending and <code>false</code> for Descending
     */
    public boolean isSortAscending() {
        return sortAscending;
    }

    /**
     * @param sortAscending Set to <code>true</code> for Ascending and <code>false</code> for Descending
     */
    public void setSortAscending(boolean sortOrder) {
        this.sortAscending = sortOrder;
    }
    
    public void initialize() {
        pageNumber = DEFAULT_PAGENUMBER;
        pageSize = DEFAULT_PAGESIZE;
        sortAscending = DEFAULT_SORTASCENDING;
    }
}
