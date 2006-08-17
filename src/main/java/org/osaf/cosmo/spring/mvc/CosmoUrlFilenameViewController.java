package org.osaf.cosmo.spring.mvc;

import org.springframework.web.servlet.mvc.UrlFilenameViewController;
import org.springframework.web.util.WebUtils;

public class CosmoUrlFilenameViewController extends UrlFilenameViewController {
    
    protected String extractViewNameFromUrlPath(String uri) {
        int dotIndex = uri.lastIndexOf('.');
        if (dotIndex != -1) {
            uri = uri.substring(0, dotIndex);
        }
        return uri;
    }
    
}
