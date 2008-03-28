/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.cmp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.PageCriteria;
import org.osaf.cosmo.util.PagedList;
import org.osaf.cosmo.util.URLQuery;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An interface for Cosmo API resources
 */
public class UsersResource implements CmpResource, OutputsXml {
    private static final Log log = LogFactory.getLog(UsersResource.class);

    /**
     */
    public static final String EL_USERS = "users";

    private static final String ATT_FIRST_PAGE_LINK = "first";
    private static final String ATT_PREVIOUS_PAGE_LINK = "previous";
    private static final String ATT_NEXT_PAGE_LINK = "next";
    private static final String ATT_LAST_PAGE_LINK = "last";
    
    private Collection<User> users;
    private String urlBase;
    private Map<String, String[]> parameterMap;
    
    /**
     * Constructs a resource that represents the given {@link User}s.
     */
    public UsersResource(Collection<User> users, String urlBase, 
            Map<String, String[]> parameterMap) {
        
        this.users = users;
        this.urlBase = urlBase;
        this.parameterMap = parameterMap;
    }

    // CmpResource methods

    /**
     * Returns the <code>Collection<User></code> that backs
     * this resource.
     */
    public Object getEntity() {
        return users;
    }

    // OutputsXml methods

    /**
     * Returns an XML representation of the resource in the form of a
     * {@link org.w3c.dom.Element}.
     *
     * The XML is structured like so:
     *
     * <pre>
     * <users>
     *   <user>
     *     ...
     *   </user>
     * </users>
     * </pre>
     *
     * where the structure of the <code>user</code> element is defined
     * by {@link UserResource}.
     */
    public Element toXml(Document doc) {
        Element e = DomUtil.createElement(doc, EL_USERS, NS_CMP);
        
        if (users instanceof PagedList){
            URLQuery query = new URLQuery(parameterMap);
            Map<String, String[]> exceptionMap = new HashMap<String, String[]>();
            
            
            PagedList<User, User.SortType> pagedUserList = (PagedList<User, User.SortType>) users;
            PageCriteria<User.SortType> pageCriteria = pagedUserList.getPageCriteria();
            
            Element firstPageLink = DomUtil.createElement(doc, "link", NS_CMP);
            
            firstPageLink.setAttribute("rel", ATT_FIRST_PAGE_LINK);
            
            exceptionMap.put(PageCriteria.PAGE_NUMBER_URL_KEY, 
                    new String[]{Integer.toString(PageCriteria.FIRST_PAGE)});
            
            firstPageLink.setAttribute("href", 
                    this.urlBase + CmpConstants.USER_LIST_PATH + 
                    query.toString(exceptionMap)
                    );
            e.appendChild(firstPageLink);
            
            if (pageCriteria.getPageNumber() > 1){
                
                Element previousPageLink = DomUtil.createElement(doc, "link", NS_CMP);
                previousPageLink.setAttribute("rel", ATT_PREVIOUS_PAGE_LINK);

                exceptionMap.clear();
                exceptionMap.put(PageCriteria.PAGE_NUMBER_URL_KEY, 
                        new String[]{Integer.toString(
                                Math.min(pageCriteria.getPageNumber() - 1, pagedUserList.getLastPageNumber())
                        )});

                previousPageLink.setAttribute("href", 
                    this.urlBase + CmpConstants.USER_LIST_PATH + 
                    query.toString(exceptionMap)
                    );
                e.appendChild(previousPageLink);
            }

            if (pageCriteria.getPageNumber() < pagedUserList.getLastPageNumber()){
                Element nextPageLink = DomUtil.createElement(doc, "link", NS_CMP);
                nextPageLink.setAttribute("rel", ATT_NEXT_PAGE_LINK);

                exceptionMap.clear();
                exceptionMap.put(PageCriteria.PAGE_NUMBER_URL_KEY, 
                        new String[]{Integer.toString(
                                Math.max(pageCriteria.getPageNumber() + 1, 1)
                        )});

                nextPageLink.setAttribute("href", 
                        this.urlBase + CmpConstants.USER_LIST_PATH + 
                        query.toString(exceptionMap)
                );
                e.appendChild(nextPageLink);
            }
          
            Element lastPageLink = DomUtil.createElement(doc, "link", NS_CMP);
            lastPageLink.setAttribute("rel", ATT_LAST_PAGE_LINK);

            exceptionMap.clear();
            exceptionMap.put(PageCriteria.PAGE_NUMBER_URL_KEY, 
                    new String[]{Integer.toString(
                            pagedUserList.getLastPageNumber()
                    )});

            lastPageLink.setAttribute("href", 
                    this.urlBase + CmpConstants.USER_LIST_PATH + 
                        query.toString(exceptionMap)
                    );
            e.appendChild(lastPageLink);
          
        }
        
        for (User user : this.users) {
            UserResource ur = new UserResource(user, urlBase);
            e.appendChild(ur.toXml(doc));
        }
        return e;
    }

    // our methods

    /**
     */
    public Collection<User> getUsers() {
        return users;
    }
}
