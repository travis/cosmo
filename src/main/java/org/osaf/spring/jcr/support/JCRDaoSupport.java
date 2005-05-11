package org.osaf.spring.jcr.support;

import org.osaf.spring.jcr.JCRTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenient super class for JCR data access objects.
 *
 * @author Brian Moseley
 */
public abstract class JCRDaoSupport {
    private static final Log log = LogFactory.getLog(JCRDaoSupport.class);

    private JCRTemplate template;
    private String workspaceName;

    /**
     */
    public JCRTemplate getTemplate() {
        return template;
    }

    /**
     */
    public void setTemplate(JCRTemplate template) {
        this.template = template;
    }

    /**
     */
    public String getWorkspaceName() {
        return workspaceName;
    }

    /**
     */
    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }
}
