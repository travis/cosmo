<%@ include   file="/WEB-INF/jsp/taglibs.jsp"            %>
<%@ attribute name="user"               required="true"
              type="org.osaf.cosmo.model.User"           %>
<%@ attribute name="reverse"            required="false" %>
<%@ attribute name="var"                required="true"
              rtexprvalue="false"                        %>
<%@ variable  name-from-attribute="var" alias="result"
              scope="AT_END"                             %>

<c:if test="${empty user}">
  <cosmoui:user var="user"/>
</c:if>

<c:if test="${empty reverse}">
  <c:set var="reverse" value="false"/>
</c:if>

<c:choose>
  <c:when test="${reverse}">
    <c:set var="result" value="${user.lastName}, ${user.firstName}"/>
  </c:when>
  <c:otherwise>
    <c:set var="result" value="${user.firstName} ${user.lastName}"/>
  </c:otherwise>
</c:choose>

