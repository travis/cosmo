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
package org.osaf.cosmo.util;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

public class MimeUtil {

    public static final String MEDIA_TYPE_FORM_ENCODED =
        "application/x-www-form-urlencoded";
    public static final MimeType MIME_TYPE_FORM_ENCODED;
    public static final String MEDIA_TYPE_XHTML = "application/xhtml+xml";
    public static final MimeType MIME_TYPE_XHTML;

    static {
        try {
            MIME_TYPE_FORM_ENCODED = new MimeType(MEDIA_TYPE_FORM_ENCODED);
            MIME_TYPE_XHTML = new MimeType(MEDIA_TYPE_XHTML);
        } catch (MimeTypeParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean match(MimeType a,
                                MimeType b) {
        return (a != null && b != null && a.match(b));
    }

    public static boolean isFormEncoded(MimeType mimeType) {
        return match(mimeType, MIME_TYPE_FORM_ENCODED);
    }

    public static boolean isXhtml(MimeType mimeType) {
        return match(mimeType, MIME_TYPE_XHTML);
    }
}
