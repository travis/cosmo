/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.server;

import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;

import org.osaf.cosmo.model.Item;

/**
 * Utility methods related to the protocols and interfaces presented
 * to clients.
 */
public class ServerUtils implements ServerConstants {

    private static final MessageDigest etagDigest;
    private static final Base64 etagEncoder = new Base64();

    static {
        try {
            etagDigest = MessageDigest.getInstance("sha1");
        } catch (Exception e) {
            throw new RuntimeException("Platform does not support sha1?", e);
        }
    }

    /**
     * Returns all ticket keys found in the request, both in the
     * {@link #HEADER_TICKET} header and the {@link #PARAM_TICKET}
     * parameter.
     */
    public static Set findTicketKeys(HttpServletRequest request) {
        HashSet<String> keys = new HashSet<String>();

        Enumeration headerValues = request.getHeaders(HEADER_TICKET);
        if (headerValues != null) {
            while (headerValues.hasMoreElements()) {
                String value = (String) headerValues.nextElement();
                String[] atoms = value.split(", ");
                for (int i=0; i<atoms.length; i++) {
                    keys.add(atoms[i]);
                }
            }
        }

        String[] paramValues = request.getParameterValues(PARAM_TICKET);
        if (paramValues != null) {
            for (int i=0; i<paramValues.length; i++) {
                keys.add(paramValues[i]);
            }
        }

        return keys;
    }

    /**
     * Calculates the HTTP entity tag for an item based on its uid and
     * last modification time.
     */
    public static String calculateEtag(Item item) {
        String uid= item.getUid() != null ? item.getUid() : "-";
        String modTime = item.getModifiedDate() != null ?
            new Long(item.getModifiedDate().getTime()).toString() : "-";
        String etag = uid + ":" + modTime;
        byte[] digest = etagDigest.digest(etag.getBytes());
        return new String(etagEncoder.encode(digest));
   }
}
