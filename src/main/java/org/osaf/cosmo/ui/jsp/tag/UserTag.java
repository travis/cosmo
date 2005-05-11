package org.osaf.cosmo.ui.jsp.tag;

import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityException;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.jsp.tag.SimpleVarSetterTag;
import org.osaf.spring.web.util.WebApplicationContextAwareTagUtils;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This tag provides access to the {@link User} object provided by
 * the current Cosmo security context as the value of a scripting
 * variable.
 *
 * @see org.osaf.jsp.tag.SimpleVarSetterTag
 */
public class UserTag extends SimpleVarSetterTag {
    private static final Log log = LogFactory.getLog(UserTag.class);

    /** The name of the Spring bean identifying the tag's
     * {@link CosmoSecurityManager}
     */
    public static final String BEAN_SECURITY_MANAGER =
        "securityManager";

    /**
     * @return the <code>User</code> provided by the current security
     * context 
     * @throws JspException if there is an error obtaining the
     * security context
     */
    public Object computeValue()
        throws JspException {
        try {
            ServletContext sc =
                ((PageContext)getJspContext()).getServletContext();
            CosmoSecurityManager securityManager = (CosmoSecurityManager)
                WebApplicationContextAwareTagUtils.
                getBean(sc, BEAN_SECURITY_MANAGER, CosmoSecurityManager.class);
            return securityManager.getSecurityContext().getUser();
        } catch (CosmoSecurityException e) {
            throw new JspException("can't get security context", e);
        }
    }
}
