package org.osaf.cosmo;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Base class for Cosmo TestCases.
 *
 * @author Brian Moseley
 */
public class BaseCoreTestCase extends TestCase {
    private static final Log log = LogFactory.getLog(BaseCoreTestCase.class);
    private ApplicationContext appCtx = null;

    /**
     */
    public BaseCoreTestCase(String name) {
        super(name);
        String[] paths = {
            "/applicationContext-test.xml",
            "/applicationContext-hibernate.xml",
            "/applicationContext-jcr.xml",
            "/applicationContext-provisioning.xml"
        };
        appCtx = new ClassPathXmlApplicationContext(paths);
    }

    /**
     */
    public ApplicationContext getAppContext() {
        return appCtx;
    }
}
