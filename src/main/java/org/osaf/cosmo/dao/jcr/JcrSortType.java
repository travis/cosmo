package org.osaf.cosmo.dao.jcr;

import java.util.ArrayList;
import java.util.List;



/**
 * This class contains a list of SortAttributes to sort on and the order to
 * sort them in. The earlier in the <code>List</code> the
 * <code>SortAttribute</code> is the higher precedence it has. 
 * 
 * @author EdBindl
 * 
 */
public class JcrSortType {
    
    private boolean ascending;

    private List<SortAttribute> attributes;

    /**
     */
    public JcrSortType(){
        this.attributes = new ArrayList<SortAttribute>();
        this.ascending = true;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
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