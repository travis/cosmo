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
package org.osaf.cosmo.http;

import java.text.ParseException;

import org.apache.abdera.protocol.EntityTag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Models the HTTP <code>If-None-Match</code> request
 * header. Described in RFC 2616 section 14.26, this header is used to
 * make the execution of a method conditional based on the state of a
 * resource.
 * </p>
 */
public class IfNoneMatch {
    private static final Log log = LogFactory.getLog(IfNoneMatch.class);

    private boolean all;
    private EntityTag[] etags;

    public IfNoneMatch(String header)
        throws ParseException {
        try {
            if (header == null) {
                all = false;
                etags = new EntityTag[0];
            } else if (header.equals("*")) {
                all = true;
                etags = new EntityTag[0];
            } else {
                all = false;
                etags = EntityTag.parseTags(header);
            }
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid entity tag in If-None-Match header", 0);
        }
    }

    /**
     * <p>
     * Indicates whether or not the server should perform the
     * requested method.
     * <p>
     * <p>
     * If <code>*</code> is given and any current entity exists for
     * the resource, then the server MUST NOT perform the requested
     * method.
     * </p>
     * <p>
     * If any of the entity tags match the entity tag of the entity,
     * then the server MUST NOT perform the requested method.
     * </p>
     * <p>
     * If none of the entity tags match, the server MAY perform
     * the requested method.
     * </p>
     * <p>
     * If no etags were provided in the header, the method is
     * allowed.
     * </p>
     *
     * @param etag the tag for the entity being examined
     * @return true if the server may execute the method, false if not
     */
    public boolean allowMethod(EntityTag etag) {
        if (!all && etags.length == 0)
            return true;

        if (all)
            return etag == null;

        return ! EntityTag.matchesAny(etag, etags);
    }

    /**
     * @see allowMethod(EntityTag)
     */
    public static boolean allowMethod(String header,
                                      EntityTag etag)
        throws ParseException {
        return new IfNoneMatch(header).allowMethod(etag);
    }
}
