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

<p>
  <div style="margin-bottom:4px;"><fmt:message key="Welcome.WelcomeMsg">
    <fmt:param value="${pageContext.request.serverName}"/>
  </fmt:message></div>
  <div><html:link page="/login">Click here to log in.</html:link></div>
</p>

<p>If you do not have an account, <html:link page="/account/new">click here to create one</html:link>.</p>
