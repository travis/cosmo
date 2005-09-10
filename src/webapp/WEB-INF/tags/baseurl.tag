<%@ include   file="/WEB-INF/jsp/taglibs.jsp"            %>
<%@ attribute name="var"                required="true"
              rtexprvalue="false"                        %>
<%@ variable  name-from-attribute="var" alias="result"
              scope="AT_END"                             %>

<c:set var="result"
       value="${pageContext.request.scheme}://${pageContext.request.serverName}"/>
<c:if test="${(pageContext.request.secure &&
            pageContext.request.serverPort != 443) ||
            (pageContext.request.serverPort != 80)}">
  <c:set var="result"
         value="${result}:${pageContext.request.serverPort}"/>
</c:if>
<c:if test="${pageContext.request.contextPath != '/'}">
  <c:set var="result"
         value="${result}${pageContext.request.contextPath}"/>
</c:if>
