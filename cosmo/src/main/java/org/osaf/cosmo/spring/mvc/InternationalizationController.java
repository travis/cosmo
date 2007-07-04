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
package org.osaf.cosmo.spring.mvc;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osaf.cosmo.spring.CosmoPropertyPlaceholderConfigurer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class InternationalizationController extends AbstractController {
    public static final String UI_PREFIX = "ui.";
    private  HashMap<String, String> properties = new HashMap<String, String>();

    public void setPropertyPlaceholderConfigurer(CosmoPropertyPlaceholderConfigurer configurer) {
        Properties configProps = configurer.getProperties();
        Enumeration propertyNames = configProps.propertyNames();
        while (propertyNames.hasMoreElements()){
            String propName = (String) propertyNames.nextElement();
            if (propName.startsWith(UI_PREFIX)){
               properties.put(propName.substring(UI_PREFIX.length()), configProps.getProperty(propName)); 
            }
        }

    }

    public ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("i18n");
        ResourceBundle bundle = ResourceBundle.getBundle("PimMessageResources",
                request.getLocale());
        Enumeration messages = bundle.getKeys();
        mav.addObject("messages", messages);
        mav.addObject("configProperties", properties);
        return mav;
    }
}