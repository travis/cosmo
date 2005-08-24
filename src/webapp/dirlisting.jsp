<%
String un = request.getParameter("username");
String rt = request.getParameter("rtype");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en-US" xml:lang="en-US">
  <head>
    <title>
      Cosmo | Home Directory
    </title>

    <link rel="stylesheet" type="text/css"
          href="/cosmo.css"/>
    <script type="text/javascript"
            src="/cosmo.js"></script>
  </head>
  <body class="bodystyle">
    <table border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
        <td align="left" valign="top">
          <div class="lg">

            <a href="/"><img src="/cosmo_logo.gif" alt="Cosmo Sharing Server"/></a>
          </div>
        </td>
        
          <td align="right" valign="top">
            <!-- main navbar -->
            <div class="mdData"><%
if (un != "" && un != null) {
	out.print("<a href=\"/dirlisting.jsp?rtype=home&username=" + un + "\">Home Directory</a>");
}
else {
 	out.print("<a href=\"/dirlisting.jsp\">Home Directories</a>");
}                        
%>             |
              
              
              <a href="/logout">Log Out</a>

              |
              <a href="mailto:root@localhost">
                Help
              </a>
            </div>
            <div class="mdData" style="margin-top:8px;">
              Logged in as <strong><%= un %></strong>
            </div>
            <!-- end main navbar -->

          </td>
        
      </tr>
    </table>
    <hr noshade="noshade"/>
    
    <div class="md">
      <!-- page body -->
  
    <div style="margin-top:24px;">
    <table cellpadding="4" cellspacing="1" border="0" width="100%">
      <tr>
        <td class="smTableColHead" style="width:1%;">
          &nbsp;

        </td>
        <td class="smTableColHead">
          Name
        </td>
        <td class="smTableColHead">
          Type
        </td>
        <td class="smTableColHead">
          Created
        </td>

        <td class="smTableColHead">
          Last Modified
        </td>
        <td class="smTableColHead">
          Size
        </td>
        <td class="smTableColHead">
          Description
        </td>
      </tr>
       
       
<%
if ("home".equals(rt)) {
%>       
       
       <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="/dirlisting.jsp?rtype=cal&username=<%= un %>">[browse]</a>    
          </td>
          <td class="smTableData">
            Apple Holiday Calendar
          </td>
          <td class="smTableData" style="text-align:center;">
            Calendar
          </td>
          <td class="smTableData" style="text-align:center;">         
              08-19-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-23-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            <span class="disabled">-</span>
          </td>
          <td class="smTableData">
          
          </td>
        </tr>
        
        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="#">[browse]</a>    
          </td>
          <td class="smTableData">
            bookmarks.xbel.xml
          </td>
          <td class="smTableData" style="text-align:center;">
            File
          </td>
          <td class="smTableData" style="text-align:center;">         
              08-24-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-24-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            2.6K
          </td>
          <td class="smTableData">
          
          </td>
        </tr>
        
       <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="/dirlisting.jsp?rtype=web&username=<%= un %>">[browse]</a>    
          </td>
          <td class="smTableData">
            Cosmo Stuff
          </td>
          <td class="smTableData" style="text-align:center;">
            Folder
          </td>
          <td class="smTableData" style="text-align:center;">         
              07-29-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-15-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            <span class="disabled">-</span>
          </td>
          <td class="smTableData">
          
          </td>
        </tr>

        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="/dirlisting.jsp?rtype=cal&username=<%= un %>">[browse]</a>    
          </td>
          <td class="smTableData">
            Home Calendar
          </td>
          <td class="smTableData" style="text-align:center;">
            Calendar
          </td>
          <td class="smTableData" style="text-align:center;">         
              08-22-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-22-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            <span class="disabled">-</span>
          </td>
          <td class="smTableData">
          
          </td>
        </tr>
        
        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="/dirlisting.jsp?rtype=cal&username=<%= un %>">[browse]</a>    
          </td>
          <td class="smTableData">
            Roommate&#8217;s Calendar
          </td>
          <td class="smTableData" style="text-align:center;">
            Calendar
          </td>
          <td class="smTableData" style="text-align:center;">         
              08-23-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-24-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            <span class="disabled">-</span>
          </td>
          <td class="smTableData">
          
          </td>
        </tr>
        
        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="/dirlisting.jsp?rtype=web&username=<%= un %>">[browse]</a>    
          </td>
          <td class="smTableData">
            Scooby Stuff
          </td>
          <td class="smTableData" style="text-align:center;">
            Folder
          </td>
          <td class="smTableData" style="text-align:center;">         
              08-19-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-19-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            <span class="disabled">-</span>
          </td>
          <td class="smTableData">
          
          </td>
        </tr>
        
        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="/dirlisting.jsp?rtype=cal&username=<%= un %>">[browse]</a>    
          </td>
          <td class="smTableData">
            SF Upcoming Events
          </td>
          <td class="smTableData" style="text-align:center;">
            Calendar
          </td>
          <td class="smTableData" style="text-align:center;">         
              08-15-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-23-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            <span class="disabled">-</span>
          </td>
          <td class="smTableData">
          
          </td>
        </tr>
  
<%
}
else if ("web".equals(rt)) {
%>
        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="#">[browse]</a>    
          </td>
          <td class="smTableData">
            0xff-deadbeefdeadbeef-cafebebe.xml
          </td>
          <td class="smTableData" style="text-align:center;">
            File
          </td>
          <td class="smTableData" style="text-align:center;">         
              08-15-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-22-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            1.4K
          </td>
          <td class="smTableData">
              
          </td>
        </tr>
        
        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="#">[browse]</a>    
          </td>
          <td class="smTableData">
            2112_graphic.jpg
          </td>
          <td class="smTableData" style="text-align:center;">
            File
          </td>
          <td class="smTableData" style="text-align:center;">         
              08-22-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-22-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            69.0K
          </td>
          <td class="smTableData">
              
          </td>
        </tr>
        
        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="#">[browse]</a>    
          </td>
          <td class="smTableData">
            bookmarks.html
          </td>
          <td class="smTableData" style="text-align:center;">
            File
          </td>
          <td class="smTableData" style="text-align:center;">         
              06-08-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-23-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            5.1K
          </td>
          <td class="smTableData">
              
          </td>
        </tr>
        
        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="#">[browse]</a>    
          </td>
          <td class="smTableData">
            index_test.html
          </td>
          <td class="smTableData" style="text-align:center;">
            File
          </td>
          <td class="smTableData" style="text-align:center;">         
              08-08-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-12-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            2.2K
          </td>
          <td class="smTableData">
              
          </td>
        </tr>
<%
}
else if ("cal".equals(rt)) {
%>
	<tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="#">[browse]</a>    
          </td>
          <td class="smTableData">
            staff meeting.ics
          </td>
          <td class="smTableData" style="text-align:center;">
            File
          </td>
          <td class="smTableData" style="text-align:center;">         
              08-20-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-20-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            1.4K
          </td>
          <td class="smTableData">
              Regularly scheduled staff meeting.
          </td>
        </tr>
        
        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="#">[browse]</a>    
          </td>
          <td class="smTableData">
            feed dog.ics
          </td>
          <td class="smTableData" style="text-align:center;">
            File
          </td>
          <td class="smTableData" style="text-align:center;">         
              08-22-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-22-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            1.2K
          </td>
          <td class="smTableData">
              Remember to feed the dog.
          </td>
        </tr>
        
        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="#">[browse]</a>    
          </td>
          <td class="smTableData">
            trip to store.ics
          </td>
          <td class="smTableData" style="text-align:center;">
            File
          </td>
          <td class="smTableData" style="text-align:center;">         
              08-22-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-22-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            1.4K
          </td>
          <td class="smTableData">
              Don&#8217;t forget to buy Little Debby snack cakes.
          </td>
        </tr>
        
        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="#">[browse]</a>    
          </td>
          <td class="smTableData">
            danzig.ics
          </td>
          <td class="smTableData" style="text-align:center;">
            File
          </td>
          <td class="smTableData" style="text-align:center;">         
              08-23-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-23-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            1.4K
          </td>
          <td class="smTableData">
              Danzig concert on Friday.
          </td>
        </tr>
        
        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <a href="#">[browse]</a>    
          </td>
          <td class="smTableData">
            cheese.ics
          </td>
          <td class="smTableData" style="text-align:center;">
            File
          </td>
          <td class="smTableData" style="text-align:center;">         
              08-23-2005
          </td>

          <td class="smTableData" style="text-align:center;">
              08-23-2005
          </td>
          <td class="smTableData" style="text-align:center;">
            1.7K
          </td>
          <td class="smTableData">
              Eat some cheese.
          </td>
        </tr>
<%	
}
%>  
  
      
    </table>
    </div>
  
      <!-- end page body -->
    </div>
    <!-- footer -->
    <img src="/spacer.gif" height="60" width="1" border="0" id="footerSpacer" alt="" />
    <hr noshade="noshade"/>
    <div class="footer">
      <a href="mailto:root@localhost">
        Cosmo sharing server v 0.2-3
      </a>

      
      &nbsp;&nbsp;&nbsp;
      Aug 23, 2005 4:44:46 PM
    </div>
    <script language="JavaScript" type="text/javascript">
      setFoot();
    </script>
    <!-- end footer -->
  </body>
</html>

