package org.osaf.struts;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * A base class for actions that control the handling of exceptions
 * caught by Struts (and, if configured in the web application
 * deployment descriptor, by the servlet container).
 */
public abstract class OSAFErrorAction extends Action {
    private static final String ATTR_CONTAINER_SOURCE =
        "javax.servlet.error.exception";
    private static final String ATTR_STRUTS_SOURCE =
        "org.apache.struts.action.EXCEPTION";
    private static final Log log = LogFactory.getLog(OSAFErrorAction.class);

    /** Finds the exception thrown by the container or a library and
     * stores it in the exception named by
     * <code>OSAFStrutsConstants.ATTR_EXCEPTION</code>. Delegates to
     * <code>findForward</code> to generate the return value.
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
        Throwable t = (Throwable) request.getAttribute(ATTR_CONTAINER_SOURCE);
        if (t == null) {
            t = (Throwable) request.getAttribute(ATTR_STRUTS_SOURCE);
        }
        if (t == null) {
            t = (Throwable)
                request.getAttribute(OSAFStrutsConstants.ATTR_EXCEPTION);
        }

        if (t != null && t instanceof ServletException) {
            ServletException se = (ServletException) t;
            Throwable st = (Throwable) se.getRootCause();
            if (st != null) {
                t = st;
            }
        }

        request.setAttribute(OSAFStrutsConstants.ATTR_EXCEPTION, t);

        return findForward(mapping, form, request, response);
    }

    /**
     * Subclasses must implement this method to return a Struts
     * forward for the appropriate view. Some subclasses will use a
     * single view for all errors, while others may examine the
     * discovered exception or other inputs to choose an appropriate
     * view.
     */
    public abstract ActionForward findForward(ActionMapping mapping,
                                              ActionForm form,
                                              HttpServletRequest request,
                                              HttpServletResponse response);
}
