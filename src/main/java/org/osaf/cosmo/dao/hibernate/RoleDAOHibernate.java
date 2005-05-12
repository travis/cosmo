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

import org.osaf.cosmo.dao.RoleDAO;
import org.osaf.cosmo.model.Role;

import java.util.Date;
import java.util.List;

import net.sf.hibernate.Hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * Hibernate implementation of RoleDAO.
 *
 * @author Brian Moseley
 */
public class RoleDAOHibernate extends HibernateDaoSupport
    implements RoleDAO {
    private final Log log = LogFactory.getLog(RoleDAOHibernate.class);

    private static final String HQL_GET_ROLES =
        "from Role order by name";
    private static final String HQL_GET_ROLE_BY_NAME =
        "from Role where name=?";
    private static final String HQL_DELETE_ROLE =
        "from Role where id=?";

    /**
     */
    public List getRoles() {
        return getHibernateTemplate().find(HQL_GET_ROLES);
    }

    /**
     */
    public Role getRole(Long id) {
        Role role = (Role) getHibernateTemplate().get(Role.class, id);
        if (role == null) {
            throw new ObjectRetrievalFailureException(Role.class, id);
        }
        return role;
    }

    /**
     */
    public Role getRole(String rolename) {
        List roles = getHibernateTemplate().find(HQL_GET_ROLE_BY_NAME,
                                                 rolename);
        if (roles.isEmpty()) {
            throw new ObjectRetrievalFailureException(Role.class, rolename);
        }
        return (Role) roles.iterator().next();
    }

    /**
     */
    public void saveRole(Role role) {
        role.setDateModified(new Date());
        role.setDateCreated(role.getDateModified());
        getHibernateTemplate().save(role);
    }

    /**
     */
    public void updateRole(Role role) {
        role.setDateModified(new Date());
        getHibernateTemplate().update(role);
    }

    /**
     */
    public void removeRole(Long id) {
        getHibernateTemplate().delete(HQL_DELETE_ROLE, id, Hibernate.LONG);
    }

    /**
     */
    public void removeRole(Role role) {
        removeRole(role.getId());
    }
}
