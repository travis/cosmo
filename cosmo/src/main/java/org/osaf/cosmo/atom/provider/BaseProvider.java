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

import java.text.ParseException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.protocol.server.provider.AbstractProvider;
import org.apache.abdera.protocol.server.provider.AbstractResponseContext;
import org.apache.abdera.protocol.server.provider.BaseResponseContext;
import org.apache.abdera.protocol.server.provider.EmptyResponseContext;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;
import org.apache.abdera.protocol.server.servlet.HttpServletRequestContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.generator.GeneratorFactory;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.server.ServiceLocatorFactory;
import org.osaf.cosmo.util.DateUtil;

public abstract class BaseProvider extends AbstractProvider
    implements ExtendedProvider {
    private static final Log log = LogFactory.getLog(BaseProvider.class);
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    private Abdera abdera;
    private GeneratorFactory generatorFactory;
    private ServiceLocatorFactory serviceLocatorFactory;

    // AbstractProvider methods

    protected int getDefaultPageSize() {
        // XXX
        return 25;
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

    public void init() {
        if (abdera == null)
            throw new IllegalStateException("abdera is required");
        if (generatorFactory == null)
            throw new IllegalStateException("generatorFactory is required");
        if (serviceLocatorFactory == null)
            throw new IllegalStateException("serviceLocatorFactory is required");
    }

    protected ServiceLocator createServiceLocator(RequestContext context) {
        HttpServletRequest request =
            ((HttpServletRequestContext)context).getRequest();
        return serviceLocatorFactory.createServiceLocator(request);
    }

    protected ResponseContext preconditionfailed(Abdera abdera,
                                                 RequestContext request,
                                                 String reason) {
        return returnBase(createErrorDocument(abdera, 412, reason, null),
                          412, null);
    }

    protected ResponseContext locked(Abdera abdera,
                                     RequestContext request) {
        return returnBase(createErrorDocument(abdera, 423, "Collection Locked",
                                              null),
                          423, null);
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

        return rc;
    }
}
