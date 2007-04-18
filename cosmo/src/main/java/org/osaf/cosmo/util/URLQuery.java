package org.osaf.cosmo.util;

import java.util.Map;
import java.util.Map.Entry;

public class URLQuery {
    private Map<String, String[]> parameterMap;
    public URLQuery(Map<String, String[]> parameterMap){
        this.parameterMap = parameterMap;
    }
    
    public String toString(Map<String, String[]> overrideMap){
        StringBuffer s = new StringBuffer();
        s.append("?");
        for (Entry<String, String[]> entry: parameterMap.entrySet()){
            
            String key = entry.getKey();
            
            String[] values = overrideMap.containsKey(key)?
                    overrideMap.get(key) : parameterMap.get(key);
            
            for (String query: values){
                s.append(key);
                s.append("=");
                s.append(query);
                s.append("&");
            }
            
        }
        return s.substring(0, s.length()-1);
    }
}
