/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.cosmo.jcr;

import org.apache.jackrabbit.JcrConstants;

/**
 * Extends {@link org.apache.jackrabbit.JcrConstants} to
 * provide additional constants for JCR items, node types
 * etc. implemented by Cosmo.
 */
public interface CosmoJcrConstants extends JcrConstants {

    // node types

    public static final String NT_TICKET = "dav:ticket";

    // node properties

    public static final String NP_OWNER = "dav:owner";
    public static final String NP_TIMEOUT = "dav:timeout";
    public static final String NP_READ = "dav:read";
    public static final String NP_WRITE = "dav:write";
}
