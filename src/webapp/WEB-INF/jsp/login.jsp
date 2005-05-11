<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>

<fmt:message var="okButton" key="Login.Button.Ok"/>

<c:if test="${not empty paramValues['login_failed']}">
  <p class="md">
    <span class="error"><fmt:message key="Login.Failed"/></span>
  </p>
</c:if>

<form method="POST" action="j_acegi_security_check">
  <table border="0" cellpadding="0" cellspacing="3">
    <tr>
      <td align="right" valign="middle">
        <span class="md"><fmt:message key="Login.Username"/></span>
      </td>
      <td align="left" valign="middle">
        <span class="md">
          <input type="text" name="j_username" size="16"
                 maxlength="32" class="md"/>
        </span>
      </td>
    </tr>
    <tr>
      <td align="right" valign="middle">
        <span class="md"><fmt:message key="Login.Password"/></span>
      </td>
      <td align="left" valign="middle">
        <span class="md">
          <input type="password" name="j_password" size="16"
                 maxlength="16" class="md"/>
        </span>
      </td>
    </tr>
    <tr>
      <td align="right" valign="middle">
        &nbsp;
      </td>
      <td align="left" valign="middle">
        <span class="md">
          <input type="submit" name="ok" value="${okButton}"
                 class="md"/>
        </span>
      </td>
    </tr>
  </table>
</form>
