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

import java.io.IOException;

import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.server.ServiceContext;
import org.apache.abdera.protocol.server.provider.TargetType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.provider.ItemTarget;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;

/**
 * Mock implementation of {@link RequestContext}.
 */
public class MockItemRequestContext extends BaseMockRequestContext {
    private static final Log log =
        LogFactory.getLog(MockItemRequestContext.class);

    public MockItemRequestContext(ServiceContext context,
                                  NoteItem item,
                                  String method) {
        this(context, item, method, false);
    }

    public MockItemRequestContext(ServiceContext context,
                                  String uid,
                                  String method) {
        this(context, uid, method, false);
    }

    public MockItemRequestContext(ServiceContext context,
                                  NoteItem item,
                                  String method,
                                  boolean isMedia) {
        this(context, item.getUid(), method, isMedia);
    }

    public MockItemRequestContext(ServiceContext context,
                                  String uid,
                                  String method,
                                  boolean isMedia) {
        super(context, method, toRequestUri(uid));
        this.target = new ItemTarget(type(isMedia), this, uid);
    }

    private static String toRequestUri(String uid) {
        return "/item/" + uid;
    }

    private static TargetType type(boolean isMedia) {
        return isMedia ? TargetType.TYPE_MEDIA : TargetType.TYPE_ENTRY;
    }

    public void setEntryContent(NoteItem item)
        throws IOException {
        String content = "this is item " + item.getUid();
        Entry entry = context.getAbdera().getFactory().newEntry();
        entry.setContent(content);
        String xml = (String)
            context.getAbdera().getWriterFactory().getWriter().write(entry);
        getMockRequest().setContentType("application/atom+xml");
        getMockRequest().setContent(xml.getBytes());
    }

    public void setMediaContent(NoteItem item)
        throws IOException {
        String content = "this is item " + item.getUid();
        getMockRequest().setContentType("text/plain");
        getMockRequest().setContent(content.getBytes());
    }
}
