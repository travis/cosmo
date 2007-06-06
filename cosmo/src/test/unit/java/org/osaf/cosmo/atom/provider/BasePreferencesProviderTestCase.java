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

import org.apache.abdera.model.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.model.Preference;

/**
 * Base class for for {@link PreferencesProvider} tests.
 */
public abstract class BasePreferencesProviderTestCase
    extends BaseProviderTestCase implements AtomConstants {
    private static final Log log =
        LogFactory.getLog(BasePreferencesProviderTestCase.class);

    protected BaseProvider createProvider() {
        PreferencesProvider provider = new PreferencesProvider();
        provider.setUserService(helper.getUserService());
        return provider;
    }

    protected Entry serialize(Preference pref) {
        Entry entry = helper.getAbdera().getFactory().newEntry();
        entry.setTitle(pref.getKey());
        if (pref.getValue() != null)
            entry.setContent(pref.getValue());
        return entry;
    }
}