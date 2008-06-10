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
package org.osaf.cosmo.atom.provider;

import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.provider.mock.MockPreferencesRequestContext;
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.hibernate.HibPreference;

/**
 * Test class for {@link PreferenceProvider#createEntry()} tests.
 */
public class CreatePreferenceTest extends BasePreferencesCollectionAdapterTestCase {
    private static final Log log =
        LogFactory.getLog(CreatePreferenceTest.class);

    public void testCreateEntry() throws Exception {
        Preference pref = helper.makeDummyPreference();
        RequestContext req = createRequestContext(pref);

        ResponseContext res = adapter.postEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 201, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        // disable last modified check til mock dao bumps modified date
        //assertNotNull("Null last modified", res.getLastModified());
        assertNotNull("Null Location header", res.getHeader("Location"));
        assertNotNull("Null Content-Location header",
                      res.getHeader("Content-Location"));

        String username = helper.getUser().getUsername();
        Preference saved = helper.getUserService().
            getUser(username).getPreference(pref.getKey());
        assertNotNull("Preference not saved", saved);
        assertEquals("Wrong key", pref.getKey(), saved.getKey());
        assertEquals("Wrong value", pref.getValue(), saved.getValue());
    }

    public void testGenerationError() throws Exception {
        Preference pref = helper.makeDummyPreference();
        RequestContext req = createRequestContext(pref);
        helper.enableGeneratorFailure();

        ResponseContext res = adapter.postEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
    }

    public void testEntryExists() throws Exception {
        Preference pref = helper.makeAndStoreDummyPreference();
        RequestContext req = createRequestContext(pref);

        ResponseContext res = adapter.postEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 409, res.getStatus());
    }

    public void testEntryNoValue() throws Exception {
        Preference pref = new HibPreference("new key", null);
        RequestContext req = createRequestContext(pref);

        ResponseContext res = adapter.postEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 201, res.getStatus());
    }

    public void testInvalidEntryNoKey() throws Exception {
        Preference pref = new HibPreference(null, "new value");
        RequestContext req = createRequestContext(pref);

        ResponseContext res = adapter.postEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    private RequestContext createRequestContext(Preference pref)
        throws Exception {
        MockPreferencesRequestContext rc =
            new MockPreferencesRequestContext(provider,
                                              helper.getUser(), "POST");
        rc.setXhtmlContentAsEntry(serialize(pref));
        return rc;
    }
}
