from DAVTest import DAVTest

class CosmoBasicQuery(DAVTest):
    
    def startRun(self):
        
        #Set Headers and paths
        
        #Create Headers for CMP
        
        # ------- Test Create Account ------- #
        
        self.testStart('Setup Accounts')
           
        cmpheaders = self.headerAdd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headerAddAuth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathBuilder('/cmp/user/cosmo-basicqueryTestAccount%s' % self.appendVar)
        
        #Create testing account        
        bodycreateaccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-basicqueryTestAccount%s</username> \
                                 <password>cosmo-basicquery</password> \
                                 <firstName>cosmo-basicquery%s</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-basicqueryTestAccount%s@osafoundation.org</email> \
                                 </user>' % (self.appendVar, self.appendVar, self.appendVar)
                                 
        #Create account and check status
        self.request('PUT', cmppath, body=bodycreateaccount, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED
        
        # ------- Test Create Calendar ------- #
        
        #Add auth to global headers
        self.headers = self.headerAddAuth("cosmo-basicqueryTestAccount%s" % self.appendVar, "cosmo-basicquery")
        
        #Create Calendar on CalDAV server   
        self.calpath = self.pathBuilder('/home/cosmo-basicqueryTestAccount%s/calendar/' % self.appendVar)
        self.request('MKCALENDAR', self.calpath, body=None, headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test Creation of events view ICS ------- #
        
        #Construct headers & body
        puticsheaders = self.headerAdd({'Content-Type' : 'text/calendar'})      
        put1icspath = self.pathBuilder('/home/cosmo-basicqueryTestAccount%s/calendar/1.ics' % self.appendVar)
        put2icspath = self.pathBuilder('/home/cosmo-basicqueryTestAccount%s/calendar/2.ics' % self.appendVar)
        put3icspath = self.pathBuilder('/home/cosmo-basicqueryTestAccount%s/calendar/3.ics' % self.appendVar)
        put4icspath = self.pathBuilder('/home/cosmo-basicqueryTestAccount%s/calendar/4.ics' % self.appendVar)    
        put5icspath = self.pathBuilder('/home/cosmo-basicqueryTestAccount%s/calendar/5.ics' % self.appendVar)
        put6icspath = self.pathBuilder('/home/cosmo-basicqueryTestAccount%s/calendar/6.ics' % self.appendVar)
        put7icspath = self.pathBuilder('/home/cosmo-basicqueryTestAccount%s/calendar/7.ics' % self.appendVar) 
        f = open("files/reports/put/1.ics")
        put1icsbody = f.read()
        f = open("files/reports/put/2.ics")
        put2icsbody = f.read()
        f = open("files/reports/put/3.ics")
        put3icsbody = f.read()
        f = open("files/reports/put/4.ics")
        put4icsbody = f.read()
        f = open("files/reports/put/5.ics")
        put5icsbody = f.read()
        f = open("files/reports/put/6.ics")
        put6icsbody = f.read()
        f = open("files/reports/put/7.ics")
        put7icsbody = f.read()
        
        self.request('PUT', put1icspath, body=put1icsbody, headers=puticsheaders)
        self.checkStatus(201)
        self.request('PUT', put2icspath, body=put2icsbody, headers=puticsheaders)
        self.checkStatus(201)
        self.request('PUT', put3icspath, body=put3icsbody, headers=puticsheaders)
        self.checkStatus(201)
        self.request('PUT', put4icspath, body=put4icsbody, headers=puticsheaders)
        self.checkStatus(201)
        self.request('PUT', put5icspath, body=put5icsbody, headers=puticsheaders)
        self.checkStatus(201)
        self.request('PUT', put6icspath, body=put6icsbody, headers=puticsheaders)
        self.checkStatus(201)
        self.request('PUT', put7icspath, body=put7icsbody, headers=puticsheaders)
        self.checkStatus(201)
        
        
    def recurringRun(self):
        
        # ------- Test 1.xml : query for resources with VCALENDAR & VEVENT defined ---------- #
        
        self.testStart('Test 1.xml : query for resources with VCALENDAR & VEVENT defined')
        
        #Setup request 
        f = open('files/reports/basicquery/1.xml')
        report1body = f.read()
        self.request('REPORT', self.calpath, body=report1body, headers=self.headers)
        self.checkStatus(207) # Verify multi-status
            
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                      'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'END:VCALENDAR',
                      'BEGIN:VEVENT', 'SUMMARY:event', 'END:VEVENT']
        
        self.verifyDAVResponseItems(['1.ics', '2.ics', '3.ics', '4.ics', '5.ics', '6.ics', '7.ics'], inelement='{DAV:}getetag', positive=[''])
        self.verifyDAVResponseItems(['1.ics', '2.ics', '3.ics', '4.ics', '5.ics', '6.ics', '7.ics'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems)
          
        # ------- Test 2.xml : query for resources where the SUMMARY in a VEVENT contains the character '1' -------- #
        
        self.testStart('Test 2.xml : query for resources where the SUMMARY in a VEVENT contains the character 1')
        
        #Setup request 
        f = open('files/reports/basicquery/2.xml')
        report2body = f.read()
        self.request('REPORT', self.calpath, body=report2body, headers=self.headers)
        self.checkStatus(207) # Verify multi-status
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                      'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'END:VCALENDAR',
                      'BEGIN:VEVENT', 'SUMMARY:event 1', 'END:VEVENT']
                      
        self.verifyDAVResponseItems(['1.ics'], inelement='{DAV:}getetag', positive=[''])
        self.verifyDAVResponseItems(['1.ics'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems)
                              
        # ------- Test 3.xml : query for resources where the DESCRIPTION property exists in a VEVENT ---------- #
        
        self.testStart('Test 3.xml : query for resources where the DESCRIPTION property exists in a VEVENT')
        
        #Setup request 
        f = open('files/reports/basicquery/3.xml')
        report3body = f.read()
        self.request('REPORT', self.calpath, body=report3body, headers=self.headers)
        self.checkStatus(207) # Verify multi-status
            
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                      'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'END:VCALENDAR',
                      'BEGIN:VEVENT', 'SUMMARY', 'END:VEVENT', 'DESCRIPTION:']
                      
        self.verifyDAVResponseItems(['2.ics', '6.ics', '7.ics'], inelement='{DAV:}getetag', positive=[''])
        self.verifyDAVResponseItems(['2.ics', '6.ics', '7.ics'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems)      
        
        # ------- Test 4.xml : query for resources that have a DTSTART in a VEVENT that contains a TZID parameter containing the text 'Paci' --- #
        
        self.testStart('Test 4.xml : query for resources that have a DTSTART in a VEVENT that contains a TZID parameter containing the text Paci')
        
        #Setup request 
        f = open('files/reports/basicquery/4.xml')
        report4body = f.read()
        self.request('REPORT', self.calpath, body=report4body, headers=self.headers)
        self.checkStatus(207) # Verify multi-status
        
        ### Blocked by bug 5551
        
        # ------- Test 5.xml : query for resources that have a DTSTART in a VEVENT that contains a TZID parameter containing the text 'Paci' or 'Moun' ---- #
        
        self.testStart('Test 5.xml : query for resources that have a DTSTART in a VEVENT that contains a TZID parameter containing the text Paci or Moun')
        
        #Setup request 
        f = open('files/reports/basicquery/5.xml')
        report5body = f.read()
        self.request('REPORT', self.calpath, body=report5body, headers=self.headers)
        self.checkStatus(207) # Verify multi-status
        
        ### Blocked by bug 5551
        
        # ------- Test 6.xml : query for resources where the SUMMARY in a VEVENT contains the character '4' or has a DTSTART in a VEVENT that contains a TZID parameter containing the text 'East' ----- #
        
        self.testStart('Test 6.xml : query for resources where the SUMMARY in a VEVENT contains the character 4 or has a DTSTART in a VEVENT that contains a TZID parameter containing the text East')
        
        #Setup request 
        f = open('files/reports/basicquery/6.xml')
        report6body = f.read()
        self.request('REPORT', self.calpath, body=report6body, headers=self.headers)
        self.checkStatus(207) # Verify multi-status
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                      'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'END:VCALENDAR',
                      'BEGIN:VEVENT', 'SUMMARY', 'END:VEVENT', 'DESCRIPTION:']
        
        self.verifyDAVResponseItems(['2.ics'], inelement='{DAV:}getetag', positive=[''])
        self.verifyDAVResponseItems(['2.ics'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems)

        
        
if __name__ == "__main__":
    
    import sys
    
    host = 'localhost'
    port = '8080'
    path = '/cosmo'
    debug = 0
    recurrence = 1
    
    for arg in sys.argv:
        args = arg.split("=")
        if args[0] == "host":
            host = args[1]
        elif args[0] == "port":
            port = int(args[1])
        elif args[0] == "path":
            path = args[1]
        elif args[0] == "recurrence":
            recurrence = int(args[1])
        elif args[0] == "debug":
            debug = int(args[1])
        
    print "host %s port %s recurring %s path %s" % (host, port, recurrence, path)
    
    cosmobasicquery = CosmoBasicQuery(host=host, port=port, path=path, recurrence=recurrence, debug=debug)
    cosmobasicquery.fullRun()
    