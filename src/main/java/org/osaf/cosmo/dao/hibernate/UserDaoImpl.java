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

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
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

    private IdentifierGenerator idGenerator;

    private static final QueryCriteriaBuilder<User.SortType> queryCriteriaBuilder = new UserQueryCriteriaBuilder<User.SortType>();

    public User createUser(User user) {

        try {
            if(user==null)
                throw new IllegalArgumentException("user is required");
            
            if(user.getId()!=-1)
                throw new IllegalArgumentException("new user is required");
            
            if (findUserByUsernameIgnoreCase(user.getUsername()) != null)
                throw new DuplicateUsernameException(user.getUsername());

            if (getUserByEmail(user.getEmail()) != null)
                throw new DuplicateEmailException(user.getEmail());

            user.setDateCreated(new Date());
            user.setDateModified(new Date());
            // always store email as lowercase
            user.setEmail(user.getEmail().toLowerCase());

            if (user.getUid() == null || "".equals(user.getUid()))
                user.setUid(getIdGenerator().nextIdentifier().toString());

            getSession().save(user);
            return user;
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
    
    public User getUserByUid(String uid) {
        if(uid==null)
            throw new IllegalArgumentException("uid required");
        
        try {
            return findUserByUid(uid);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User getUserByActivationId(String id) {
        if(id==null)
            throw new IllegalArgumentException("id required");
        
        try {
            return findUserByActivationId(id);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User getUserByEmail(String email) {
        if(email==null)
            throw new IllegalArgumentException("email required");
            
        try {
            return findUserByEmail(email.toLowerCase());
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public Set<User> getUsers() {
        try {
            HashSet<User> users = new HashSet<User>();
            Iterator it = getSession().createQuery("from User").iterate();
            while (it.hasNext())
                users.add((User) it.next());

            return users;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public PagedList getUsers(PageCriteria<User.SortType> pageCriteria) {
        try {
            Criteria crit = queryCriteriaBuilder.buildQueryCriteria(
                    getSession(), pageCriteria);
            List<User> results = crit.list();

            // Need the total
            Long size = (Long) getSession().createQuery(
                    "select count(*) from User").uniqueResult();

            return new ArrayPagedList<User, User.SortType>(pageCriteria, results, size.intValue());
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void removeUser(String username) {
        try {
            User user = findUserByUsername(username);
            // delete user
            if (user != null)
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

    public User updateUser(User user) {
        try {
            
            // Evict user from session cache to prevent auto flush occuring
            // when searching for duplicates
            if(getSession().contains(user))
                getSession().evict(user);
            
            User findUser = findUserByUsernameIgnoreCase(user.getUsername());
            if (findUser!=null && !findUser.getId().equals(user.getId()))
                throw new DuplicateUsernameException(user.getUsername());
            
            findUser = getUserByEmail(user.getEmail());
            if (findUser!=null && !findUser.getId().equals(user.getId()))
                throw new DuplicateEmailException(user.getEmail());

            user.setDateModified(new Date());
            user.setEmail(user.getEmail().toLowerCase());
            
            // merge transient instance with persistent instance
            getSession().merge(user);
           
            // return persistent instance
            return (User) getSession().load(User.class, user.getId());
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void destroy() {
        // TODO Auto-generated method stub

    }

    public void init() {
        if (idGenerator == null) {
            throw new IllegalStateException("idGenerator is required");
        }
    }

    public IdentifierGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdentifierGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    private User findUserByUsername(String username) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byUsername").setParameter(
                "username", username);
        hibQuery.setCacheable(true);
        return (User) hibQuery.uniqueResult();
    }
    
    private User findUserByUsernameIgnoreCase(String username) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byUsername.ignorecase").setParameter(
                "username", username);
        hibQuery.setCacheable(true);
        List users = hibQuery.list();
        if (users.size() > 0)
            return (User) users.get(0);
        else
            return null;
    }

    private User findUserByEmail(String email) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byEmail").setParameter(
                "email", email);
        hibQuery.setCacheable(true);
        List users = hibQuery.list();
        if (users.size() > 0)
            return (User) users.get(0);
        else
            return null;
    }

    private User findUserByUid(String uid) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byUid").setParameter(
                "uid", uid);
        hibQuery.setCacheable(true);
        return (User) hibQuery.uniqueResult();
    }

    private User findUserByActivationId(String id) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byActivationId").setParameter(
                "activationId", id);
        hibQuery.setCacheable(true);
        return (User) hibQuery.uniqueResult();
    }

    private static class UserQueryCriteriaBuilder<SortType extends User.SortType> extends
            StandardQueryCriteriaBuilder<SortType> {

        public UserQueryCriteriaBuilder() {
            super(User.class);
        }

        protected List<Order> buildOrders(PageCriteria<SortType> pageCriteria) {
            List<Order> orders = new ArrayList<Order>();

            User.SortType sort = pageCriteria.getSortType();
            if (sort == null)
                sort = User.SortType.USERNAME;

            if (sort.equals(User.SortType.NAME)) {
                orders.add(createOrder(pageCriteria, "lastName"));
                orders.add(createOrder(pageCriteria, "firstName"));
            }
            else if (sort.equals(User.SortType.ADMIN))
                orders.add(createOrder(pageCriteria, "admin"));
            else if (sort.equals(User.SortType.EMAIL))
                orders.add(createOrder(pageCriteria, "email"));
            else if (sort.equals(User.SortType.CREATED))
                orders.add(createOrder(pageCriteria, "dateCreated"));
            else if (sort.equals(User.SortType.LAST_MODIFIED))
                orders.add(createOrder(pageCriteria, "dateModified"));
            else
                orders.add(createOrder(pageCriteria, "username"));

            return orders;
        }

        private Order createOrder(PageCriteria pageCriteria, String property) {
            return pageCriteria.isSortAscending() ?
                Order.asc(property) :
                   Order.desc(property);
        }
    }

}
