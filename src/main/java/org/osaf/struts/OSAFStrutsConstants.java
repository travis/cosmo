package org.osaf.struts;

/**
 * Constant definitions for common Struts constructs.
 */
public class OSAFStrutsConstants {
    /**
     * The request attribute which is used to carry an exception from
     * the Struts layer (or, if configured in the web application
     * deployment descriptor, the servlet container itself) to the
     * view layer.
     */
    public static final String ATTR_EXCEPTION = "Exception";
    /**
     * The Struts forward used by actions that have a single outcome.
     */
    public static final String FWD_OK = "ok";
    /**
     * The Struts forward used by actions to signify that a form
     * submission was canceled.
     */
    public static final String FWD_CANCEL = "cancel";
    /**
     * The Struts forward used by actions to signify that a form
     * submission failed due to a user input or some other such
     * recoverable error.
     */
    public static final String FWD_FAILURE = "failure";
    /**
     * The Struts forward used by actions to signify that a form
     * submission succeeded.
     */
    public static final String FWD_SUCCESS = "success";
    /**
     * The Struts forward used by actions to signify that some sort
     * of non-recoverable error occurred.
     */
    public static final String FWD_ERROR = "error";
    /**
     * The request parameter that represents the click of a create
     * button.
     */
    public static final String PARAM_BUTTON_CREATE = "create";
}
