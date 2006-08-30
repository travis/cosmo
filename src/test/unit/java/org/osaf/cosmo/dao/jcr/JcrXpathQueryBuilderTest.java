package org.osaf.cosmo.dao.jcr;

import org.osaf.cosmo.dao.jcr.JcrXpathQueryBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

public class JcrXpathQueryBuilderTest extends TestCase{

    private static final Log log = LogFactory.getLog(JcrXpathQueryBuilderTest.class);
    
    public void testQueryBuilder(){
        
        JcrXpathQueryBuilder jcrXpathQueryBuilder = new JcrXpathQueryBuilder();
        
        
        JcrSortType nameSort = new JcrSortType();
    
        nameSort.addAttribute("cosmo:firstName", true);
        nameSort.addAttribute("cosmo:lastName" , true);
        nameSort.setAscending(true);
        
        String query = jcrXpathQueryBuilder.buildOrderByQuery(nameSort);
        
        //Try the ascending query
        assertEquals("Ascending Query Test", query , "order by @cosmo:firstName ascending, @cosmo:lastName ascending");
        
        nameSort.setAscending(false);
        
        query = jcrXpathQueryBuilder.buildOrderByQuery(nameSort);
        
        //Try the descending query
        assertEquals("Descending Query Test", query , "order by @cosmo:firstName descending, @cosmo:lastName descending");
        
        nameSort.setAscending(true);
        
        query = jcrXpathQueryBuilder.buildOrderByQuery(nameSort);
        
        //Make sure ascending query still works
        assertEquals("Ascending Query Test after descending", query , "order by @cosmo:firstName ascending, @cosmo:lastName ascending");
    }
}
