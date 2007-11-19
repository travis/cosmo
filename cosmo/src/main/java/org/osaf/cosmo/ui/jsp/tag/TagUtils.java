/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.ui.jsp.tag;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;

import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This class provides utility methods for JSP tags. All methods are
 * static, and the class may not be instantiated.
 */
public abstract class TagUtils {

    /**
     * Looks up the bean with given name and class in the Spring
     * web application context.
     *
     * @param name the bean's name
     * @param clazz the bean's class
     * @return the bean as an <code>Object</code>
     * @throws JspException
     */
    public static Object getBean(ServletContext sc, String name, Class clazz)
        throws JspException {
        try {
            WebApplicationContext wac =
                WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
            return wac.getBean(name, clazz);
        } catch (BeansException e) {
            throw new JspException("Error retrieving bean " + name +
                                   " of type " + clazz +
                                   " from web application context", e);
        }
    }
    
    /**
     * Determines if an object is an instance of a given type.
     * @param type fully qualified type name
     * @param value object to test
     * @return true if the value is an instance of the given type
     */
    public static boolean instanceOf(String type, Object value) {

        try {
            Class c = Class.forName(type, true, Thread.currentThread()
                    .getContextClassLoader());
            return c.isInstance(value);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
