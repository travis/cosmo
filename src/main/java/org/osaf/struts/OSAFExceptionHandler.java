package org.osaf.struts;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ExceptionHandler;
import org.apache.struts.config.ExceptionConfig;

/**
 * A Struts ExceptionHandler that places the caught exception into a
 * well-known location so that other components can easily find it.
 */
public class OSAFExceptionHandler extends ExceptionHandler {
    private static final Log log =
        LogFactory.getLog(OSAFExceptionHandler.class);

    /**
     * Places the caught exception into the
     * <code>OSAFStrutsConstants.ATTR_EXCEPTION</code> request
     * attribute and returns the
     * <code>OSAFStrutsConstants.FWD_ERROR</code> forward.
     */
    public ActionForward execute(Exception e,
                                 ExceptionConfig config,
                                 ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws ServletException {
        request.setAttribute(OSAFStrutsConstants.ATTR_EXCEPTION, e);
        if (log.isDebugEnabled()) {
            log.debug("caught exception from Struts", e);
        }
        return mapping.findForward(OSAFStrutsConstants.FWD_ERROR);
    }
}
