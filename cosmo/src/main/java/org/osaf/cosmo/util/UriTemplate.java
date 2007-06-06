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
    private ArrayList<Segment> segments;

    public UriTemplate(String pattern) {
        this.pattern = pattern;
        this.segments = new ArrayList<Segment>();

        StrTokenizer tokenizer = new StrTokenizer(pattern, '/');
        while (tokenizer.hasNext())
            segments.add(new Segment(tokenizer.nextToken()));
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

        StrTokenizer candidate = new StrTokenizer(path, '/');
        Iterator<Segment> si = segments.iterator();

        while (si.hasNext()) {
            Segment segment = si.next();

            if (! candidate.hasNext()) {
                // if the segment is optional, the candidate doesn't
                // have to have a matching segment
                if (segment.isOptional())
                    continue;
                // mandatory segment - not a match
                return null;
            }

            String token = candidate.nextToken();

            if (segment.isVariable()) {
                try {
                    String value = URLDecoder.decode(token, "UTF-8");
                    match.put(segment.getData(), token);
                } catch (Exception e) {
                    throw new RuntimeException("Cannot decode path segment " + token, e);
                }
            } else if (! segment.getData().equals(token)) {
                // literal segment doesn't match, so path is not a match
                return null;
            }
        }

        return match;
    }

    private class Segment {
        private String data;
        private boolean variable = false;
        private boolean optional = false;

        public Segment(String data) {
            if (data.startsWith("{")) {
                if (data.endsWith("}?")) {
                    variable = true;
                    optional = true;
                    this.data = data.substring(1, data.length()-2);
                } else if (data.endsWith("}")) {
                    variable = true;
                    this.data = data.substring(1, data.length()-1);
                }
            }
            if (this.data == null)
                this.data = data;
        }

        public String getData() {
            return data;
        }

        public boolean isVariable() {
            return variable;
        }

        public boolean isOptional() {
            return optional;
        }
    }

    public class Match extends HashMap<String, String> {

        public String get(String key) {
            return super.get(key);
        }

        /**
         * Overrides the superclass method to unescape the value.
         */
        public String put(String key,
                          String value) {
            try {
                return super.put(key, URLDecoder.decode(value, "UTF-8"));
            } catch (Exception e) {
                throw new RuntimeException("Cannot decode variable value " + value, e);
            }
        }
    }
}
