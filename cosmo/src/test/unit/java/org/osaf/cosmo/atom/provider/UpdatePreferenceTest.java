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
import org.osaf.cosmo.atom.provider.mock.MockPreferenceRequestContext;
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.mock.MockPreference;

/**
 * Test class for {@link PreferencesProvider#updateEntry()} tests.
 */
public class UpdatePreferenceTest extends BasePreferencesCollectionAdapterTestCase {
    private static final Log log =
        LogFactory.getLog(UpdatePreferenceTest.class);

    public void testUpdateEntry() throws Exception {
        MockPreference pref = (MockPreference) helper.makeAndStoreDummyPreference();
        String oldKey = pref.getKey();
        MockPreference newpref = (MockPreference) newPreference();
        RequestContext req = createRequestContext(pref, newpref);

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 200, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertFalse("Matching etags",
                    pref.calculateEntityTag().equals(newpref.calculateEntityTag()));
        assertNotNull("Null last modified", res.getLastModified());

        String username = helper.getUser().getUsername();
        Preference saved = helper.getUserService().getUser(username).
            getPreference(oldKey);
        assertNull("Preference under old key still around", saved);

        saved = helper.getUserService().getUser(username).
            getPreference(newpref.getKey());
        assertNotNull("Preference not found under new key", saved);

        assertEquals("Wrong value", newpref.getValue(), saved.getValue());
    }

    public void testGenerationError() throws Exception {
        Preference pref = helper.makeDummyPreference();
        Preference newpref = newPreference();
        RequestContext req = createRequestContext(pref, newpref);
        helper.enableGeneratorFailure();

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
    }

    public void testEntryExists() throws Exception {
        Preference pref1 = helper.makeAndStoreDummyPreference();
        Preference pref2 = helper.makeAndStoreDummyPreference();
        Preference newpref = newPreference();
        newpref.setKey(pref2.getKey());
        RequestContext req = createRequestContext(pref1, newpref);

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 409, res.getStatus());
    }

    public void testEntryNoValue() throws Exception {
        Preference pref = helper.makeDummyPreference();
        Preference newpref = new MockPreference("new key", null);
        RequestContext req = createRequestContext(pref, newpref);

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 200, res.getStatus());
    }

    public void testInvalidEntryNoKey() throws Exception {
        Preference pref = helper.makeDummyPreference();
        Preference newpref = new MockPreference(null, "new value");
        RequestContext req = createRequestContext(pref, newpref);

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    private Preference newPreference() {
        Preference pref = new MockPreference();
        pref.setKey("new key");
        pref.setValue("new value");
        return pref;
    }

    private RequestContext createRequestContext(Preference pref,
                                                Preference newpref)
        throws Exception {
        MockPreferenceRequestContext rc =
            new MockPreferenceRequestContext(provider,
                                             helper.getUser(), pref, "POST");
        rc.setXhtmlContentAsEntry(serialize(newpref));
        return rc;
    }
}
