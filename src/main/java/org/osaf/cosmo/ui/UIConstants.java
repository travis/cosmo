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

/**
 * Constant definitions for common Struts constructs.
 */
public class UIConstants {
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
