<%@ include   file="/WEB-INF/jsp/taglibs.jsp"            %>
<%@ include   file="/WEB-INF/jsp/tagfiles.jsp"            %>
<%@ attribute name="var"                required="true"
              rtexprvalue="false"                        %>
<%@ variable  name-from-attribute="var" alias="result"
              scope="AT_END"                             %>

<cosmo:baseurl var="baseurl"/>

<c:set var="result"
       value="${baseurl}"/>
