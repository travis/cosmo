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
package org.osaf.cosmo.dav.report;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;

import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;

/**
 * Base class for WebDAV reports that return multistatus responses.
 */
public abstract class MultiStatusReport extends ReportBase {
    private static final Log log = LogFactory.getLog(MultiStatusReport.class);

    private MultiStatus multistatus = new MultiStatus();
    private int propfindType = PROPFIND_ALL_PROP;
    private DavPropertyNameSet propfindProps;

    // Report methods

    public boolean isMultiStatusReport() {
        return true;
    }

    // our methods

    /**
     * Generates and writes the multistatus response.
     */
    protected void output(DavServletResponse response)
        throws DavException {
        DavPropertyNameSet resultProps = createResultPropSpec();

        for (DavResource result : getResults()) {
            MultiStatusResponse msr =
                buildMultiStatusResponse(result, resultProps);
            multistatus.addResponse(msr);
        }

        try {
            response.sendXmlResponse(multistatus, 207);
        } catch (Exception e) {
            throw new DavException(e);
        }
    }

    protected DavPropertyNameSet createResultPropSpec() {
        return new DavPropertyNameSet(propfindProps);
    }

    /**
     * Returns a <code>MultiStatusResponse</code> describing the
     * specified resource including the specified properties.
     */
    protected MultiStatusResponse
        buildMultiStatusResponse(DavResource resource,
                                 DavPropertyNameSet props)
        throws DavException {
        if (props.isEmpty()) {
            String href = resource.getResourceLocator().
                getHref(resource.isCollection());
            return new MultiStatusResponse(href, 200);
        }
        return new MultiStatusResponse(resource, props, propfindType);
    }

    protected MultiStatus getMultiStatus() {
        return multistatus;
    }

    public int getPropFindType() {
        return propfindType;
    }

    public void setPropFindType(int type) {
        this.propfindType = type;
    }

    public DavPropertyNameSet getPropFindProps() {
        return propfindProps;
    }

    public void setPropFindProps(DavPropertyNameSet props) {
        this.propfindProps = props;
    }
}
