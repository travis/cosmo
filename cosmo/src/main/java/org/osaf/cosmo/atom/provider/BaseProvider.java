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

import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import javax.activation.MimeType;
import javax.servlet.http.HttpServletRequest;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Base;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.AbstractProvider;
import org.apache.abdera.protocol.server.impl.AbstractResponseContext;
import org.apache.abdera.protocol.server.impl.BaseResponseContext;
import org.apache.abdera.protocol.server.impl.EmptyResponseContext;
import org.apache.abdera.protocol.server.impl.HttpServletRequestContext;
import org.apache.abdera.util.Constants;
import org.apache.abdera.util.EntityTag;
import org.apache.abdera.util.MimeTypeHelper;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.atom.AtomException;
import org.osaf.cosmo.atom.generator.GeneratorFactory;
import org.osaf.cosmo.model.AuditableObject;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.server.ServerConstants;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.server.ServiceLocatorFactory;
import org.osaf.cosmo.util.DateUtil;

public abstract class BaseProvider extends AbstractProvider
    implements ExtendedProvider, AtomConstants, ServerConstants {
    private static final Log log = LogFactory.getLog(BaseProvider.class);
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    private Abdera abdera;
    private GeneratorFactory generatorFactory;
    private ServiceLocatorFactory serviceLocatorFactory;
    private EntityFactory entityFactory;

    // AbstractProvider methods

    protected int getDefaultPageSize() {
        // XXX
        return 25;
    }
    
    /**
     * <p>
     * Extends the superclass method to implement the following APP
     * extensions:
     * </p>
     * <ul>
     * <li> When <code>PUT</code>ing to a collection, the request is
     * interpreted as an update of the collection itself.
     * </ul>
     */
    public ResponseContext request(RequestContext request) {
        String method = request.getMethod();
        TargetType type = request.getTarget().getType();

        if (log.isDebugEnabled())
            log.debug("servicing request for method " + method + " against target type " + type.name());

        try {
            if (method.equals("POST")) {
                if (type == TargetType.TYPE_SERVICE)
                    return createCollection(request);
            } else if (method.equals("PUT")) {
                if (type == TargetType.TYPE_COLLECTION)
                    return updateCollection(request);
            } else if (method.equals("DELETE")) {
                if (type == TargetType.TYPE_COLLECTION)
                    return deleteCollection(request);
            }

            return super.request(request);
        } catch (RuntimeException e) {
            // the request handler swallows the exception when it sends its
            // 500 response, so stuff the exception into a request attribute
            // to let the servlet and filters examine the exception if they
            // wish
            request.setAttribute(RequestContext.Scope.REQUEST,
                                 ATTR_SERVICE_EXCEPTION, e);
            throw e;
        }
    }

    public String[] getAllowedMethods(TargetType type) {
        if (type != null && type == TargetType.TYPE_COLLECTION)
            return new String[] {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS" };
        if (type != null && type == TargetType.TYPE_SERVICE)
            return new String[] { "GET", "POST", "HEAD", "OPTIONS" };
        return super.getAllowedMethods(type);
    }

    // our methods

    /**
     * Returns the value for a request parameter. If the parameter's
     * value is empty, returns null.
     */
    protected String getNonEmptyParameter(RequestContext request,
                                          String name) {
        String value = request.getParameter(name);
        return ! StringUtils.isBlank(value) ? value : null;
    }

    /**
     * Returns the value for a request parameter as a
     * <code>Date</code>. The value must be specified as an RFC 3339
     * datetime.
     *
     * @throws ParseException if the value cannot be parsed
     */
    protected Date getDateParameter(RequestContext request,
                                    String name)
        throws ParseException {
        String value = getNonEmptyParameter(request, name);
        if (value == null)
            return null;
        return DateUtil.parseRfc3339Calendar(value).getTime();
    }

    /**
     * Returns the value for a request parameter as a
     * <code>TimeZone</code>. The value must be specified as the
     * identifier string for a time zone registered in the server's
     * <code>TimeZoneRegistry</code>.
     *
     * @throws IllegalArgumentException if the specified time zone is
     * not registered
     */
    protected TimeZone getTimeZoneParameter(RequestContext request,
                                            String name) {
        String value = getNonEmptyParameter(request, name);
        if (value == null)
            return null;
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone(value);
        if (tz == null)
            throw new IllegalArgumentException("Unregistered timezone " + value);
        return tz;
    }

    public Abdera getAbdera() {
        return abdera;
    }

    public void setAbdera(Abdera abdera) {
        this.abdera = abdera;
    }

    public GeneratorFactory getGeneratorFactory() {
        return generatorFactory;
    }

    public void setGeneratorFactory(GeneratorFactory factory) {
        generatorFactory = factory;
    }

    public ServiceLocatorFactory getServiceLocatorFactory() {
        return serviceLocatorFactory;
    }

    public void setServiceLocatorFactory(ServiceLocatorFactory factory) {
        serviceLocatorFactory = factory;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }


    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }


    public void init() {
        if (abdera == null)
            throw new IllegalStateException("abdera is required");
        if (generatorFactory == null)
            throw new IllegalStateException("generatorFactory is required");
        if (serviceLocatorFactory == null)
            throw new IllegalStateException("serviceLocatorFactory is required");
        if (entityFactory == null)
            throw new IllegalStateException("entityFactory is required");
    }

    protected ResponseContext
        checkCollectionWritePreconditions(RequestContext request) {
        int contentLength = Integer.valueOf(request.getProperty(RequestContext.Property.CONTENTLENGTH).toString());
        if (contentLength <= 0)
            return lengthrequired(getAbdera(), request);

        MimeType ct = request.getContentType();
        if (ct == null ||
            ! MimeTypeHelper.isMatch(MEDIA_TYPE_XHTML, ct.toString()))
            return notsupported(getAbdera(), request, "Content-Type must be " + MEDIA_TYPE_XHTML);

        return null;
    }

    protected ResponseContext checkEntryWritePreconditions(RequestContext request) {
        return checkEntryWritePreconditions(request, true);
    }

    protected ResponseContext checkEntryWritePreconditions(RequestContext request,
                                                           boolean requireAtomContent) {
        int contentLength = Integer.valueOf(request.getProperty(RequestContext.Property.CONTENTLENGTH).toString());
        if (contentLength <= 0)
            return lengthrequired(getAbdera(), request);

        MimeType ct = request.getContentType();
        if (ct == null)
            return badrequest(getAbdera(), request, "Content-Type required");

        if (! requireAtomContent)
            return null;

        if (! MimeTypeHelper.isMatch(Constants.ATOM_MEDIA_TYPE, ct.toString()))
            return notsupported(getAbdera(), request, "Content-Type must be " + Constants.ATOM_MEDIA_TYPE);

        try {
            if (! (request.getDocument().getRoot() instanceof Entry))
                return badrequest(getAbdera(), request, "Entity-body must be an Atom entry");
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e); 
        }

        return null;
    }

    protected ResponseContext checkMediaWritePreconditions(RequestContext request) {
        int contentLength = Integer.valueOf(request.getProperty(RequestContext.Property.CONTENTLENGTH).toString());
        if (contentLength <= 0)
            return lengthrequired(getAbdera(), request);

        return null;
    }

    protected ServiceLocator createServiceLocator(RequestContext context) {
        HttpServletRequest request =
            ((HttpServletRequestContext)context).getRequest();
        return serviceLocatorFactory.createServiceLocator(request);
    }

    protected ResponseContext ok(Feed feed) {
        return ok(feed, null);
    }

    protected ResponseContext ok(Feed feed,
                                 AuditableObject auditable) {
        AbstractResponseContext rc =
            createResponseContext(feed.getDocument());

        if (auditable != null) {
            rc.setEntityTag(new EntityTag(auditable.getEntityTag()));
            rc.setLastModified(auditable.getModifiedDate());
        }

        return rc;
    }

    protected ResponseContext ok(Entry entry,
                                 AuditableObject auditable) {
         AbstractResponseContext rc =
             createResponseContext(entry.getDocument());

         if (auditable != null) {
             rc.setEntityTag(new EntityTag(auditable.getEntityTag()));
             rc.setLastModified(auditable.getModifiedDate());
         }

         return rc;
    }

    protected ResponseContext created(Entry entry,
                                      AuditableObject auditable,
                                      ServiceLocator locator) {
        AbstractResponseContext rc =
            createResponseContext(entry.getDocument(), 201, "Created");

        if (auditable != null) {
            rc.setEntityTag(new EntityTag(auditable.getEntityTag()));
            rc.setLastModified(auditable.getModifiedDate());
        }

        try {
            String location = locator.getAtomBase() +
                entry.getSelfLink().getHref().toString();
            rc.setLocation(location);
            rc.setContentLocation(location);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing self link href", e);
        }

        return rc;
    }

    protected ResponseContext updated(AuditableObject auditable) {
        AbstractResponseContext rc = createResponseContext(204);

        if (auditable != null) {
            rc.setEntityTag(new EntityTag(auditable.getEntityTag()));
            rc.setLastModified(auditable.getModifiedDate());
        }

        return rc;
    }

    protected ResponseContext updated(Entry entry,
                                      AuditableObject auditable,
                                      ServiceLocator locator,
                                      boolean locationChanged) {
        AbstractResponseContext rc =
            createResponseContext(entry.getDocument());

        if (auditable != null) {
            rc.setEntityTag(new EntityTag(auditable.getEntityTag()));
            rc.setLastModified(auditable.getModifiedDate());
        }

        if (locationChanged) {
            try {
                String location = locator.getAtomBase() +
                    entry.getSelfLink().getHref().toString();
                rc.setLocation(location);
                rc.setContentLocation(location);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing self link href", e);
            }
        }

        return rc;
    }

    protected ResponseContext deleted() {
        return createResponseContext(204);
    }

    protected ResponseContext conflict(RequestContext request,
                                       AtomException e) {
        return returnBase(e.createDocument(abdera), e.getCode(), null);
    }

    protected ResponseContext preconditionfailed(Abdera abdera,
                                                 RequestContext request,
                                                 String reason) {
        return returnBase(createErrorDocument(abdera, 412, reason, null),
                          412, null);
    }

    protected ResponseContext lengthrequired(Abdera abdera,
                                             RequestContext request) {
        return returnBase(createErrorDocument(abdera, 411, "Length Required",
                                              null),
                          411, null);
    }

    protected ResponseContext locked(Abdera abdera,
                                     RequestContext request) {
        return returnBase(createErrorDocument(abdera, 423, "Collection Locked",
                                              null),
                          423, null);
    }

    protected ResponseContext methodnotallowed(Abdera abdera, 
                                               RequestContext request,
                                               String[] methods) {
        BaseResponseContext resp = (BaseResponseContext)
            returnBase(createErrorDocument(abdera, 405, "Method Not Allowed",
                                           null),
                       405, null);
        resp.setAllow(methods);
        return resp;
    }
  
    protected AbstractResponseContext
        createResponseContext(int status) {
        return createResponseContext(status, null);
    }

    protected AbstractResponseContext
        createResponseContext(int status,
                              String reason) {
        AbstractResponseContext rc = new EmptyResponseContext(status);

        if (reason != null)
            rc.setStatusText(reason);

        return rc;
    }

    protected AbstractResponseContext
        createResponseContext(Document<Element> doc) {
        return createResponseContext(doc, -1, null);
    }

    protected AbstractResponseContext
        createResponseContext(Document<Element> doc,
                              int status,
                              String reason) {
        AbstractResponseContext rc =
            new BaseResponseContext<Document<Element>>(doc);

        rc.setWriter(abdera.getWriterFactory().getWriter("PrettyXML"));

        if (status > 0)
            rc.setStatus(status);
        if (reason != null)
            rc.setStatusText(reason);
        
        // Cosmo data is sufficiently dynamic that clients
        // should always revalidate with the server rather than caching.
        rc.setMaxAge(0);
        rc.setMustRevalidate(true);
        rc.setExpires(new java.util.Date());
        
        return rc;
    }

    protected ResponseContext returnBase(Base base, 
                                         int status,
                                         Date lastModified) {
        ResponseContext rc = super.returnBase(base, status, lastModified);
        rc.setWriter(abdera.getWriterFactory().getWriter("PrettyXML"));
        return rc;
    }
}
