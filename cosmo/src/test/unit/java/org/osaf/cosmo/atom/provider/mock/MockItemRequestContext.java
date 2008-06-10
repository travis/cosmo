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

import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.provider.ItemTarget;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.mock.MockNoteItem;

/**
 * Mock implementation of {@link RequestContext}.
 */
public class MockItemRequestContext extends BaseMockRequestContext
    implements Constants {
    private static final Log log =
        LogFactory.getLog(MockItemRequestContext.class);

    public MockItemRequestContext(Provider provider,
                                  NoteItem item,
                                  String method) {
        this(provider, item, method, null, null);
    }

    public MockItemRequestContext(Provider provider,
                                  NoteItem item,
                                  String method,
                                  String projection,
                                  String format) {
        super(provider, method, toRequestUri(item, projection, format));
        this.target = new ItemTarget(this, item, projection, format);
    }

    public MockItemRequestContext(Provider provider,
                                  String uid,
                                  String method) {
        this(provider, newItem(uid), method);
    }

    public MockItemRequestContext(Provider provider,
                                  String uid,
                                  String method,
                                  String projection,
                                  String format) {
        this(provider, newItem(uid), method, projection, format);
    }

    private static String toRequestUri(NoteItem item,
                                       String projection,
                                       String format) {
        return TEMPLATE_ITEM.bind(item.getUid(), projection, format);
    }

    private static NoteItem newItem(String uid) {
        NoteItem item = new MockNoteItem();
        item.setUid(uid);
        return item;
    }
}
