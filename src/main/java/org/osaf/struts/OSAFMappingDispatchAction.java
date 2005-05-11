package org.osaf.struts;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.MappingDispatchAction;

/**
 * Base class for MappingDispatchActions that provides important
 * utility methods.
 *
 * In general it's ideal to delegate to classes outside the Action
 * hierarchy as much as possible, rather than extending Action. In
 * this case, Struts makes several important methods of Action
 * protected, so we must insert a class into the Action
 * hierarchy.
 */
public class OSAFMappingDispatchAction extends MappingDispatchAction {
    private static final Log log =
        LogFactory.getLog(OSAFMappingDispatchAction.class);
    /**
     * The request parameter in which view title parameters are
     * stored: <code>TitleParam</code>
     */
    public static final String ATTR_TITLE_PARAM = "TitleParam";

    /**
     * Store a title parameter in the <code>ATTR_TITLE_PARAM</code>
     * request attribute.
     *
     * Title parameters can be used by a view to build a page title
     * including dynamic elements that are set by the action but not
     * known by the view at rendering time. An example is using a
     * layout JSP to include the name of an object being managed via
     * an administrative console in the title of an HTML page without
     * the layout knowing the type of object it is displaying.
     */
    public void addTitleParam(HttpServletRequest request,
                              String param) {
        List params = (List) request.getAttribute(ATTR_TITLE_PARAM);
        if (params == null) {
            params = new ArrayList();
            request.setAttribute(ATTR_TITLE_PARAM, params);
        }
        params.add(param);
    }

    /**
     * Saves the key for a global confirmation message into the HTTP
     * session such that it can be retrieved by the Struts
     * <html:messages message="true"/> tag.
     */
    public void saveConfirmationMessage(HttpServletRequest request,
                                        String key) {
        ActionMessages messages = new ActionMessages();
        messages.add(ActionMessages.GLOBAL_MESSAGE,
                     new ActionMessage(key));
        saveMessages(request.getSession(), messages);
    }

    /**
     * Saves the key for a property-specific error message into the
     * HTTP request such that it can be retrieved by the Struts
     * <html:messages/> tag.
     */
    public void saveErrorMessage(HttpServletRequest request,
                                 String key,
                                 String property) {
        if (property == null) {
            throw new IllegalArgumentException("cannot save an error message with a null property");
        }
        ActionMessages messages = new ActionMessages();
        messages.add(property, new ActionMessage(key));
        saveErrors(request, messages);
    }

    /**
     * Saves the key for a global error message into the HTTP request
     * such that it can be retrieved by the Struts <html:messages/>
     * tag.
     */
    public void saveErrorMessage(HttpServletRequest request,
                                 String key) {
        saveErrorMessage(request, key, ActionMessages.GLOBAL_MESSAGE);
    }
}
