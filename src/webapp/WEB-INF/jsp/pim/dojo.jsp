<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>
<cosmo:staticbaseurl var="staticBaseUrl"/>

<script type="text/javascript">
    var djConfig = {isDebug: true};
</script>

<script type="text/javascript" src="${staticBaseUrl}/js/lib/dojo-event_and_io/dojo.js.uncompressed.js"></script>
<script type="text/javascript">
{
    var staticBaseUrl = "${staticBaseUrl}";
    dojo.setModulePrefix("scooby", "../../cosmo"); // path is relative to dojo root

    dojo.require("dojo.widget.*");
    dojo.require("scooby.ui.widget.Debug");
    dojo.require("scooby.env");
    scooby.env.setBaseUrl("${staticBaseUrl}");
    dojo.widget.manager.registerWidgetPackage("scooby.ui.widget");
    
}
    
</script>


