/*
 * Copyright 2005-2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.ui.status;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.osaf.cosmo.server.StatusSnapshot;
import org.osaf.cosmo.ui.CosmoAction;
import org.osaf.cosmo.ui.UIConstants;

/**
 * Action that provides a snapshot of the server status via
 * {@link StatusSnapshot}.
 */
public class StatusAction extends CosmoAction {
    private static final Log log = LogFactory.getLog(StatusAction.class);
    private static final String MSG_CONFIRM_GC = "Status.GC";

    /**
     * The request attribute where the status snapshot is stored.
     */
    public static final String ATTR_STATUS = "Status";

    /**
     * Causes a snapshot of the server status to be taken and stored
     * in the request underneath the {@link #ATTR_STATUS} request
     * attribute and forwards to the
     * {@link UIConstants#FWD_OK} forward.
     */
    public ActionForward status(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
        throws Exception {
        StatusSnapshot snapshot = takeSnapshot();

        request.setAttribute(ATTR_STATUS, snapshot);

        return mapping.findForward(UIConstants.FWD_OK);
    }

    /**
     * Causes the JVM to begin a garbage collection run
     * (asynchronously, in a separate thread) and forwards to the
     * {@link UIConstants#FWD_OK} forward.
     */
    public ActionForward gc(ActionMapping mapping,
                            ActionForm form,
                            HttpServletRequest request,
                            HttpServletResponse response)
        throws Exception {
        System.gc();

        saveConfirmationMessage(request, MSG_CONFIRM_GC);

        return mapping.findForward(UIConstants.FWD_OK);
    }

    /**
     * Takes a snapshot of the server status.
     */
    protected StatusSnapshot takeSnapshot() {
        return new StatusSnapshot();
    }
}
