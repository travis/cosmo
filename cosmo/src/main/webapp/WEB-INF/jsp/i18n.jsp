<%@ page language="java" contentType="text/html; charset=UTF-8" %>

<%--
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
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"        prefix="c"      %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"         prefix="fmt"    %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"   prefix="fn"     %>
<fmt:setBundle basename="PimMessageResources"/>

<%-- This jsp generates json used by cosmo.util.i18n. 
     Please see /src/main/webapp/cosmo/util/i18n.js for more details. --%>

[{<c:forEach var="key" items="${messages}" varStatus="status"><c:if test='${status.count != 1}'>,</c:if>"<c:out value="${key}"/>": <fmt:message key="${key}" var="msg"/>"${fn:replace(fn:replace(msg, '"', '\\"'), '\'\'', '\'')}"</c:forEach>},
{<c:forEach var="item" items="${configProperties}" varStatus="status"><c:if test='${status.count != 1}'>,</c:if>"<c:out value="${item.key}"/>": "<c:out value="${item.value}"/>"</c:forEach>}]