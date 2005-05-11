package org.osaf.cosmo.dao;

import java.util.List;

/**
 * DAO interface for the Shared File Store.
 *
 * @author Brian Moseley
 */
public interface ShareDAO extends DAO {

    /**
     */
    public void createHomedir(String username);

    /**
     */
    public boolean existsHomedir(String username);

    /**
     */
    public void deleteHomedir(String username);
}
