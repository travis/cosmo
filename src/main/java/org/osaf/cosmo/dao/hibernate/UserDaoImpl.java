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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.ArrayPagedList;
import org.osaf.cosmo.util.PageCriteria;
import org.osaf.cosmo.util.PagedList;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Implemtation of UserDao using Hibernate persistence objects.
 */
public class UserDaoImpl extends HibernateDaoSupport implements UserDao {

    private static final Log log = LogFactory.getLog(UserDaoImpl.class);

    private static final QueryCriteriaBuilder queryCriteriaBuilder =
        new UserQueryCriteriaBuilder();

    public void createUser(User user) {

        try {
            if (getUser(user.getUsername()) != null)
                throw new DuplicateUsernameException(user.getUsername());

            if (getUserByEmail(user.getEmail()) != null)
                throw new DuplicateEmailException(user.getEmail());

            
            user.setDateCreated(new Date());
            user.setDateModified(new Date());
            
            getSession().save(user);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } 

    }

    public User getUser(String username) {

        try {
           return findUserByUsername(username);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User getUserByEmail(String email) {
        try {
            return findUserByEmail(email);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public Set<User> getUsers() {
        try {
            HashSet<User> users = new HashSet<User>();
            Iterator it = getSession().createQuery("from User").iterate();
            while(it.hasNext())
                users.add((User) it.next());
            
            return users;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } 
    }

    public PagedList getUsers(PageCriteria pageCriteria) {
        try {
            Criteria crit = queryCriteriaBuilder.
                buildQueryCriteria(getSession(), pageCriteria);
            List results = crit.list();
            
            // Need the total
            Integer size = (Integer) getSession().
                createQuery("select count(*) from User").uniqueResult();

            return new ArrayPagedList(pageCriteria, results, size);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } 
    }

    public void removeUser(String username) {
        try {
            User user = findUserByUsername(username);
            // delete user
            if(user!=null)
                getSession().delete(user);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } 
    }

    public void removeUser(User user) {
        try {
            getSession().delete(user);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } 
    }

    public void updateUser(User user) {
        try {
            getSession().save(user);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void destroy() {
        // TODO Auto-generated method stub

    }

    public void init() {
        // TODO Auto-generated method stub

    }

    private User findUserByUsername(String username) {
        Session session = getSession();
        List users = session.getNamedQuery("user.byUsername").setParameter(
                "username", username).list();
        if (users.size() > 0)
            return (User) users.get(0);
        else
            return null;
    }

    private User findUserByEmail(String email) {
        Session session = getSession();
        List users = session.getNamedQuery("user.byEmail").setParameter(
                "email", email).list();
        if (users.size() > 0)
            return (User) users.get(0);
        else
            return null;
    }

    private static class UserQueryCriteriaBuilder
        extends StandardQueryCriteriaBuilder {

        public UserQueryCriteriaBuilder() {
            super(User.class);
        }

        protected List<Order> buildOrders(PageCriteria pageCriteria) {
            List<Order> orders = new ArrayList<Order>();

            String sort = pageCriteria.getSortTypeString();
            if (sort == null)
                sort = User.USERNAME_SORT_STRING;

            if (sort.equals(User.NAME_SORT_STRING)) {
                orders.add(createOrder(pageCriteria, "lastName"));
                orders.add(createOrder(pageCriteria, "firstName"));
            }
            else if (sort.equals(User.ADMIN_SORT_STRING))
                orders.add(createOrder(pageCriteria, "admin"));
            else if (sort.equals(User.EMAIL_SORT_STRING))
                orders.add(createOrder(pageCriteria, "email"));
            else if (sort.equals(User.CREATED_SORT_STRING))
                orders.add(createOrder(pageCriteria, "dateCreated"));
            else if (sort.equals(User.LAST_MODIFIED_SORT_STRING))
                orders.add(createOrder(pageCriteria, "dateModified"));
            else
                orders.add(createOrder(pageCriteria, "username"));

            return orders;
        }

        private Order createOrder(PageCriteria pageCriteria,
                                  String property) {
            return pageCriteria.isSortAscending() ?
                Order.asc(property) :
                Order.desc(property);
        }
    }
}
