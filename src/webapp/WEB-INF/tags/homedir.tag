<%@ include   file="/WEB-INF/jsp/taglibs.jsp"            %>
<%@ attribute name="user"               required="false"
              type="org.osaf.cosmo.model.User"           %>
<%@ attribute name="var"                required="true"
              rtexprvalue="false"                        %>
<%@ variable  name-from-attribute="var" alias="result"
              scope="AT_END"                             %>

<c:if test="${empty user}">
  <cosmoui:user var="user"/>
</c:if>

<c:if test="${pageContext.request.contextPath != '/'}">
  <c:set var="result" value="${pageContext.request.contextPath}"/>
</c:if>

<c:set var="result" value="${result}/home/${user.username}/"/>
