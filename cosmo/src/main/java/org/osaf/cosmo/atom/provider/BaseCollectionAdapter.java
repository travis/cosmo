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
import java.text.ParseException;
import java.util.Date;

import javax.activation.MimeType;
import javax.servlet.http.HttpServletRequest;

import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Base;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.context.AbstractResponseContext;
import org.apache.abdera.protocol.server.context.BaseResponseContext;
import org.apache.abdera.protocol.server.context.EmptyResponseContext;
import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.apache.abdera.protocol.server.impl.AbstractCollectionAdapter;
import org.apache.abdera.protocol.server.servlet.ServletRequestContext;
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

public abstract class BaseCollectionAdapter extends AbstractCollectionAdapter
    implements ExtendedCollectionAdapter, AtomConstants, ServerConstants {
    private static final Log log = LogFactory.getLog(BaseCollectionAdapter.class);
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    private GeneratorFactory generatorFactory;
    private ServiceLocatorFactory serviceLocatorFactory;
    private EntityFactory entityFactory;
   
    // AbstractProvider methods
    
    protected int getDefaultPageSize() {
        // XXX
        return 25;
    }
    
    protected ServiceLocator createServiceLocator(RequestContext context) {
        HttpServletRequest request =
            ((ServletRequestContext)context).getRequest();
        return serviceLocatorFactory.createServiceLocator(request);
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
    
    @Override
    public void compensate(RequestContext request, Throwable t) {
        // keep track of unexpected exception by storing in the request
        ((ServletRequestContext) request).getRequest().setAttribute(
                ServerConstants.ATTR_SERVICE_EXCEPTION, t);
    }
    
    @Override
    public String getAuthor(RequestContext context)
            throws ResponseContextException {
        // required by AbstractCollectionAdapter but not used
        return null;
    }

    @Override
    public String getId(RequestContext context) {
        // required by AbstractCollectionAdapter but not used
        return null;
    }
    
    public String getTitle(RequestContext arg0) {
        // required by CollectionInfo but not used
        return null;
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
        if (generatorFactory == null)
            throw new IllegalStateException("generatorFactory is required");
        if (serviceLocatorFactory == null)
            throw new IllegalStateException("serviceLocatorFactory is required");
        if (entityFactory == null)
            throw new IllegalStateException("entityFactory is required");
    }

    protected ResponseContext ok(RequestContext context, Feed feed) {
        return ok(context, feed, null);
    }

    protected ResponseContext ok(RequestContext context,
                                 Feed feed,
                                 AuditableObject auditable) {
        AbstractResponseContext rc =
            createResponseContext(context, feed.getDocument());

        if (auditable != null) {
            rc.setEntityTag(new EntityTag(auditable.getEntityTag()));
            rc.setLastModified(auditable.getModifiedDate());
        }

        return rc;
    }

    protected ResponseContext ok(RequestContext context,
                                 Entry entry,
                                 AuditableObject auditable) {
         AbstractResponseContext rc =
             createResponseContext(context, entry.getDocument());

         if (auditable != null) {
             rc.setEntityTag(new EntityTag(auditable.getEntityTag()));
             rc.setLastModified(auditable.getModifiedDate());
         }

         return rc;
    }

    protected ResponseContext created(RequestContext context,
                                      Entry entry,
                                      AuditableObject auditable,
                                      ServiceLocator locator) {
        AbstractResponseContext rc =
            createResponseContext(context, entry.getDocument(), 201, "Created");

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

    protected ResponseContext updated(RequestContext context,
                                      Entry entry,
                                      AuditableObject auditable,
                                      ServiceLocator locator,
                                      boolean locationChanged) {
        AbstractResponseContext rc =
            createResponseContext(context, entry.getDocument());

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
    
    protected ResponseContext conflict(RequestContext request, AtomException e) {
        return returnBase(e.createDocument(request.getAbdera()), e.getCode(), null, request.getAbdera());
    }
    
    protected ResponseContext lengthrequired(RequestContext request) {
        return ProviderHelper.createErrorResponse(request.getAbdera(), 411, "Length Required",null);
    }

    protected ResponseContext locked(RequestContext request) {
        return ProviderHelper.createErrorResponse(request.getAbdera(), 423, "Collection Locked",null);
    }
    
    protected ResponseContext insufficientPrivileges(RequestContext request, AtomException e) {
        return returnBase(e.createDocument(request.getAbdera()), e.getCode(), null, request.getAbdera());
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
        createResponseContext(RequestContext context, Document<Element> doc) {
        return createResponseContext(context, doc, -1, null);
    }

    protected AbstractResponseContext
        createResponseContext(RequestContext context,
                              Document<Element> doc,
                              int status,
                              String reason) {
        AbstractResponseContext rc =
            new BaseResponseContext<Document<Element>>(doc);

        rc.setWriter(context.getAbdera().getWriterFactory().getWriter("PrettyXML"));

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
                                         Date lastModified,
                                         Abdera abdera) {
        ResponseContext rc = ProviderHelper.returnBase(base, status, lastModified);
        rc.setWriter(abdera.getWriterFactory().getWriter("PrettyXML"));
        return rc;
    }
    
    protected ResponseContext checkCollectionWritePreconditions(
            RequestContext request) {
        int contentLength = Integer.valueOf(request.getProperty(
                RequestContext.Property.CONTENTLENGTH).toString());
        if (contentLength <= 0)
            return lengthrequired(request);

        MimeType ct = request.getContentType();
        if (ct == null
                || !MimeTypeHelper.isMatch(MEDIA_TYPE_XHTML, ct.toString()))
            return ProviderHelper.notsupported(request, "Content-Type must be "
                    + MEDIA_TYPE_XHTML);

        return null;
    }
    
    protected ResponseContext checkEntryWritePreconditions(RequestContext request) {
        return checkEntryWritePreconditions(request, true);
    }

    protected ResponseContext checkEntryWritePreconditions(RequestContext request,
                                                           boolean requireAtomContent) {
        int contentLength = Integer.valueOf(request.getProperty(RequestContext.Property.CONTENTLENGTH).toString());
        if (contentLength <= 0)
            return lengthrequired(request);

        MimeType ct = request.getContentType();
        if (ct == null)
            return ProviderHelper.badrequest(request, "Content-Type required");

        if (! requireAtomContent)
            return null;

        if (! MimeTypeHelper.isMatch(Constants.ATOM_MEDIA_TYPE, ct.toString()))
            return ProviderHelper.notsupported(request, "Content-Type must be " + Constants.ATOM_MEDIA_TYPE);

        try {
            if (! (request.getDocument().getRoot() instanceof Entry))
                return ProviderHelper.badrequest(request, "Entity-body must be an Atom entry");
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e); 
        }

        return null;
    }
    
    protected ResponseContext checkMediaWritePreconditions(RequestContext request) {
        int contentLength = Integer.valueOf(request.getProperty(RequestContext.Property.CONTENTLENGTH).toString());
        if (contentLength <= 0)
            return lengthrequired(request);

        return null;
    }
    
}
