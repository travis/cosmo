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
package org.osaf.cosmo.atom.servlet;

import org.apache.abdera.protocol.ItemManager;
import org.apache.abdera.protocol.Request;
import org.apache.abdera.protocol.server.RequestHandler;

public class StandardRequestHandlerManager
    implements ItemManager<RequestHandler> {

    private RequestHandler requestHandler;

    // ItemManager methods

    public RequestHandler get(Request request) {
        return requestHandler;
    }

    public void release(RequestHandler item) {}

    // our methods

    public void setRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public void init() {
        if (requestHandler == null)
            throw new IllegalStateException("requestHandler is required");
    }
}
