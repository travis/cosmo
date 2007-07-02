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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.osaf.cosmo.dav.impl.BufferedServletInputStream;

/**
 * InvocationHandler that allows a BufferedServletInputStream to be
 * used as part of a given HttpServletRequest.  This allows a dynamic
 * HttpServletRequest proxy to be created that delegates all methods
 * to a given HttpServletRequest except for "getInputStream", which
 * returns the instance of BufferedServletInputStream.
 */
public class HttpServletRequestInvocationHandler implements InvocationHandler {

    private HttpServletRequest request = null;
    private BufferedServletInputStream inputStream = null;
    private boolean retryRequest = false;
    
    /**
     * @param request delegate request
     * @param inputStream buffered input stream to use
     */
    public HttpServletRequestInvocationHandler(HttpServletRequest request, BufferedServletInputStream inputStream)  {
        this.request = request;
        this.inputStream =  inputStream;
    }
    
    /**
     * When called, next call to getInputStream() will reset the buffered
     * inputstream, allowing the request to be re-processed
     */
    public void retryRequest() {
        retryRequest = true;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        String name = method.getName();
        
        // Look for "getInputStream"
        if("getInputStream".equals(name)) {
            if(retryRequest) {
                retryRequest = false;
                inputStream.resetToBeginning();
            }
            return inputStream;
        } else {
            // handle all other methods by invoking on delegate request
            try {
                return method.invoke(request, args);
            } catch (InvocationTargetException  e) {
                throw e.getTargetException();
            } catch (Exception e) {
                throw new RuntimeException("unexpected invocation exception: " +
                       e.getMessage());
            }
        }
    }
}
