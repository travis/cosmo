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
    <fmt:message key="Welcome.HomeDirectory"/>
    <html:link page="${homedir}">
      <b>${baseurl}${homedir}</b>
    </html:link>
  </p>
  <p>
    <fmt:message key="Welcome.YourHomeDirectory"/>
  </p>
</authz:authorize>
