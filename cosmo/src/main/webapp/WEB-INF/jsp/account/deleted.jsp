<%@ page language="java" contentType="text/html; charset=UTF-8" %>

<%--
/*
 * Copyright 2008 Open Source Applications Foundation
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

<fmt:setBundle basename="PimMessageResources"/>

<cosmo:staticbaseurl var="staticBaseUrl"/>

<cosmo:standardLayout prefix="Account.Deleted." showNav="false">
<style type="text/css">
p {
  margin-left: auto; 
  margin-right: auto; 
  width: 30em; 
  text-align: center;"
}
</style>
<p>
<fmt:message key="Account.Deleted.AccountDeleted"/>
</p>
<p>
<fmt:message key="Account.Deleted.NextAction">
<fmt:param><c:url value='/login'/></fmt:param>
<fmt:param><c:url value='/login?signup=true'/></fmt:param>
</fmt:message>
</p>
</cosmo:standardLayout>
