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

import org.apache.abdera.i18n.iri.IRISyntaxException;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Service;
import org.apache.abdera.model.Workspace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * Standard implementation of {@link ServiceGenerator}.
 *
 * @see Service
 * @see CollectionItem
 * @see User
 */
public class StandardServiceGenerator
    implements ServiceGenerator, AtomConstants{
    private static final Log log =
        LogFactory.getLog(StandardServiceGenerator.class);

    private StandardGeneratorFactory factory;
    private ServiceLocator serviceLocator;

    public StandardServiceGenerator(StandardGeneratorFactory factory,
                                    ServiceLocator serviceLocator) {
        this.factory = factory;
        this.serviceLocator = serviceLocator;
    }

    /**
     * <p>
     * Generates a service containing the following workspaces
     * describing the collections accessible for a user:
     * </p>
     * <dl>
     * <dt>{@link ServiceGenerator#WORKSPACE_HOME}</dt>
     * <dd>Contains an Atom collection for each of the collections
     * published in a user's home collection (not including any
     * sub-collections).</dd>
     * <dt>{@link ServiceGenerator#WORKSPACE_ACCOUNT}</dt>
     * <dd>Contains an Atom collection for each of the local
     * collections (those on the same server) that the user to which
     * the user is subscriptions.
     * </dl>
     *
     * @param the user
     * @param home the user's home collection
     * @throws GeneratorException
     */
    public Service generateService(User user)
        throws GeneratorException {
        Service service = createService(user);

        Workspace hw = createHomeWorkspace();
        service.addWorkspace(hw);

        HomeCollectionItem home =
            factory.getContentService().getRootItem(user);
        for (Item child : home.getChildren()) {
            if (child instanceof CollectionItem)
                hw.addCollection(createCollection((CollectionItem)child));
        }

        Workspace mw = createAccountWorkspace();
        service.addWorkspace(mw);

        mw.addCollection(createSubscriptionsCollection(user));
        mw.addCollection(createPreferencesCollection(user));

        return service;
    }

    /**
     * Creates a <code>Service</code> representing the given User.
     *
     * @param user the user whose content is described by the service
     * @throws GeneratorException
     */
    protected Service createService(User user)
        throws GeneratorException {
        Service service = factory.getAbdera().getFactory().newService();

        String baseUri = serviceLocator.getAtomBase();
        try {
            service.setBaseUri(baseUri);
        } catch (IRISyntaxException e) {
            throw new GeneratorException("Attempted to set base URI " + baseUri, e);
        }

        return service;
    }

    /**
     * Creates a <code>Workspace</code> representing the service
     * user's home collection.
     *
     * @param home the home collection whose content is described by
     * the workspace
     * @throws GeneratorException
     */
    protected Workspace createHomeWorkspace()
        throws GeneratorException {
        Workspace workspace = factory.getAbdera().getFactory().newWorkspace();
        workspace.setTitle(WORKSPACE_HOME);
        return workspace;
    }

    /**
     * Creates a <code>Workspace</code> representing the service
     * user's subscribed collections.
     *
     * @throws GeneratorException
     */
    protected Workspace createAccountWorkspace()
        throws GeneratorException {
        Workspace workspace = factory.getAbdera().getFactory().newWorkspace();
        workspace.setTitle(WORKSPACE_ACCOUNT);
        return workspace;
    }

    /**
     * Creates a <code>Collection</code> based on a collection item.
     *
     * @param collection the collection item described by the atom
     * collection
     * @throws GeneratorException
     */
    protected Collection createCollection(CollectionItem item)
        throws GeneratorException {
        Collection collection =
            factory.getAbdera().getFactory().newCollection();
        String href = serviceLocator.getAtomUrl(item, false);

        try {
            collection.setAccept("entry");
            collection.setHref(href);
            collection.setTitle(item.getDisplayName());
        } catch (IRISyntaxException e) {
            throw new GeneratorException("Attempted to set invalid collection href " + href, e);
        }

        return collection;
    }

    /**
     * Creates the <code>subscriptions Collection</code> describing a
     * user's collection subscriptions.
     *
     * @param user the user
     * @throws GeneratorException
     */
    protected Collection createSubscriptionsCollection(User user)
        throws GeneratorException {
        Collection collection =
            factory.getAbdera().getFactory().newCollection();
        String href = serviceLocator.getAtomUrl(user, false) + "/subscriptions";

        try {
            collection.setAccept("entry");
            collection.setHref(href);
            collection.setTitle(COLLECTION_SUBSCRIPTIONS);
        } catch (IRISyntaxException e) {
            throw new GeneratorException("Attempted to set invalid collection href " + href, e);
        }

        return collection;
    }

    /**
     * Creates the <code>preferences Collection</code> describing a
     * user's preferences.
     *
     * @param  user the user
     * @throws GeneratorException
     */
    protected Collection createPreferencesCollection(User user)
        throws GeneratorException {
        Collection collection =
            factory.getAbdera().getFactory().newCollection();
        String href = serviceLocator.getAtomUrl(user, false) + "/preferences";

        try {
            collection.setAccept("entry");
            collection.setHref(href);
            collection.setTitle(COLLECTION_PREFERENCES);
        } catch (IRISyntaxException e) {
            throw new GeneratorException("Attempted to set invalid collection href " + href, e);
        }

        return collection;
    }

    public StandardGeneratorFactory getFactory() {
        return factory;
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }
}
