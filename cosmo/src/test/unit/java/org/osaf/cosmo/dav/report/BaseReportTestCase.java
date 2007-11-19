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

import org.apache.jackrabbit.webdav.version.report.ReportInfo;

import org.osaf.cosmo.dav.BaseDavTestCase;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.model.EntityFactory;

import org.w3c.dom.Document;

/**
 * Base class for report tests.
 */
public abstract class BaseReportTestCase extends BaseDavTestCase {
    private static final Log log =
        LogFactory.getLog(BaseReportTestCase.class);

    protected DavResource makeTarget(Class clazz)
        throws Exception {
        return (DavResource)
            clazz.getConstructor(DavResourceLocator.class,
                                 DavResourceFactory.class,
                                 EntityFactory.class).
                newInstance(testHelper.getHomeLocator(),
                            testHelper.getResourceFactory(),
                            testHelper.getEntityFactory());
    }

    protected ReportBase makeReport(Class clazz,
                                    String reportXml,
                                    int depth,
                                    DavResource target)
        throws Exception {
        ReportBase report = (ReportBase) clazz.newInstance();
        report.init(target, makeReportInfo(reportXml, depth));
        return report;
    }

    protected ReportInfo makeReportInfo(String reportXml,
                                        int depth)
        throws Exception {
        Document doc = testHelper.loadXml(reportXml);
        return new ReportInfo(doc.getDocumentElement(), depth);
    }
}
