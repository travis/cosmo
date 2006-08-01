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
	 * Number of the Page (1 Based)
	 */
	private int pageNumber;

    /**
     * Size of each page
     */
    private int pageSize;

    /**
     * Indicates the sort order to sort the Sort Type, <code>true</code> for
     * ascending, <code>false</code> for descending
     */
    private boolean sortAscending;

    /**
     * Describes the way the data is sorted. If a SortOrder is
     * <code>false</code> (descending) the precendence of the
     * <code>SortType</code>'s <code>SortAttribute</code> will be preserved
     * but will each be sorted in the opposite order.
     */
    private SortType sortType;

    /**
     */
    public PageCriteria(){
    }

    /**
     */
    public PageCriteria(Integer pageNumber, Integer pageSize, boolean sortOrder, SortType sortType) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sortAscending = sortOrder;
        this.sortType = sortType;
    }

    /**
     */
    public Integer getPageNumber() {
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
    public SortType getSortType() {
        return sortType;
    }

    /**
     */
    public void setSortType(SortType sortType) {
        this.sortType = sortType;
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

    /**
     * This class contains a list of SortAttributes to sort on and the order to
     * sort them in. The earlier in the <code>List</code> the
     * <code>SortAttribute</code> is the higher precedence it has. 
     * 
     * @author EdBindl
     * 
     */
    public class SortType {

        private List<SortAttribute> attributes;

        /**
         */
        public SortType(){
            this.attributes = new ArrayList<SortAttribute>();
        }

        /**
         */
        public List<SortAttribute> getAttributes() {
            return attributes;
        }

        /**
         */
        public void setAttributes(List<SortAttribute> attributes) {
            this.attributes = attributes;
        }

        /**
         * This method adds a <code>SortAttribute</code>. Note that the
         * earlier a <code>SortAttribute</code> the higher precedence it has
         * in the order
         * 
         * @param sortAttribute the Attribute to add
         */
        public void addAttribute( SortAttribute sortAttribute){
            attributes.add(sortAttribute);
        }

        /**
         * This method adds a <code>SortAttribute</code>, by supplying the
         * attribute name and order to sort in. Note that the earlier a
         * <code>SortAttribute</code> the higher precedence it has in the
         * order
         * 
         * @param attributeName
         *            the name of the attribute
         * @param order
         *            the order to search in, <CODE>true</CODE> for ascending
         *            and <CODE>false</CODE> for descending
         */
        public void addAttribute( String attributeName, boolean order){
            SortAttribute sortAttribute = new SortAttribute(attributeName, order);
            addAttribute(sortAttribute);
        }

    }
    /**
     * This class contains the relevant information to sort on a single
     * attribute. It holds the name of the attribute and the order to sort it
     * in.
     * 
     * @author EdBindl
     * 
     */
    public class SortAttribute {

        private String attributeName;

        /**
         * Indicated the sort order, <CODE>true</CODE> for ascending and <CODE>false</CODE>
         * for descending
         */
        private boolean order;

        /**
         */
        public SortAttribute(){
        }
        /**
         */
        public SortAttribute(String attributeName, boolean order){
            this.attributeName = attributeName;
            this.order = order;
        }

        /**
         */
        public String getAttributeName() {
            return attributeName;
        }

        /**
         */
        public void setAttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        /**
         */
        public boolean getOrder() {
            return order;
        }

        /**
         */
        public void setOrder(boolean order) {
            this.order = order;
        }
    }
}
