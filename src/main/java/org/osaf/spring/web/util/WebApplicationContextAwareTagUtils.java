package org.osaf.spring.web.util;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;

import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This class provides utility methods for JSP tags which use a
 * Spring
 * {@link org.springframework.web.context.WebApplicationContext}. All
 * methods are static, and the class may not be instantiated.
 */
public abstract class WebApplicationContextAwareTagUtils {

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
}
