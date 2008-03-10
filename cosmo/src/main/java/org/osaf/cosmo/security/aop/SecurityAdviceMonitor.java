/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.osaf.cosmo.security.aop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;

/**
 * Advice for determining if methods have been secured.
 */
@Aspect
public class SecurityAdviceMonitor implements Ordered {

    private int order = 0;
   
    private static final Log log =
        LogFactory.getLog(SecurityAdviceMonitor.class);
    
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
    
    // Execute for all ContentService methods and log those
    // method calls that are not secured.  A method that is 
    // secured means that security advice was executed.
    @Around("execution(* org.osaf.cosmo.service.ContentService.*(..))")
    public Object baseSecurityCheck(ProceedingJoinPoint pjp) throws Throwable {
      
        if(log.isDebugEnabled())
           log.debug("in baseSecurityCheck()");
       
        SecurityAdvice.setSecured(false);
        Object returnVal = pjp.proceed();
        if(SecurityAdvice.getSecured()==Boolean.FALSE)
            log.warn("method not secured: " + pjp.toString());
        SecurityAdvice.setSecured(false);
        return returnVal;
    }
}
