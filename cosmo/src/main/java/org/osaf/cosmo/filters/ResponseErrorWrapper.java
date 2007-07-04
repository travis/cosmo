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
package org.osaf.cosmo.filters;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * HttpServletResponseWrapper that catchs calls to sendError() and
 * delays invoking sendError on the wrapped response until
 * flushError() is called.  This allows a call to sendError() to
 * be voided.
 */
public class ResponseErrorWrapper extends HttpServletResponseWrapper {
    
    private String msg = null;
    private Integer code = null;
    
    public ResponseErrorWrapper(HttpServletResponse response) throws IOException {
        super(response);
    }

    @Override
    public void sendError(int code, String msg) throws IOException {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public void sendError(int code) throws IOException {
        this.code = code;
    }
    
    /**
     * Invoke sendError() on wrapped response if sendError() was
     * invoked on wrapper.
     * @throws IOException
     */
    public void flushError() throws IOException {
        if(code!=null && msg!=null)
            super.sendError(code, msg);
        else if(code!=null)
            super.sendError(code);
    }
    
    /**
     * Clear error, voiding sendError().
     */
    public void clearError() {
        code = null;
        msg = null;
    }
}
