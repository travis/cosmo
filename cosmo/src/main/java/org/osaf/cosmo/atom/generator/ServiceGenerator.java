/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Service;
import org.apache.abdera.model.Workspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * Constructs an Atom introspection document describing the
 * collections owned by a user.
 *
 * @see Service
 * @see CollectionItem
 * @see User
 */
public class ServiceGenerator {
    private static final Log log = LogFactory.getLog(ServiceGenerator.class);

    private static final String WORKSPACE_HOME = "home";

    private Factory factory;
    private ServiceLocator serviceLocator;

    public ServiceGenerator(Factory factory,
                            ServiceLocator serviceLocator) {
        this.factory = factory;
        this.serviceLocator = serviceLocator;
    }

    /**
     * Generates a Service with a single workspace containing a
     * collection for each of the collections in a home collection
     * (not including any subcollections).
     *
     * @param collection the collection on which the feed is based
     * @throws GeneratorException
     */
    public Service generateService(HomeCollectionItem home)
        throws GeneratorException {
        Service service = createService(home.getOwner());

        Workspace workspace = createHomeWorkspace(home);
        service.addWorkspace(workspace);

        for (Item child : home.getChildren()) {
            if (child instanceof CollectionItem)
                workspace.
                    addCollection(createCollection((CollectionItem)child));
        }

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
        Service service = factory.newService();

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
    protected Workspace createHomeWorkspace(HomeCollectionItem home)
        throws GeneratorException {
        Workspace workspace = factory.newWorkspace();
        workspace.setTitle(WORKSPACE_HOME);
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
        Collection collection = factory.newCollection();
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

    public Factory getFactory() {
        return factory;
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }
}
