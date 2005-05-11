package org.osaf.jcr.jackrabbit;

import java.util.Hashtable;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;

/**
 * An implementation of {@link ObjectFactory} that creates an instance
 * of Jackrabbit's {@link RepositoryImpl}.
 */
public class JackrabbitRepositoryFactory implements ObjectFactory {
    private static final Log log =
        LogFactory.getLog(JackrabbitRepositoryFactory.class);

    /**
     * type of <code>configFilePath</code> reference address
     * (@see <code>{@link Reference#get(String)}</code>
     */
    public static final String CONFIGFILEPATH_ADDRTYPE = "configFilePath";
    /**
     * type of <code>repHomeDir</code> reference address
     * (@see <code>{@link Reference#get(String)}</code>
     */
    public static final String REPHOMEDIR_ADDRTYPE = "repHomeDir";

    private Repository repository;

    /**
     */
    public JackrabbitRepositoryFactory() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment)
        throws NamingException {
        if (repository == null) {
            try {
                Reference ref = (Reference) obj;
                String configFilePath =
                    (String) ref.get(CONFIGFILEPATH_ADDRTYPE).getContent();
                String repHomeDir =
                    (String) ref.get(REPHOMEDIR_ADDRTYPE).getContent();

                if (log.isDebugEnabled()) {
                    log.debug("loading repository at " + repHomeDir +
                              " with config " + configFilePath);
                }
                RepositoryConfig config =
                    RepositoryConfig.create(configFilePath, repHomeDir);
                repository = RepositoryImpl.create(config);
            } catch (RepositoryException e) {
                log.error("error creating repository", e);
                throw new NamingException("error creating repository: " +
                                          e.getMessage());
            }
        }
        return repository;
    }
}
