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

import java.io.IOException;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.EntityTag;
import org.apache.abdera.protocol.server.provider.AbstractResponseContext;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;
import org.apache.abdera.util.Constants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.atom.generator.GeneratorException;
import org.osaf.cosmo.atom.generator.PreferencesFeedGenerator;
import org.osaf.cosmo.atom.processor.ValidationException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.service.UserService;

public class PreferencesProvider extends BaseProvider
    implements AtomConstants {
    private static final Log log = LogFactory.getLog(PreferencesProvider.class);

    private UserService userService;

    // Provider methods

    public ResponseContext createEntry(RequestContext request) {
        PreferencesTarget target = (PreferencesTarget) request.getTarget();
        User user = target.getUser();

        // XXX: check write preconditions?

        try {
            Entry entry = (Entry) request.getDocument().getRoot();
            Preference pref = readPreference(entry);

            if (user.getPreference(pref.getKey()) != null)
                return conflict(getAbdera(), request, "Preference exists");

            if (log.isDebugEnabled())
                log.debug("creating preference " + pref.getKey() +
                          " for user " + user.getUsername());

            user.addPreference(pref);
            user = userService.updateUser(user);

            ServiceLocator locator = createServiceLocator(request);
            PreferencesFeedGenerator generator =
                createPreferencesFeedGenerator(locator);
            entry = generator.generateEntry(pref);

            AbstractResponseContext rc =
                createResponseContext(entry.getDocument(), 201, "Created");
            rc.setContentType(Constants.ATOM_MEDIA_TYPE);
            rc.setEntityTag(new EntityTag(pref.getEntityTag()));

            try {
                rc.setLocation(entry.getSelfLink().getHref().toString());
                rc.setContentLocation(entry.getSelfLink().getHref().toString());
            } catch (Exception e) {
                throw new RuntimeException("Error parsing self link href", e);
            }

            return rc;
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (ValidationException e) {
            return badrequest(getAbdera(), request, "Invalid preference entry: " + e.getMessage());
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        }
    }

    public ResponseContext deleteEntry(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext deleteMedia(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext updateEntry(RequestContext request) {
        PreferenceTarget target = (PreferenceTarget) request.getTarget();
        User user = target.getUser();
        Preference pref = target.getPreference();
        if (log.isDebugEnabled())
            log.debug("upudating preference " + pref.getKey() + " for user " +
                      user.getUsername());

        // XXX: check write preconditions?

        try {
            Entry entry = (Entry) request.getDocument().getRoot();
            Preference newpref = readPreference(entry);

            if (user.getPreference(newpref.getKey()) != null &&
                ! user.getPreference(newpref.getKey()).equals(pref))
                return conflict(getAbdera(), request, "Preference exists");

            pref.setKey(newpref.getKey());
            pref.setValue(newpref.getValue());

            user = userService.updateUser(user);

            ServiceLocator locator = createServiceLocator(request);
            PreferencesFeedGenerator generator =
                createPreferencesFeedGenerator(locator);
            entry = generator.generateEntry(pref);

            AbstractResponseContext rc =
                createResponseContext(entry.getDocument());
            rc.setContentType(Constants.ATOM_MEDIA_TYPE);
            rc.setEntityTag(new EntityTag(pref.getEntityTag()));

            return rc;
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (ValidationException e) {
            return badrequest(getAbdera(), request, "Invalid entry: " + e.getMessage());
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        }
    }
  
    public ResponseContext updateMedia(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext getService(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext getFeed(RequestContext request) {
        PreferencesTarget target = (PreferencesTarget) request.getTarget();
        User user = target.getUser();
        if (log.isDebugEnabled())
            log.debug("getting preferences feed for user " +
                      user.getUsername());

        try {
            ServiceLocator locator = createServiceLocator(request);
            PreferencesFeedGenerator generator =
                createPreferencesFeedGenerator(locator);
            Feed feed = generator.generateFeed(user);

            // no entity tag for this synthetic feed
            return createResponseContext(feed.getDocument());
        } catch (GeneratorException e) {
            String reason = "Unknown feed generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        }
    }

    public ResponseContext getEntry(RequestContext request) {
        PreferenceTarget target = (PreferenceTarget) request.getTarget();
        User user = target.getUser();
        Preference pref = target.getPreference();
        if (log.isDebugEnabled())
            log.debug("getting entry for preference " +
                      pref.getKey() + " for user " + user.getUsername());

        try {
            ServiceLocator locator = createServiceLocator(request);
            PreferencesFeedGenerator generator =
                createPreferencesFeedGenerator(locator);
            Entry entry = generator.generateEntry(pref);

            AbstractResponseContext rc =
                createResponseContext(entry.getDocument());
            rc.setEntityTag(new EntityTag(pref.getEntityTag()));
            rc.setContentType(Constants.ATOM_MEDIA_TYPE);
            return rc;
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        }
    }
  
    public ResponseContext getMedia(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext getCategories(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext entryPost(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext mediaPost(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    // ExtendedProvider methods

    public ResponseContext updateCollection(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    // our methods

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void init() {
        super.init();
        if (userService == null)
            throw new IllegalStateException("userService is required");
    }

    protected PreferencesFeedGenerator
        createPreferencesFeedGenerator(ServiceLocator locator) {
        return getGeneratorFactory().
            createPreferencesFeedGenerator(locator);
    }

    private Preference readPreference(Entry entry)
        throws ValidationException {
        Preference pref = new Preference();

        if (entry.getContent() == null)
            throw new ValidationException("No entry content found");

        pref.setKey(entry.getTitle());
        pref.setValue(entry.getContent());
        return pref;
    }
}
