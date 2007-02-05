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
package org.osaf.cosmo.ui.jsp.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.util.TagUtils;

/**
 * A base class for JSP Simple Tag Handlers which need to set a
 * scripting variable. It provides <code>scope</code> and
 * <code>var</code> attributes allowing the JSP programmer to specify
 * the name and scope of the scripting variable.
 *
 * Subclasses must implement the <code>computeValue</code> method,
 * the result of which will be set as the value of the scripting
 * variable.
 *
 * <code>name</code> is a required attribute, but <code>scope</code>
 * is optional, defaulting to page scope. Scope is specified as a
 * <code>String</code>, one of <code>application</code>,
 * <code>session</code>, <code>request</code>, or
 * <code>page</code>.
 */
public abstract class SimpleVarSetterTag extends SimpleTagSupport {
    private static final Log log = LogFactory.getLog(SimpleVarSetterTag.class);

    private String scope = TagUtils.SCOPE_PAGE;
    private String var;

    /**
     */
    public void doTag()
        throws JspException, IOException {
        getJspContext().setAttribute(var, computeValue(),
                                     TagUtils.getScope(scope));
    }

    /**
     * @return the <code>Object</code> that will be set as the value
     * of the scripting variable
     * @throws JspException
     */
    public abstract Object computeValue()
        throws JspException;

    /**
     */
    public String getScope() {
        return scope;
    }

    /**
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     */
    public String getVar() {
        return var;
    }

    /**
     */
    public void setVar(String var) {
        this.var = var;
    }
}
