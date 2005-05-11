<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ attribute name="property" required="true" %>

<logic:messagesPresent property="${property}"><span class="error"><html:messages property="${property}" id="msg">${msg}<br/></html:messages></span></logic:messagesPresent>
