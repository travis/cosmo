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
package org.osaf.cosmo.ui;

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
public class CosmoExceptionHandler extends ExceptionHandler {
    private static final Log log =
        LogFactory.getLog(CosmoExceptionHandler.class);

    /**
     * Places the caught exception into the
     * <code>UIConstants.ATTR_EXCEPTION</code> request
     * attribute and returns the
     * <code>UIConstants.FWD_ERROR</code> forward.
     */
    public ActionForward execute(Exception e,
                                 ExceptionConfig config,
                                 ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws ServletException {
        request.setAttribute(UIConstants.ATTR_EXCEPTION, e);
        if (log.isDebugEnabled()) {
            log.debug("caught exception from Struts", e);
        }
        return mapping.findForward(UIConstants.FWD_ERROR);
    }
}
