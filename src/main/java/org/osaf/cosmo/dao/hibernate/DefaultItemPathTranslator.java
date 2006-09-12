/*
 * Copyright (c) 2006 SimDesk Technologies, Inc.  All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * SimDesk Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with SimDesk Technologies.
 *
 * SIMDESK TECHNOLOGIES MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT
 * THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.  SIMDESK TECHNOLOGIES
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package org.osaf.cosmo.dao.hibernate;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.osaf.cosmo.model.Item;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * Default implementation for ItempPathTranslator. This implementation expects
 * paths to be of the format: /username/parent1/parent2/itemname
 * 
 */
public class DefaultItemPathTranslator implements ItemPathTranslator {

    
    private HibernateTemplate template = null;
    
    public DefaultItemPathTranslator(HibernateTemplate template) {
        this.template = template;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.hibernate.ItemPathTranslator#findItemByPath(org.hibernate.Session,
     *      java.lang.String)
     */
    public Item findItemByPath(final String path) {
        
        return (Item) template.execute(new HibernateCallback() {
            public Object doInHibernate(Session session){
                return findItemByPath(session, path);
            }
        });
        
    }
    
    public Item findItemParent(String path) {
        if(path==null)
            return null;
        
        int lastIndex = path.lastIndexOf("/");
        if(lastIndex==-1)
            return null;
        
        if((lastIndex+1) >= path.length())
            return null;
        
        String parentPath =  path.substring(0,lastIndex);

        return findItemByPath(parentPath);
    }

    public String getItemName(String path) {
        if(path==null)
            return null;
        
        int lastIndex = path.lastIndexOf("/");
        if(lastIndex==-1)
            return null;
        
        if((lastIndex+1) >= path.length())
            return null;
        
        return path.substring(lastIndex+1);
    }

    public String getItemPath(Item dbItem) {
        StringBuffer path = new StringBuffer();
        LinkedList<String> hierarchy = new LinkedList<String>();
        hierarchy.addFirst(dbItem.getName());
        Item currentDbItem = dbItem;
        while (currentDbItem.getParent() != null) {
            currentDbItem = currentDbItem.getParent();
            hierarchy.addFirst(currentDbItem.getName());
        }

        // hierarchy
        for (String part : hierarchy)
            path.append("/" + part);

        return path.toString();
    }

    protected Item findItemByPath(Session session, String path) {
        
        if(path==null || "".equals(path))
            return null;
        
        if (path.charAt(0) == '/')
            path = path.substring(1, path.length());

        String[] segments = path.split("/");
        String username = segments[0];

        if (segments.length == 0)
            return null;

        String rootName = segments[0];
        Item rootItem = findRootItemByOwnerAndName(session, username,
                rootName);

        // If parent item doesn't exist don't go any further
        if (rootItem == null)
            return null;

        Item parentItem = rootItem;
        for (int i = 1; i < segments.length; i++) {
            Item nextItem = findItemByParentAndName(session, parentItem,
                    segments[i]);
            parentItem = nextItem;
            // if any parent item doesn't exist then bail now
            if (parentItem == null)
                return null;
        }

        return parentItem;
    }
    
    protected Item findRootItemByOwnerAndName(Session session,
            String username, String name) {
        Query hibQuery = session.getNamedQuery(
                "item.by.ownerName.name.nullParent").setParameter("username",
                username).setParameter("name", name);

        List results = hibQuery.list();
        if (results.size() > 0)
            return (Item) results.get(0);
        else
            return null;
    }

    protected Item findItemByParentAndName(Session session, Item parent,
            String name) {
        Query hibQuery = session.getNamedQuery("item.by.parent.name")
                .setParameter("parent", parent).setParameter("name", name);

        List results = hibQuery.list();
        if (results.size() > 0)
            return (Item) results.get(0);
        else
            return null;
    }

}
