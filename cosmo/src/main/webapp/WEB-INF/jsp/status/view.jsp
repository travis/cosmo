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
<cosmo:standardLayout prefix="Status." contentWrapperClass="fullPageWidthContent">
<cosmo:cnfmsg/>

<!-- simple memory stats -->
<div style="margin-top:24px; text-align: left;">
  <table cellpadding="4" cellspacing="1" border="0">
    <tr>
      <td class="smTableColHead">
        <fmt:message key="Status.Memory.TH.Memory"/>
      </td>
      <td class="smTableColHead">
        <fmt:message key="Status.Memory.TH.Units"/>
      </td>
    </tr>
    <tr>
      <td class="smLabel">
        <fmt:message key="Status.Memory.Max"/>
      </td>
      <td class="smData" style="text-align:right">
        <fmt:formatNumber value="${Status.maxMemory}"/>
      </td>
    </tr>
    <tr>
      <td class="smLabel">
        <fmt:message key="Status.Memory.Total"/>
      </td>
      <td class="smData" style="text-align:right">
        <fmt:formatNumber value="${Status.totalMemory}"/>
      </td>
    </tr>
    <tr>
      <td class="smLabel">
        <fmt:message key="Status.Memory.Used"/>
      </td>
      <td class="smData" style="text-align:right">
        <fmt:formatNumber value="${Status.usedMemory}"/>
      </td>
    </tr>
    <tr>
      <td class="smLabel">
        <fmt:message key="Status.Memory.Free"/>
      </td>
      <td class="smData" style="text-align:right">
        <fmt:formatNumber value="${Status.freeMemory}"/>
      </td>
    </tr>
  </table>
</div>

<!-- controls -->
<div class="md" style="margin-top:24px; text-align: left;">
  <a href="<c:url value="/admin/status/gc"/>">
    <fmt:message key="Status.Controls.RunGC"/>
  </a>
  |
  <a href="<c:url value="/admin/status/dump"/>">
    <fmt:message key="Status.Controls.DumpRaw"/>
  </a>
</div>
</cosmo:standardLayout>