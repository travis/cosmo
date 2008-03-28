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
 */package org.osaf.cosmo.spring.mvc.views.resolvers;

import java.util.Locale;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.ResourceBundleViewResolver;

public class CosmoDefaultViewResolver extends InternalResourceViewResolver {
    private ResourceBundleViewResolver notFoundViewResolver;
    private String notFoundViewName;
    
    @Override
    protected View loadView(String viewName, Locale locale) throws Exception{
        
        if (viewName.startsWith("forward:") || viewName.startsWith("redirect:")){
            return super.loadView(viewName, locale);
        } else {
            return notFoundViewResolver.resolveViewName(notFoundViewName, locale);
        }
    
    }

    public void setNotFoundViewResolver(
            ResourceBundleViewResolver notFoundViewResolver) {
        this.notFoundViewResolver = notFoundViewResolver;
    }

    public String getNotFoundViewName() {
        return notFoundViewName;
    }

    public void setNotFoundViewName(String notFoundViewName) {
        this.notFoundViewName = notFoundViewName;
    }
}
