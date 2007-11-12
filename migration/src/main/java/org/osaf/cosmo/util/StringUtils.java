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

/**
 * Contains simple String utility methods.
 */
public class StringUtils {

    private StringUtils() {

    }

    /**
     * Replace all occurrences of a string with a replacement string.
     * @param str string to search
     * @param target search string
     * @param replacement string to replace target occurrences with
     * @return original string with all occurrences of the target string
     *         replaced with the replacement string
     */
    public static String replace(String str, String target, String replacement) {
        return str.replace(target, replacement);
    }
}
