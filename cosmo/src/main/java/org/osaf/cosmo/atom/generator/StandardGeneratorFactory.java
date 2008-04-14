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
package org.osaf.cosmo.atom.generator;

import org.apache.abdera.Abdera;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.service.ContentService;

/**
 * Standard implementation of <code>GeneratorFactory</code>.
 *
 * @see GeneratorFactory
 * @see FeedGenerator
 */
public class StandardGeneratorFactory
    implements GeneratorFactory, AtomConstants {
    private static final Log log =
        LogFactory.getLog(StandardGeneratorFactory.class);

    private Abdera abdera;
    private ContentFactory contentFactory;
    private ContentService contentService;
    private CosmoSecurityManager securityManager;

    // GeneratorFactory methods

    /**
     * Creates an instance of <code>ServiceGenerator</code>.
     *
     * @param locator the service locator from which collection
     * URLs are calculated
     * @return the service generator
     */
    public ServiceGenerator
        createServiceGenerator(ServiceLocator locator) {
        return new StandardServiceGenerator(this, locator);
    }

    /**
     * Creates an instance of <code>ItemFeedGenerator</code> that can
     * service the given projection and format.
     * <p>
     * The following feed generators are supported:
     * <dl>
     * <dt>{@link AtomConstants#PROJECTION_BASIC}</dt>
     * <dd>{@link BasicFeedGenerator}</dd>
     * <dt>{@link AtomConstants#PROJECTION_FULL}</dt>
     * <dd>{@link FullFeedGenerator}</dd>
     * <dd>{@link AtomConstants#PROJECTION_DASHBOARD}</dt>
     * <dd>{@link DashboardFeedGenerator}</dd>
     * <dt>{@link AtomConstants#PROJECTION_DASHBOARD_NOW}</dt>
     * <dd>{@link DashboardFeedGenerator}</dd>
     * <dt>{@link AtomConstants#PROJECTION_DASHBOARD_LATER}</dt>
     * <dd>{@link DashboardFeedGenerator}</dd>
     * <dt>{@link AtomConstants#PROJECTION_DASHBOARD_DONE}</dt>
     * <dd>{@link DashboardFeedGenerator}</dd>
     * <dt>{@link AtomConstants#PROJECTION_DETAILS}</dt>
     * <dd>{@link DetailsFeedGenerator}</dd>
     * </dl>
     * <p>
     * If no projection is specified, the basic feed generator is
     * returned.
     * <p>
     * The following data formats are supported:
     * <dl>
     * <dt>{@link AtomConstants#FORMAT_EIM_JSON}</dt>
     * <dd>EIM over JSON</dd>
     * <dt>{@link AtomConstants#FORMAT_EIMML}</dt>
     * <dd>EIMML</dd>
     * </dl>
     * <p>
     * The basic and details feed generators ignore the format
     * argument. For all other generators, if no format is provided,
     * EIM over JSON is used.
     *
     * @param projection the projection name
     * @param format the format name
     * @param locator the service locator from which feed URLs
     * are calculated
     * @return the feed generator, or null if no generator is
     * supported for the named projection
     */
    public ItemFeedGenerator createItemFeedGenerator(String projection,
                                                     String format,
                                                     ServiceLocator locator)
        throws UnsupportedProjectionException, UnsupportedFormatException {
        if (projection == null ||
            projection.equals(PROJECTION_BASIC))
            return new BasicFeedGenerator(this, locator);
        if (projection.equals(PROJECTION_FULL))
            return new FullFeedGenerator(this, locator, format);
        if (projection.equals(PROJECTION_DETAILS))
            return new DetailsFeedGenerator(this, locator);
        if (projection.equals(PROJECTION_DASHBOARD))
            return new DashboardFeedGenerator(this, locator, format);
        if (projection.equals(PROJECTION_DASHBOARD_NOW))
            return new DashboardFeedGenerator(this, locator, format,
                                              TriageStatus.CODE_NOW);
        if (projection.equals(PROJECTION_DASHBOARD_LATER))
            return new DashboardFeedGenerator(this, locator, format,
                                              TriageStatus.CODE_LATER);
        if (projection.equals(PROJECTION_DASHBOARD_DONE))
            return new DashboardFeedGenerator(this, locator, format,
                                              TriageStatus.CODE_DONE);
        throw new UnsupportedProjectionException(projection);
    }

    /**
     * Creates an instance of
     * <code>StandardSubscriptionFeedGenerator</code>.
     *
     * @param locator the service locator from which feed URLs
     * are calculated
     * @return the feed generator
     */
    public SubscriptionFeedGenerator
        createSubscriptionFeedGenerator(ServiceLocator locator) {
        return new StandardSubscriptionFeedGenerator(this, locator);
    }

    /**
     * Creates an instance of
     * <code>StandardPreferencesFeedGenerator</code>.
     *
     * @param locator the service locator from which feed URLs
     * are calculated
     * @return the feed generator
     */
    public PreferencesFeedGenerator
        createPreferencesFeedGenerator(ServiceLocator locator) {
        return new StandardPreferencesFeedGenerator(this, locator);
    }

    /**
     * Creates an instance of
     * <code>StandardTicketsFeedGenerator</code>.
     *
     * @param locator the service locator from which feed URLs
     * are calculated
     * @return the feed generator
     */
    public TicketsFeedGenerator
        createTicketsFeedGenerator(ServiceLocator locator) {
        return new StandardTicketsFeedGenerator(this, locator);
    }

    // our methods

    public Abdera getAbdera() {
        return abdera;
    }

    public void setAbdera(Abdera abdera) {
        this.abdera = abdera;
    }

    public ContentFactory getContentFactory() {
        return contentFactory;
    }

    public void setContentFactory(ContentFactory factory) {
        contentFactory = factory;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService service) {
        contentService = service;
    }

    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    public void setSecurityManager(CosmoSecurityManager manager) {
        this.securityManager = manager;
    }

    public void init() {
        if (abdera == null)
            throw new IllegalStateException("abdera is required");
        if (contentFactory == null)
            throw new IllegalStateException("contentFactory is required");
        if (contentService == null)
            throw new IllegalStateException("contentService is required");
        if (securityManager == null)
            throw new IllegalStateException("securityManager is required");
    }
}
