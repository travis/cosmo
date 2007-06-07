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
import org.apache.abdera.util.Constants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.provider.ItemTarget;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;

/**
 * Mock implementation of {@link RequestContext}.
 */
public class MockItemRequestContext extends BaseMockRequestContext
    implements Constants {
    private static final Log log =
        LogFactory.getLog(MockItemRequestContext.class);

    public MockItemRequestContext(ServiceContext context,
                                  NoteItem item,
                                  String method) {
        this(context, item, method, null, null, null);
    }

    public MockItemRequestContext(ServiceContext context,
                                  NoteItem item,
                                  String method,
                                  String contentType) {
        this(context, item, method, contentType, null, null);
    }

    public MockItemRequestContext(ServiceContext context,
                                  NoteItem item,
                                  String method,
                                  String contentType,
                                  String projection,
                                  String format) {
        super(context, method, toRequestUri(item, projection, format));
        if (contentType == null)
            contentType = ATOM_MEDIA_TYPE;
        getMockRequest().setContentType(contentType);
        getMockRequest().addHeader("Content-Type", contentType);
        this.target = new ItemTarget(this, item, projection, format);
    }

    public MockItemRequestContext(ServiceContext context,
                                  String uid,
                                  String method,
                                  String contentType) {
        this(context, newItem(uid), method, contentType);
    }

    public MockItemRequestContext(ServiceContext context,
                                  String uid,
                                  String method,
                                  String contentType,
                                  String projection,
                                  String format) {
        this(context, newItem(uid), method, contentType, projection, format);
    }

    private static String toRequestUri(NoteItem item,
                                       String projection,
                                       String format) {
        return TEMPLATE_ITEM.bind(item.getUid(), projection, format);
    }

    private static NoteItem newItem(String uid) {
        NoteItem item = new NoteItem();
        item.setUid(uid);
        return item;
    }

    public void setContentAsEntry(NoteItem item)
        throws IOException {
        setContentAsEntry("this is item " + item.getUid());
    }

    public void setContentAsText(NoteItem item)
        throws IOException {
        setContentAsText("this is item " + item.getUid());
    }
}
