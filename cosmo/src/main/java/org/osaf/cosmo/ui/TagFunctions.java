/* Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.ui;

import java.util.Properties;

import org.osaf.cosmo.spring.CosmoPropertyPlaceholderConfigurer;

public class TagFunctions {
    private static Properties configProperties;
    private static String staticHostUrl = null;
    private static boolean set = false;
    
    public static void setPropertyPlaceholderConfigurer(CosmoPropertyPlaceholderConfigurer configurer){
        configProperties = configurer.getProperties();
    } 
    
    public static String getStaticHostUrlTemplate(){
        return configProperties.getProperty("cosmo.ui.statichost.urlTemplate");
    }
    
    public static String getStaticHostUrlRange(){
        return configProperties.getProperty("cosmo.ui.statichost.range");
    }
    
    public static String getConfigProperty(String propertyName){
        return configProperties.getProperty(propertyName);
    }
    
    public static String getStaticHostUrl(){
        if (set){
            return staticHostUrl;
        }
        
        String template = getStaticHostUrlTemplate();
        if (template != null){
            String range = getStaticHostUrlRange();
            if (range == null){
                staticHostUrl = template;
            } else {
                String[] parsedRange = range.split("\\.\\.");
                staticHostUrl = template.replace("*", parsedRange[0]);
            }
        }
        set = true;
        return staticHostUrl;
    }
    
    public static void main (String[] args){
        System.out.println("1..2".split("\\.\\."));   
    }
}