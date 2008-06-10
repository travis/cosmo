package org.osaf.cosmo.atom.provider;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Service;
import org.apache.abdera.protocol.Resolver;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.TargetBuilder;
import org.apache.abdera.protocol.server.WorkspaceManager;
import org.apache.abdera.protocol.server.context.AbstractResponseContext;
import org.apache.abdera.protocol.server.context.BaseResponseContext;
import org.apache.abdera.protocol.server.impl.AbstractProvider;
import org.apache.abdera.protocol.server.servlet.ServletRequestContext;
import org.apache.abdera.util.EntityTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.generator.GeneratorException;
import org.osaf.cosmo.atom.generator.GeneratorFactory;
import org.osaf.cosmo.atom.generator.ServiceGenerator;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.server.ServiceLocatorFactory;

public class StandardProvider extends AbstractProvider {

    private WorkspaceManager workspaceManager;
    private Resolver<Target> resolver;
    private ServiceLocatorFactory serviceLocatorFactory;
    private GeneratorFactory generatorFactory;
   
    private static final Log log = LogFactory.getLog(StandardProvider.class);
    
    @Override
    public ResponseContext process(RequestContext request) {
        ResponseContext response = preconditions(request);
        if(response!=null)
            return response;
        
        return super.process(request);
    }
    
    protected ServiceLocator createServiceLocator(RequestContext context) {
        HttpServletRequest request =
            ((ServletRequestContext)context).getRequest();
        return serviceLocatorFactory.createServiceLocator(request);
    }

    @Override
    protected TargetBuilder getTargetBuilder(RequestContext arg0) {
        return null;
    }

    @Override
    protected Resolver<Target> getTargetResolver(RequestContext arg0) {
        return resolver;
    }

    @Override
    protected WorkspaceManager getWorkspaceManager(RequestContext arg0) {
        return workspaceManager;
    }

    public void setWorkspaceManager(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    public void setTargetResolver(Resolver<Target> resolver) {
        this.resolver = resolver;
    }

    public void setAbdera(Abdera abdera) {
        this.abdera = abdera;
    }
    
    @Override
    public ResponseContext getServiceDocument(RequestContext request) {
        // TODO handle other types of services somehow
        UserTarget target = (UserTarget) request.getTarget();
        User user = target.getUser();
        if (log.isDebugEnabled())
            log.debug("getting service for user " + user.getUsername());

        try {
            ServiceLocator locator = createServiceLocator(request);
            ServiceGenerator generator = createServiceGenerator(locator);
            Service service =
                generator.generateService(target.getUser());

            return createResponseContext(request, service.getDocument());
        } catch (GeneratorException e) {
            String reason = "Unknown service generation error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        }
    }
    
    protected ServiceGenerator createServiceGenerator(ServiceLocator locator) {
        return getGeneratorFactory().createServiceGenerator(locator);
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
    
    /**
     * Extends the superclass method to implement conditional request
     * methods by honoring conditional method headers for
     * <code>AuditableTarget</code>s.
     */
    protected ResponseContext preconditions(RequestContext request) {
        
        if (! (request.getTarget() instanceof AuditableTarget))
            return null;

        AuditableTarget target = (AuditableTarget) request.getTarget();

        ResponseContext response = null;
        
        response = ifMatch(request.getIfMatch(), target, request);
        if(response != null)
            return response;
       
        response = ifNoneMatch(request.getIfNoneMatch(), target, request);
        if(response != null)
            return response;
       
        response = ifModifiedSince(request.getIfModifiedSince(), target, request);
        if(response != null)
            return response;
       
        response = ifUnmodifiedSince(request.getIfUnmodifiedSince(), target, request);
        if(response != null)
            return response;

        return null;
    }

    private ResponseContext ifMatch(EntityTag[] etags,
                            AuditableTarget target,
                            RequestContext request) {
        if (etags.length == 0)
            return null;

        if (EntityTag.matchesAny(target.getEntityTag(), etags))
            return null;
        
        ResponseContext rc = ProviderHelper.preconditionfailed(request, "If-Match disallows conditional request");
        rc.setEntityTag(target.getEntityTag());
        return rc;
    }

    private ResponseContext ifNoneMatch(EntityTag[] etags,
                                AuditableTarget target,
                                RequestContext request) {
        if (etags.length == 0)
            return null;

        if (! EntityTag.matchesAny(target.getEntityTag(), etags))
            return null;

        ResponseContext rc;
        
        if (deservesNotModified(request))
            rc = ProviderHelper.notmodified(request, "Not Modified");
        else
            rc = ProviderHelper.preconditionfailed(request, "If-None-Match disallows conditional request");
    
        rc.setEntityTag(target.getEntityTag());
        return rc;
    }

    private ResponseContext ifModifiedSince(Date date,
                                    AuditableTarget target,
                                    RequestContext request) {
        if (date == null)
            return null;
        if (target.getLastModified().after(date))
            return null;
        
        return ProviderHelper.notmodified(request, "Not Modified");
    }

    private ResponseContext ifUnmodifiedSince(Date date,
                                      AuditableTarget target,
                                      RequestContext request) {
        if (date == null)
            return null;
        if (target.getLastModified().before(date))
            return null;
        
        return ProviderHelper.preconditionfailed(request, "If-Unmodified-Since disallows conditional request");
    }

    private boolean deservesNotModified(RequestContext request) {
        return (request.getMethod().equals("GET") ||
                request.getMethod().equals("HEAD"));
    }
    
    protected AbstractResponseContext createResponseContext(
            RequestContext context, Document<Element> doc) {
        return createResponseContext(context, doc, -1, null);
    }

    protected AbstractResponseContext createResponseContext(
            RequestContext context, Document<Element> doc, int status,
            String reason) {
        AbstractResponseContext rc = new BaseResponseContext<Document<Element>>(
                doc);

        rc.setWriter(context.getAbdera().getWriterFactory().getWriter(
                "PrettyXML"));

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

}
