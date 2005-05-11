package org.osaf.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


/**
 * An {@link org.apache.struts.action.Action} that logs the user out
 * of an application.
 */
public class OSAFLogoutAction extends Action {
    private static final Log log = LogFactory.getLog(OSAFLogoutAction.class);

    /**
     * Logs the user out of the application by invalidating his
     * {@link javax.servlet.http.HttpSession} and then returns the
     * <code>OK</code> forward.
     *
     * @see OSAFStrutsConstants#FWD_OK
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
        request.getSession().invalidate();

        return mapping.findForward(OSAFStrutsConstants.FWD_OK);
    }
}
