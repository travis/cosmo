/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.spring.jcr.support;

import org.osaf.spring.jcr.JCRTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenient super class for JCR data access objects.
 *
 * @author Brian Moseley
 */
public abstract class JCRDaoSupport {
    private static final Log log = LogFactory.getLog(JCRDaoSupport.class);

    private JCRTemplate template;
    private String workspaceName;

    /**
     */
    public JCRTemplate getTemplate() {
        return template;
    }

    /**
     */
    public void setTemplate(JCRTemplate template) {
        this.template = template;
    }

    /**
     */
    public String getWorkspaceName() {
        return workspaceName;
    }

    /**
     */
    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }
}
