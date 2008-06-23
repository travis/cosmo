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

import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.generator.GeneratorException;
import org.osaf.cosmo.atom.generator.ItemFeedGenerator;
import org.osaf.cosmo.atom.generator.UnsupportedFormatException;
import org.osaf.cosmo.atom.generator.UnsupportedProjectionException;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.server.ServiceLocator;

public class ExpandedItemCollectionAdapter extends ItemCollectionAdapter {
    private static final Log log =
        LogFactory.getLog(ExpandedItemCollectionAdapter.class);
    private static final String[] ALLOWED_COLL_METHODS =
        new String[] { "GET", "HEAD", "OPTIONS" };
    private static final String[] ALLOWED_ENTRY_METHODS =
        new String[] { "OPTIONS" };

    // Provider methods
    @Override
    public ResponseContext putEntry(RequestContext request) {
        return ProviderHelper.notallowed(request, ALLOWED_COLL_METHODS);
    }

    @Override
    public ResponseContext deleteEntry(RequestContext request) {
        return ProviderHelper.notallowed(request, ALLOWED_ENTRY_METHODS);
    }

    @Override
    public ResponseContext postEntry(RequestContext request) {
        return ProviderHelper.notallowed(request, ALLOWED_ENTRY_METHODS);
    }
  
    @Override
    public ResponseContext putMedia(RequestContext request) {
        return ProviderHelper.notallowed(request, ALLOWED_ENTRY_METHODS);
    }
  
    public ResponseContext getFeed(RequestContext request) {
        ExpandedItemTarget target = (ExpandedItemTarget) request.getTarget();
        NoteItem item = target.getItem();
        if (log.isDebugEnabled())
            log.debug("getting expanded feed for item " + item.getUid());

        try {
            ServiceLocator locator = createServiceLocator(request);
            ItemFeedGenerator generator =
                createItemFeedGenerator(target, locator);
            generator.setFilter(createQueryFilter(request));

            Feed feed = generator.generateFeed(item);

            return ok(request, feed, item);
        } catch (InvalidQueryException e) {
            return ProviderHelper.badrequest(request, e.getMessage());
        } catch (UnsupportedProjectionException e) {
            String reason = "Projection " + target.getProjection() + " not supported";
            return ProviderHelper.badrequest(request, reason);
        } catch (UnsupportedFormatException e) {
            String reason = "Format " + target.getFormat() + " not supported";
            return ProviderHelper.badrequest(request, reason);
        } catch (GeneratorException e) {
            String reason = "Unknown feed generation error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        }
    }

    public ResponseContext getEntry(RequestContext request) {
        return ProviderHelper.notallowed(request, ALLOWED_ENTRY_METHODS);
    }
  
    public ResponseContext getMedia(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext getCategories(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext entryPost(RequestContext request) {
        return ProviderHelper.notallowed(request, ALLOWED_ENTRY_METHODS);
    }
  
    public ResponseContext mediaPost(RequestContext request) {
        return ProviderHelper.notallowed(request, ALLOWED_ENTRY_METHODS);
    }

    // ExtendedCollectionAdapter methods
    @Override
    public ResponseContext postCollection(RequestContext request) {
        return ProviderHelper.notallowed(request, ALLOWED_COLL_METHODS);
    }
}
