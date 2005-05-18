<!--
    Copyright 2005 Open Source Applications Foundation

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->

<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>

<cosmo-core:user var="user"/>
<cosmo:homedir var="homedir" user="${user}"/>
<cosmo:baseurl var="baseurl"/>

<p>
  <fmt:message key="Welcome.WelcomeMsg">
    <fmt:param value="${pageContext.request.serverName}"/>
  </fmt:message>
</p>
<authz:authorize ifAllGranted="ROLE_USER">
  <p>
    <fmt:message key="Welcome.User.HomeDirectory"/>
    <html:link page="${homedir}">
      <b>${baseurl}${homedir}</b>
    </html:link>
  </p>
  <p>
    <fmt:message key="Welcome.User.YourHomeDirectory"/>
  </p>
</authz:authorize>
