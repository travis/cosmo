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
<fmt:setBundle basename="MessageResources"/>
<cosmo:standardLayout prefix="Account.PasswordReset." showNav="false">
<cosmo:staticbaseurl var="staticBaseUrl"/>
<script type="text/javascript">
dojo.require("cosmo.ui.widget.PasswordResetter");

</script>
<style type="text/css">
#passwordResetter{
margin-left: auto;
margin-right: auto;
}
</style>

<div dojoType="cosmo:PasswordResetter" displayDefaultInfo="true" recoveryKey="${param.key}" widgetId="passwordResetter"/>

</cosmo:standardLayout>