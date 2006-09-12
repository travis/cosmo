<%--
/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
--%>

<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>

<u:bind var="SERVER_ADMIN"
        type="org.osaf.cosmo.CosmoConstants"
        field="SC_ATTR_SERVER_ADMIN"/>

<div style="width:100%;" align="center">

  <div class="widgetBorder" style="margin-top:36px; width:360px; 
       background:#efefef;">   
    <div class="widgetContent" style="padding:16px; text-align:left;">  
      <div><fmt:message key="Help.NotCompleted"/></div>    
      <div style="margin-top:12px;"><fmt:message key="Help.SendMessageOpen"/><a 
           href="mailto:${applicationScope[SERVER_ADMIN]}">
      <fmt:message key="Help.SendMessageLinkText"/></a><fmt:message 
                   key="Help.SendMessageClose"/></div>  
    </div> 
  </div>

</div>


