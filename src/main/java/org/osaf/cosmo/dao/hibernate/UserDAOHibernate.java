/*
 * Copyright 2005 Open Source Applications Foundation
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

import org.osaf.cosmo.dao.UserDAO;
import org.osaf.cosmo.model.User;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * Hibernate implementation of UserDAO.
 *
 * @author Brian Moseley
 */
public class UserDAOHibernate extends HibernateDaoSupport
    implements UserDAO {
    private final Log log = LogFactory.getLog(UserDAOHibernate.class);

    private static final String HQL_GET_USERS =
        "from User order by username";
    private static final String HQL_GET_USER_BY_USERNAME =
        "from User where username=?";
    private static final String HQL_GET_USER_BY_EMAIL =
        "from User where email=?";
    private static final String HQL_DELETE_USER =
        "from User where id=?";

    /**
     */
    public List getUsers() {
        return getHibernateTemplate().find(HQL_GET_USERS);
    }

    /**
     */
    public User getUser(final Long id) {
        return (User) getHibernateTemplate().execute(new HibernateCallback() {
                public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                    User user = (User) session.get(User.class, id);
                    if (user == null) {
                        throw new ObjectRetrievalFailureException(User.class,
                                                                  id);
                    }
                    Hibernate.initialize(user.getRoles());
                    return user;
                }
            });
    }

    private User queryForUser(final String query,
                              final String param) {
        return (User) getHibernateTemplate().execute(new HibernateCallback() {
                public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                    List users = session.find(query, param, Hibernate.STRING);
                    if (users.isEmpty()) {
                        throw new ObjectRetrievalFailureException(User.class,
                                                                  param);
                    }
                    User user = (User) users.get(0);
                    Hibernate.initialize(user.getRoles());
                    return user;
                }
            });
    }

    /**
     */
    public User getUserByUsername(String username) {
        return queryForUser(HQL_GET_USER_BY_USERNAME, username);
    }

    /**
     */
    public User getUserByEmail(String email) {
        return queryForUser(HQL_GET_USER_BY_EMAIL, email);
    }

    /**
     */
    public void saveUser(User user) {
        user.setDateModified(new Date());
        user.setDateCreated(user.getDateModified());
        getHibernateTemplate().save(user);
    }

    /**
     */
    public void updateUser(User user) {
        user.setDateModified(new Date());
        getHibernateTemplate().update(user);
    }

    /**
     */
    public void removeUser(Long id) {
        getHibernateTemplate().delete(HQL_DELETE_USER, id, Hibernate.LONG);
    }

    /**
     */
    public void removeUser(User user) {
        removeUser(user.getId());
    }
}
