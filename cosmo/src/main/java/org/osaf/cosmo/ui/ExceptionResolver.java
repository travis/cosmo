/*
 * Copyright 2007 Open Source Applications Foundation
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

/**
 * Extends the Spring {@link SimpleMappingExceptionResolver}
 * to provide Cosmo-specific behaviors.
 */
public class ExceptionResolver extends SimpleMappingExceptionResolver {
    private static final Log log = LogFactory.getLog(ExceptionResolver.class);

    // SimpleMappingExceptionResolver methods

    /**
     * Overrides the superclass method to log the exception at the error
     * level.
     */
	protected void logException(Exception e,
	                            HttpServletRequest request) {
	    log.error("Internal UI error", e);
	}
}
