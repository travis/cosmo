<%--
/*
 * Copyright 2005 Open Source Applications Foundation
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

<u:bind var="SERVER_VERSION"
        type="org.osaf.cosmo.CosmoConstants"
        field="SC_ATTR_SERVER_VERSION"/>

<div style="width:100%;" align="center">

  <div style="width:300px; margin-top:48px;">

    <div><html:link page="http://wiki.osafoundation.org/bin/view/Projects/CosmoHome">
    <img src="/cosmo_logo_large.jpg" alt="<fmt:message 
         key="About.LogoAltText"/>"/>
    </html:link>    
    </div>
    <div class="smLabel" style="margin-top:2px;"><fmt:message 
         key="About.VersionString"/>${applicationScope[SERVER_VERSION]}
    </div>
    <div style="margin-top:28px;"><fmt:message key="About.LicenseOpen"/>
    <html:link page="http://www.apache.org/licenses/LICENSE-2.0"><fmt:message 
               key="About.LicenseLinkText"/></html:link><fmt:message 
               key="About.LicenseClose"/>
    </div>
    <div style="margin-top:8px;"><fmt:message key="About.InfoOpen"/><html:link 
         page="http://wiki.osafoundation.org/bin/view/Projects/CosmoHome">
    <fmt:message key="About.InfoLinkText"/></html:link><fmt:message 
                 key="About.InfoClose"/></div>
    </div>
    
  </div>

</div>


