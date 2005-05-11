package org.osaf.cosmo.dao.jcr;

import org.osaf.cosmo.dao.ShareDAO;
import org.osaf.spring.jcr.JCRCallback;
import org.osaf.spring.jcr.support.JCRDaoSupport;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JCR implementation of ShareDAO.
 *
 * @author Brian Moseley
 */
public class ContentStoreDAOJCR extends JCRDaoSupport implements ShareDAO {
    private static final Log log = LogFactory.getLog(ContentStoreDAOJCR.class);

    private static final String NODE_TYPE_COLLECTION = "nt:folder";

    /**
     */
    public void createHomedir(final String username) {
        if (log.isDebugEnabled()) {
            log.debug("creating homedir for " + username);
        }
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    Node rootNode = session.getRootNode();
                    rootNode.addNode(username, NODE_TYPE_COLLECTION);
                    rootNode.save();
                    return null;
                }
            });
    }

    /**
     */
    public boolean existsHomedir(final String username) {
        if (log.isDebugEnabled()) {
            log.debug("checking existence of homedir for " + username);
        }
        Boolean rv = (Boolean) getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    return new Boolean(session.itemExists("/" + username));
                }
            });
        return rv.booleanValue();
    }

    /**
     */
    public void deleteHomedir(final String username) {
        if (log.isDebugEnabled()) {
            log.debug("deleting homedir for " + username);
        }
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    Item homedir = session.getItem("/" + username);
                    homedir.remove();
                    session.save();
                    return null;
                }
            });
    }
}
