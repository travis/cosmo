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
package org.osaf.cosmo.dav.acl.resource;

import java.util.Set;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.property.DavPropertyName;

import org.osaf.cosmo.dav.BaseDavTestCase;
import org.osaf.cosmo.dav.acl.AclConstants;
import org.osaf.cosmo.dav.acl.property.AlternateUriSet;
import org.osaf.cosmo.dav.acl.property.GroupMembership;
import org.osaf.cosmo.dav.acl.property.PrincipalUrl;
import org.osaf.cosmo.dav.property.DavProperty;
import org.osaf.cosmo.dav.property.DisplayName;
import org.osaf.cosmo.dav.property.ResourceType;

/**
 * Test case for <code>DavUserPrincipal</code>.
 */
public class DavUserPrincipalTest extends BaseDavTestCase
    implements AclConstants {
    private static final Log log =
        LogFactory.getLog(DavUserPrincipalTest.class);

    // all section references are from RFC 3744

    public void testLoadProperties() throws Exception {
        DavUserPrincipal p = testHelper.getPrincipal(testHelper.getUser());

        DavProperty prop = null;

        // section 4

        DisplayName displayName = (DisplayName)
            p.getProperty(DavPropertyName.DISPLAYNAME);
        assertNotNull("No displayname property", displayName);
        assertTrue("Empty displayname ",
                    ! StringUtils.isBlank(displayName.getDisplayName()));

        ResourceType resourceType = (ResourceType)
            p.getProperty(DavPropertyName.RESOURCETYPE);
        assertNotNull("No resourcetype property", resourceType);
        boolean foundPrincipalQname = false;
        for (QName qname : resourceType.getQnames()) {
            if (qname.equals(RESOURCE_TYPE_PRINCIPAL)) {
                foundPrincipalQname = true;
                break;
            }
        }
        assertTrue("Principal qname not found", foundPrincipalQname);

        // 4.1
        AlternateUriSet alternateUriSet = (AlternateUriSet)
            p.getProperty(ALTERNATEURISET);
        assertNotNull("No alternate-uri-set property", alternateUriSet);
        assertTrue("Found hrefs for alternate-uri-set",
                   alternateUriSet.getHrefs().isEmpty());

        // 4.2
        PrincipalUrl principalUrl = (PrincipalUrl)
            p.getProperty(PRINCIPALURL);
        assertNotNull("No principal-URL property", principalUrl);
        assertEquals("principal-URL value not the same as locator href",
                     p.getResourceLocator().getHref(false),
                     principalUrl.getHref());

        // 4.4
        GroupMembership groupMembership = (GroupMembership)
            p.getProperty(GROUPMEMBERSHIP);
        assertNotNull("No group-membership property", groupMembership);
        assertTrue("Found hrefs for group-membership",
                   groupMembership.getHrefs().isEmpty());
    }
}
