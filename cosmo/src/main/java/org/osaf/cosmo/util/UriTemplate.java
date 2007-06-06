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

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang.text.StrTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Models a URI pattern such that candidate URIs can be matched
 * against the template to extract interesting information from them.
 * </p>
 * <p>
 * A URI pattern looks like
 * <code>/user/{username}/preference/{preference}</code>. Each path
 * segment can either be a literal segment. When a uri-path (the
 * portion of the URI after the host and port information) is matched
 * against the pattern, the result is a map of variable names and
 * values extracted from the candidate URI (in the above example
 * pattern, the variables are <code>username</code> and
 * <code>preference</code>).
 * </p>
 * <p>
 * Inspired by the .NET UriTemplate class.
 * </p>
 */
public class UriTemplate {
    private static final Log log = LogFactory.getLog(UriTemplate.class);

    private String pattern;
    private ArrayList<String> segments;

    public UriTemplate(String pattern) {
        this.pattern = pattern;
        this.segments = new ArrayList<String>();

        StrTokenizer tokenizer = new StrTokenizer(pattern, '/');
        while (tokenizer.hasNext())
            segments.add(tokenizer.nextToken());
    }

    /**
     * <p>
     * Matches a candidate uri-path against the template. Returns a
     * <code>Match</code> instance containing the names and values of
     * all variables found in the uri-path as specified by the
     * template.
     * </p>
     * <p>
     * Each literal segment in the template must match the
     * corresponding segment in the uri-path. For each variable
     * segment in the template, an entry is added to the
     * <code>Match</code> to be returned; the entry key is the
     * variable name from the template, and the entry value is the
     * corresponding (unescaped) token from the uri-path.
     * </p>
     *
     * @param path the candidate uri-path
     * @return a <code>Match</code>, or <code>null</code> if the path
     * did not successfully match
     */
    public Match match(String path) {
        Match match = new Match();

        if (log.isDebugEnabled())
            log.debug("matching " + path + " to " + pattern);

        StrTokenizer tokenizer = new StrTokenizer(path, '/');
        Iterator<String> si = segments.iterator();
        while (tokenizer.hasNext()) {
            if (! si.hasNext())
                return null;

            String segment = si.next();
            String token = tokenizer.nextToken();

            if (segment.startsWith("{") && segment.endsWith("}")) {
                try {
                    String key = segment.substring(1, segment.length()-1);
                    String value = URLDecoder.decode(token, "UTF-8");
                    match.put(key, value);
                } catch (Exception e) {
                    throw new RuntimeException("Cannot decode path segment " + token, e);
                }
            } else if (! segment.equals(token)) {
                // literal segment doesn't match, so path is not a match
                return null;
            }
        }

        return match;
    }

    public class Match {
        private HashMap<String, String> variables =
            new HashMap<String, String>();

        public String get(String key) {
            return variables.get(key);
        }

        protected void put(String key,
                           String value) {
            variables.put(key, value);
        }

        public String toString() {
            return variables.toString();
        }
    }
}
