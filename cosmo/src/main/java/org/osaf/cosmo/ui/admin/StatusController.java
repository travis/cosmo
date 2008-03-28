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
package org.osaf.cosmo.ui.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.server.StatusSnapshot;
import org.osaf.cosmo.ui.UIConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * Action that provides a snapshot of the server status via
 * {@link StatusSnapshot}.
 */
public class StatusController extends MultiActionController {
    private static final Log log = LogFactory.getLog(StatusController.class);
    private static final String MSG_CONFIRM_GC = "Status.GC";
    private String dumpView;
    private String viewView;
    private String gcView;

    /**
     * The request attribute where the status snapshot is stored.
     */
    public static final String ATTR_STATUS = "Status";

    /**
     * Causes a snapshot of the server status to be taken and stored
     * in the request underneath the {@link #ATTR_STATUS} request
     * attribute and returns the view specified by {@link viewView}.
     */
    public ModelAndView view(HttpServletRequest request,
    		HttpServletResponse response)
    throws Exception {
    	StatusSnapshot snapshot = takeSnapshot();

    	return new ModelAndView(viewView, ATTR_STATUS, snapshot);
    }
    
    /**
     * Causes a snapshot of the server status to be taken and stored
     * in the request underneath the {@link #ATTR_STATUS} request
     * attribute and returns the view specified by {@link dumpView}.
     */
    public ModelAndView dump(HttpServletRequest request,
    		HttpServletResponse response)
    throws Exception {
    	StatusSnapshot snapshot = takeSnapshot();

    	return new ModelAndView(dumpView, ATTR_STATUS, snapshot);
    }

    /**
     * Causes the JVM to begin a garbage collection run
     * (asynchronously, in a separate thread) and forwards to the
     * {@link UIConstants#FWD_OK} forward.
     */
    public ModelAndView gc(HttpServletRequest request,
                           HttpServletResponse response)
        throws Exception {
        System.gc();

        //saveConfirmationMessage(request, MSG_CONFIRM_GC);

        return new ModelAndView(gcView);
    }

    /**
     * Takes a snapshot of the server status.
     */
    protected StatusSnapshot takeSnapshot() {
        return new StatusSnapshot();
    }

	public void setDumpView(String dumpView) {
		this.dumpView = dumpView;
	}

	public void setGcView(String gcView) {
		this.gcView = gcView;
	}

	public void setViewView(String viewView) {
		this.viewView = viewView;
	}

}
