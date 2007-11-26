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
package org.osaf.cosmo.dav.servlet;

import java.util.Date;

import org.apache.abdera.util.EntityTag;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dav.BaseDavTestCase;
import org.osaf.cosmo.dav.DavTestContext;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.NotModifiedException;
import org.osaf.cosmo.dav.PreconditionFailedException;

/**
 * Test class for {@link StandardRequestHandler}.
 */
public class StandardRequestHandlerTest extends BaseDavTestCase {
    private static final Log log = LogFactory.getLog(StandardRequestHandlerTest.class);

    public void testIfMatchAll() throws Exception {
        DavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        ctx.getHttpRequest().addHeader("If-Match", "*");

        try {
            new StandardRequestHandler().preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
        } catch (PreconditionFailedException e) {
            fail("If-Match all failed");
        }
    }

    public void testIfMatchOk() throws Exception {
        DavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        ctx.getHttpRequest().addHeader("If-Match", home.getETag());

        try {
            new StandardRequestHandler().preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
        } catch (PreconditionFailedException e) {
            fail("If-Match specific etag failed");
        }
    }

    public void testIfMatchNotOk() throws Exception {
        DavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        String requestEtag = "\"aeiou\"";
        ctx.getHttpRequest().addHeader("If-Match", requestEtag);

        try {
            new StandardRequestHandler().preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
            fail("If-Match bogus etag succeeded");
        } catch (PreconditionFailedException e) {}

        String responseEtag = (String) ctx.getHttpResponse().getHeader("ETag");
        assertNotNull("Null ETag header", responseEtag);
        assertEquals("Incorrect ETag header value", responseEtag, home.getETag());
    }

    public void testIfNoneMatchAll() throws Exception {
        DavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        ctx.getHttpRequest().setMethod("GET");
        ctx.getHttpRequest().addHeader("If-None-Match", "*");

        try {
            new StandardRequestHandler().preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
            fail("If-None-Match all succeeded");
        } catch (NotModifiedException e) {
            // expected
        } catch (PreconditionFailedException e) {
            fail("If-None-Match all (method GET) failed with 412");
        }

        String responseEtag = (String) ctx.getHttpResponse().getHeader("ETag");
        assertNotNull("Null ETag header", responseEtag);
        assertEquals("Incorrect ETag header value", responseEtag, home.getETag());
    }

    public void testIfNoneMatchOk() throws Exception {
        DavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        String requestEtag = "\"aeiou\"";
        ctx.getHttpRequest().addHeader("If-None-Match", requestEtag);

        try {
            new StandardRequestHandler().preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
        } catch (PreconditionFailedException e) {
            fail("If-None-Match bogus etag failed");
        }
    }

    public void testIfNoneMatchNotOk() throws Exception {
        DavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        ctx.getHttpRequest().setMethod("GET");
        ctx.getHttpRequest().addHeader("If-None-Match", home.getETag());

        try {
            new StandardRequestHandler().preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
            fail("If-None-Match specific etag succeeded");
        } catch (NotModifiedException e) {
            // expected
        } catch (PreconditionFailedException e) {
            fail("If-None-Match specific etag (method GET) failed with 412");
        }

        String responseEtag = (String) ctx.getHttpResponse().getHeader("ETag");
        assertNotNull("Null ETag header", responseEtag);
        assertEquals("Incorrect ETag header value", responseEtag, home.getETag());
    }

    public void testIfModifiedSinceUnmodified() throws Exception {
        DavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        Date requestDate = since(home, 1000000);
        ctx.getHttpRequest().addHeader("If-Modified-Since", requestDate);

        try {
            new StandardRequestHandler().preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
            fail("If-Modified-Since succeeded for unmodified resource");
        } catch (NotModifiedException e) {
            // expected
        } catch (PreconditionFailedException e) {
            fail("If-Modified-Since failed with 412 for unmodified resource");
        }
    }

    public void testIfModifiedSinceModified() throws Exception {
        DavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        Date requestDate = since(home, -1000000);
        ctx.getHttpRequest().addHeader("If-Modified-Since", requestDate);

        try {
            new StandardRequestHandler().preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
        } catch (PreconditionFailedException e) {
            fail("If-Modified-Since failed for mmodified resource");
        }
    }

    public void testIfUnmodifiedSinceUnmodified() throws Exception {
        DavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        Date requestDate = since(home, 1000000);
        ctx.getHttpRequest().addHeader("If-Unmodified-Since", requestDate);

        try {
            new StandardRequestHandler().preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
        } catch (PreconditionFailedException e) {    
            fail("If-Unmodified-Since failed for unmodified resource");
        }
    }

    // if unmodified since earlier date, pass

    public void testIfUnmodifiedSinceModified() throws Exception {
        DavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        Date requestDate = since(home, -1000000);
        ctx.getHttpRequest().addHeader("If-Unmodified-Since", requestDate);

        try {
            new StandardRequestHandler().preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
            fail("If-Unmodified-Since succeeded for modified resource");
        } catch (PreconditionFailedException e) {}
    }

    private static String etags(EntityTag[] etags) {
        return StringUtils.join(etags, ", ");
    }

    private static Date since(DavResource resource,
                              long delta) {
        return new Date((resource.getModificationTime() / 1000 * 1000) + delta);
    }
}
