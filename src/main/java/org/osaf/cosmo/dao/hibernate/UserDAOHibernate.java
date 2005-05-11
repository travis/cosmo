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

    /**
     */
    public User getUser(final String username) {
        return (User) getHibernateTemplate().execute(new HibernateCallback() {
                public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                    List users = session.find(HQL_GET_USER_BY_USERNAME,
                                              username, Hibernate.STRING);
                    if (users.isEmpty()) {
                        throw new ObjectRetrievalFailureException(User.class,
                                                                  username);
                    }
                    User user = (User) users.get(0);
                    Hibernate.initialize(user.getRoles());
                    return user;
                }
            });
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
