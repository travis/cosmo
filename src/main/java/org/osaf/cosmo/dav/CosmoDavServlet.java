package org.osaf.cosmo.dav;

import javax.servlet.ServletException;

import org.apache.jackrabbit.server.simple.WebdavServlet;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavSessionProvider;

import org.apache.log4j.Logger;

import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * An extension of Jackrabbit's 
 * {@link org.apache.jackrabbit.server.simple.WebdavServlet} which
 * integrates the Spring Framework for configuring support objects.
 */
public class CosmoDavServlet extends WebdavServlet {
    private static final Logger log =
        Logger.getLogger(CosmoDavServlet.class);

    /** The name of the Spring bean identifying the servlet's
     * {@link org.apache.jackrabbit.webdav.DavResourceFactory}
     */
    public static final String BEAN_DAV_RESOURCE_FACTORY =
        "resourceFactory";
    /** The name of the Spring bean identifying the servlet's
     * {@link org.apache.jackrabbit.webdav.DavSessionProvider}
     */
    public static final String BEAN_DAV_SESSION_PROVIDER =
        "sessionProvider";

    private WebApplicationContext wac;

    /**
     * Load the servlet context's
     * {@link org.springframework.web.context.WebApplicationContext}
     * and look up support objects.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {
        super.init();
        wac = WebApplicationContextUtils.
            getRequiredWebApplicationContext(getServletContext());

        DavSessionProvider sessionProvider = (DavSessionProvider)
            getBean(BEAN_DAV_SESSION_PROVIDER,
                    DavSessionProvider.class);
        setSessionProvider(sessionProvider);

        DavResourceFactory resourceFactory = (DavResourceFactory)
            getBean(BEAN_DAV_RESOURCE_FACTORY,
                    DavResourceFactory.class);
        setResourceFactory(resourceFactory);
    }

    /**
     * Looks up the bean with given name and class in the web
     * application context.
     *
     * @param name the bean's name
     * @param clazz the bean's class
     */
    protected Object getBean(String name, Class clazz)
        throws ServletException {
        try {
            return wac.getBean(name, clazz);
        } catch (BeansException e) {
            throw new ServletException("Error retrieving bean " + name +
                                       " of type " + clazz +
                                       " from web application context", e);
        }
    }

    /**
     */
    public WebApplicationContext getWebApplicationContext() {
        return wac;
    }
}
