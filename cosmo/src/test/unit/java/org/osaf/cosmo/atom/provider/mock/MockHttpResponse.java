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
package org.osaf.cosmo.atom.provider.mock;

import org.apache.abdera.protocol.server.HttpResponse;
import org.apache.abdera.protocol.server.servlet.HttpResponseServletAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Mock implementation of {@link HttpResponse}.
 */
public class MockHttpResponse extends HttpResponseServletAdapter {
    private static final Log log = LogFactory.getLog(MockHttpResponse.class);

    public MockHttpResponse() {
        super(new MockHttpServletResponse());
    }

    public int getStatus() {
        return ((MockHttpServletResponse)getActual()).getStatus();
    }

    public String getEtag() {
        return (String)
            ((MockHttpServletResponse)getActual()).getHeader("ETag");
    }
}
