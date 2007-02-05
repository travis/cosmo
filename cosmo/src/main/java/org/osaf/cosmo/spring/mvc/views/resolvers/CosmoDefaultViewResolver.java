package org.osaf.cosmo.spring.mvc.views.resolvers;

import java.util.Locale;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;


public class CosmoDefaultViewResolver extends InternalResourceViewResolver {
    private String requestContextAttribute = "";
    
    public void setRequestContextAttribute(String requestContextAttribute) {
        this.requestContextAttribute = requestContextAttribute;
    }
    
    @Override
    protected View loadView(String viewName, Locale locale) throws Exception{
        
        if (viewName.startsWith("forward:") || viewName.startsWith("redirect:")){
            return super.loadView(viewName, locale);
        } else {
            InternalResourceView view = new InternalResourceView();
            view.setUrl("/WEB-INF/jsp/error/notfound.jsp");
            view.setRequestContextAttribute(this.requestContextAttribute);
            return view;
        }
    
    }
}
