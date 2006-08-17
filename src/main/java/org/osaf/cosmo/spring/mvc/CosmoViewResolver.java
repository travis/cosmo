package org.osaf.cosmo.spring.mvc;

import java.util.Locale;

import javax.servlet.ServletException;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class CosmoViewResolver extends InternalResourceViewResolver {
    private String prefix = "";
    private String suffix = "";
    private String requestContextAttribute = "";
    

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setRequestContextAttribute(String requestContextAttribute) {
        this.requestContextAttribute = requestContextAttribute;
    }
    
    protected View loadView(String viewName, Locale locale) throws ServletException {
        try {
            InternalResourceView view = (InternalResourceView) this.getViewClass().newInstance();
            view.setUrl(this.prefix + viewName + this.suffix);
            view.setRequestContextAttribute(this.requestContextAttribute);
            return view;
        }
        catch (InstantiationException ex) {
            throw new ServletException("Cannot instantiate view class", ex);
        }
        catch (IllegalAccessException ex) {
            throw new ServletException("Cannot access view class", ex);
        }
    }
}
