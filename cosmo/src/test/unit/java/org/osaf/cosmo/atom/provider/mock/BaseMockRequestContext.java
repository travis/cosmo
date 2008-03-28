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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.server.ServiceContext;
import org.apache.abdera.protocol.server.impl.HttpServletRequestContext;
import org.apache.abdera.util.Constants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.AtomConstants;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;

/**
 * Mock implementation of {@link RequestContext}.
 */
public class BaseMockRequestContext extends HttpServletRequestContext
    implements AtomConstants, Constants {
    private static final Log log =
        LogFactory.getLog(BaseMockRequestContext.class);

    public BaseMockRequestContext(ServiceContext context,
                                  String method,
                                  String uri) {
        super(context, createRequest(method, uri));
    }

    private static MockHttpServletRequest createRequest(String method,
                                                        String uri) {
        MockServletContext ctx = new MockServletContext();
        return new MockHttpServletRequest(ctx, method, uri);
    }

    public MockHttpServletRequest getMockRequest() {
        return (MockHttpServletRequest) getRequest();
    }

    public void setContent(String content,
                           String mediaType)
        throws IOException {
        if (content != null) {
            byte[] bytes = content.getBytes("UTF-8");
            getMockRequest().setContent(bytes);
            getMockRequest().addHeader("Content-Length", bytes.length);
        }
        getMockRequest().setContentType(mediaType);
        getMockRequest().addHeader("Content-Type", mediaType);
    }

    public void setContent(Entry entry)
        throws IOException {
        String xml = context.getAbdera().getWriter().write(entry).toString();
        setContent(xml, ATOM_MEDIA_TYPE);
    }

    public void setContentAsText(String content)
        throws IOException {
        setContent(content, "text/plain");
    }

    public void setContentAsEntry(String content)
        throws IOException {
        Entry entry = context.getAbdera().getFactory().newEntry();
        entry.setContent(content);
        setContent(entry);
    }

    public void setContentAsXhtml(String content)
        throws IOException {
        setContent(content, "application/xhtml+xml");
    }

    public void setXhtmlContentAsEntry(String content)
        throws IOException {
        Entry entry = context.getAbdera().getFactory().newEntry();
        entry.setContentAsXhtml(content);
        setContent(entry);
    }

    public void setProperties(Properties props,
                              String mediaType)
        throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.store(out, null);
        setContent(out.toString(), mediaType);
    }

    public void setPropertiesAsText(Properties props)
        throws IOException {
        setProperties(props, "text/plain");
    }

    public void setPropertiesAsEntry(Properties props)
        throws IOException {
        Entry entry = context.getAbdera().getFactory().newEntry();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.store(out, null);
        entry.setContent(out.toString());
        setContent(entry);
    }
}
