<%@ page language="java" contentType="text/html; charset=UTF-8" %>

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

<cosmo:dialogLayout prefix="Forgot.">

<fmt:message var="signupButton" key="Forgot.Button.Signup"/>
<fmt:message var="cancelButton" key="Button.Cancel"/>
<fmt:message var="closeButton" key="Button.Close"/>

<logic:messagesPresent message="true">
  <p>
    <span class="confirm"><html:messages message="true" id="msg"><c:out value="${msg}"/><br/></html:messages></span>
  </p>
    <input type="submit" name="ok" value="${closeButton}" class="md"
           onClick="window.close()"/>
</logic:messagesPresent>
<logic:messagesNotPresent message="true">
  <p class="hd">
    <fmt:message key="Forgot.Header"/>
  </p>
  <cosmo:errmsg/>
  <p>
    <fmt:message key="Forgot.Instructions"/>
  </p>
  <html:form action="/remind">
    <p>
      <b><fmt:message key="Forgot.Label.Email"/></b>
      <html:text property="email" size="32" maxlength="128"
                 styleClass="md"/>
      <cosmo:errmsg property="email"/>
    </p>
    <p>
      <html:submit property="username" styleClass="md">
        <fmt:message key="Forgot.Button.Username"/>
      </html:submit>
      <html:submit property="password" styleClass="md">
        <fmt:message key="Forgot.Button.Password"/>
      </html:submit>
      <input type="submit" name="signup" value="${signupButton}"
             class="md" onClick="window.opener.location='<html:rewrite page="/console/account/new"/>'; window.close()"/>
      <input type="submit" name="cancel" value="${cancelButton}"
             class="md" onClick="window.close()"/>
    </p>
  </html:form>
</logic:messagesNotPresent>
</cosmo:dialogLayout>