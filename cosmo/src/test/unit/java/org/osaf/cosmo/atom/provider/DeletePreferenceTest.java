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

/**
 * Test class for {@link PreferencesProvider#deleteEntry()} tests.
 */
public class DeletePreferenceTest extends BasePreferencesCollectionAdapterTestCase {
    private static final Log log =
        LogFactory.getLog(DeletePreferenceTest.class);

    public void testDeleteEntry() throws Exception {
        Preference pref = helper.makeAndStoreDummyPreference();
        RequestContext req = createRequestContext(pref);

        ResponseContext res = adapter.deleteEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertNull("Preference not removed",
                   helper.getUser().getPreference(pref.getKey()));
    }

    private RequestContext createRequestContext(Preference pref) {
        return new MockPreferenceRequestContext(provider,
                                                helper.getUser(), pref,
                                               "DELETE");
    }
}
