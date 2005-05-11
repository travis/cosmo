<%@ include file="/WEB-INF/jsp/taglibs.jsp"    %>
<%@ attribute name="exception" required="true"
              type="java.lang.Exception"       %>

<c:if test="${not empty exception}">
  <div class="pre">
${exception}<c:forEach var="element" items="${exception.stackTrace}">
    at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})</c:forEach>
</div>
  <c:if test="${not empty exception.cause}">
    <div class="pre">
${exception.cause}<c:forEach var="element" items="${exception.cause.stackTrace}">
    at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})</c:forEach>
  </div>
  </c:if>
</c:if>
