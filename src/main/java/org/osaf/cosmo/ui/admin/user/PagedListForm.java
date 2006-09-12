/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.ui.admin.user;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import org.osaf.cosmo.util.PageCriteria;

/**
 * Action for managing users.
 */
public class PagedListForm extends ActionForm {

    private PageCriteria pageCriteria;
    
    /**
     */
    public PagedListForm() {
        pageCriteria = new PageCriteria();
    }

    /**
     */
    public PageCriteria getPageCriteria() {
        return pageCriteria;
    }

    /**
     */
    public void setPageCriteria(PageCriteria pageCriteria) {
        this.pageCriteria = pageCriteria;
    }


    
    /**
     */
    public void reset(ActionMapping mapping,
                      HttpServletRequest request) {
        super.reset(mapping, request);
        pageCriteria.initialize();
    }
}
