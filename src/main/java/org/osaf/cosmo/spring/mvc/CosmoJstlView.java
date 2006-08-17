package org.osaf.cosmo.spring.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.JstlView;

public class CosmoJstlView extends JstlView {

    @Override
    protected String prepareForRendering(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return "/" + getUrl();
    }

}
