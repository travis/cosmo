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
package org.osaf.cosmo.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.osaf.cosmo.model.CollectionItem;
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
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.hibernate.ItemPathTranslator#findItemByPath(java.lang.String, org.osaf.cosmo.model.CollectionItem)
     */
    public Item findItemByPath(final String path, final CollectionItem root) {
        return (Item) template.execute(new HibernateCallback() {
            public Object doInHibernate(Session session){
                return findItemByPath(session, path, root);
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
    
    protected Item findItemByPath(Session session, String path, CollectionItem root) {
        
        if(path==null || "".equals(path))
            return null;
        
        if (path.charAt(0) == '/')
            path = path.substring(1, path.length());

        String[] segments = path.split("/");
        
        if (segments.length == 0)
            return null;

        Item parentItem = root;
        for (int i = 0; i < segments.length; i++) {
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
