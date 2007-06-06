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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
 * <code>/collection/{uid}/{projection}?/{format}?</code>. Each path
 * segment can be either a literal or a variable (the latter enclosed
 * in curly braces}. A segment can be further denoted as optional, in
 * which case the segment is trailed by a question mark.
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
     * Generates a uri-path based on the template with variable
     * segments replaced by the provided values. All literal segments,
     * optional or no, are always included. Values are bound into
     * the template in the order in which they are provided. If a
     * value is not provided for an optional variable segment, the
     * segment is not included.
     *
     * @param values the (unescaped) values to be bound
     * @return a uri-path with variables replaced by bound values
     * @throws IllegalArgumentException if more or fewer values are
     * provided than are needed by the template
     */
    public String bind(String... values) {
        StringBuffer buf = new StringBuffer("/");

        List<String> variables = Arrays.asList(values);
        Iterator<String> vi = variables.iterator();

        Iterator<Segment> si = segments.iterator();
        while (si.hasNext()) {
            Segment segment = si.next();

            if (segment.isVariable()) {
                if (! vi.hasNext()) {
                    if (segment.isOptional())
                        continue;
                    throw new IllegalArgumentException("Not enough variables");
                }
                buf.append(escape(vi.next()));
            } else {
                buf.append(segment.getData());
            }

            if (si.hasNext())
                buf.append("/");
        }

        if (vi.hasNext())
            throw new IllegalArgumentException("Too many variables");

        return buf.toString();
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
     * corresponding segment in the uri-path unless the segment is
     * optional. For each variable segment in the template, an entry
     * is added to the <code>Match</code> to be returned; the entry
     * key is the variable name from the template, and the entry value
     * is the corresponding (unescaped) token from the uri-path.
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

            if (segment.isVariable())
                match.put(segment.getData(), unescape(token));
            else if (! segment.getData().equals(token))
                // literal segment doesn't match, so path is not a match
                return null;
        }

        return match;
    }

    private static final String escape(String raw) {
        try {
            return URLEncoder.encode(raw, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Could not escape string " + raw, e);
        }
    }

    private static final String unescape(String escaped) {
        try {
            return URLDecoder.decode(escaped, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Could not unescape string " + escaped, e);
        }
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
            } else if (data.endsWith("?")) {
                optional = true;
                this.data = data.substring(0, data.length()-1);
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

        public String put(String key,
                          String value) {
            return super.put(key, value);
        }
    }
}
