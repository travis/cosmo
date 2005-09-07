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
package org.osaf.cosmo.ui;

import org.osaf.commons.struts.OSAFAction;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.ui.config.ServletContextConfigurer;

/**
 * Base class for Cosmo actions, including security configuration.
 */
public class CosmoAction extends OSAFAction {

    private ServletContextConfigurer configurer;
    private CosmoSecurityManager securityManager;

    /**
     */
    public ServletContextConfigurer getConfigurer() {
        return configurer;
    }

    /**
     */
    public void setConfigurer(ServletContextConfigurer configurer) {
        this.configurer = configurer;
    }

    /**
     */
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     */
    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }
}
