<%@ page    isErrorPage="true"               %>
<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>

<p>
  <fmt:message key="Error.General.ErrorOccurred"/>
</p>
<cosmo:stacktrace exception="${Exception}"/>
