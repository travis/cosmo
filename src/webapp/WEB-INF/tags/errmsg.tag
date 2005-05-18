<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ attribute name="property" required="false" %>

<c:if test="${empty property}">
  <u:bind var="property"
          type="org.apache.struts.action.ActionMessages"
          field="GLOBAL_MESSAGE"/>
</c:if>

<logic:messagesPresent property="${property}"><span class="error"><html:messages property="${property}" id="msg">${msg}<br/></html:messages></span></logic:messagesPresent>
