package org.osaf.cosmo.dao.jcr;

import org.osaf.cosmo.util.PageCriteria;
import org.osaf.cosmo.util.PageCriteria.SortType;
import org.osaf.cosmo.dao.jcr.JcrXpathQueryBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

public class JcrXpathQueryBuilderTest extends TestCase{

    private static final Log log = LogFactory.getLog(JcrXpathQueryBuilderTest.class);
    
    public void testQueryBuilder(){
        
        PageCriteria pageCriteria = new PageCriteria();
        
        SortType nameSort = pageCriteria.new SortType();
    
        nameSort.addAttribute("cosmo:firstName", true);
        nameSort.addAttribute("cosmo:lastName" , true);
        
        pageCriteria.setSortType(nameSort);
        pageCriteria.setSortAscending(true);
        
        String query = JcrXpathQueryBuilder.buildOrderByQuery(pageCriteria);
        
        //Try the ascending query
        assertEquals("Ascending Query Test", query , "order by @cosmo:firstName ascending, @cosmo:lastName ascending");
        
        pageCriteria.setSortAscending(false);
        
        query = JcrXpathQueryBuilder.buildOrderByQuery(pageCriteria);
        
        //Try the descending query
        assertEquals("Descending Query Test", query , "order by @cosmo:firstName descending, @cosmo:lastName descending");
        
        pageCriteria.setSortAscending(true);
        
        query = JcrXpathQueryBuilder.buildOrderByQuery(pageCriteria);
        
        //Make sure ascending query still works
        assertEquals("Ascending Query Test after descending", query , "order by @cosmo:firstName ascending, @cosmo:lastName ascending");
    }
}
