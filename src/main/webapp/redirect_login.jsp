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
<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>

<cosmo:staticbaseurl var="staticBaseUrl"/>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

<title></title>

<cosmo:dojoBoilerplate/>


<script type="text/javascript">
dojo.require("cosmo.util.cookie");
dojo.require("cosmo.util.auth");

function init() {

    if (opener) {
        opener.location = location.href;
        window.close();
    }
    else {
        cosmo.util.cookie.destroy('JSESSIONID', '/cosmo');
        cosmo.util.cookie.destroy('inputTimestamp');
        cosmo.util.cookie.destroy('username');
		cosmo.util.auth.clearAuth();

        location = '${staticBaseUrl}/';
    }
}

window.onload = init;

</script>

</head>

<body>Logging out...</body>

</html>
