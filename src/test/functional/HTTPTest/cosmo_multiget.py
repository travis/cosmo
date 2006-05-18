from DAVTest import DAVTest    

class CosmoMultiget(DAVTest):
    
    def startRun(self):
        
        self.testStart('Setup Accounts')
        
        try:
            self.appendUser = self.appendDict['username']
        except KeyError:
            self.appendUser = ''
            
        # ------- Test Create Account ------- #
           
        cmpheaders = self.headerAdd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headerAddAuth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathBuilder('/cmp/user/cosmo-multigetTestAccount%s' % self.appendUser)
        
        #Create testing account        
        bodycreateaccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-multigetTestAccount%s</username> \
                                 <password>cosmo-multiget</password> \
                                 <firstName>cosmo-multiget</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-multigetTestAccount%s@osafoundation.org</email> \
                                 </user>' % (self.appendUser, self.appendUser)
                                 
        #Create account and check status
        self.request('PUT', cmppath, body=bodycreateaccount, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED
        
        # ------- Test Create Calendar ------- #
        
        #Add auth to global headers
        self.headers = self.headerAddAuth("cosmo-multigetTestAccount%s" % self.appendUser, "cosmo-multiget")
        
        #Create Calendar on CalDAV server   
        self.calpath = self.pathBuilder('/home/cosmo-multigetTestAccount%s/calendar/' % self.appendUser)
        self.request('MKCALENDAR', self.calpath, body=None, headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test Creation of events view ICS ------- #
        
        #Construct headers & body
        puticsheaders = self.headerAdd({'Content-Type' : 'text/calendar'})      
        put1icspath = self.pathBuilder('/home/cosmo-multigetTestAccount%s/calendar/1.ics' % self.appendUser)
        put2icspath = self.pathBuilder('/home/cosmo-multigetTestAccount%s/calendar/2.ics' % self.appendUser)
        put3icspath = self.pathBuilder('/home/cosmo-multigetTestAccount%s/calendar/3.ics' % self.appendUser)
        put4icspath = self.pathBuilder('/home/cosmo-multigetTestAccount%s/calendar/4.ics' % self.appendUser)        
        f = open("files/reports/put/1.ics")
        put1icsbody = f.read()
        f = open("files/reports/put/2.ics")
        put2icsbody = f.read()
        f = open("files/reports/put/3.ics")
        put3icsbody = f.read()
        f = open("files/reports/put/4.ics")
        put4icsbody = f.read()
        
        #Send request and check status
        self.request('PUT', put1icspath, body=put1icsbody, headers=puticsheaders)
        self.checkStatus(201)
        self.request('PUT', put2icspath, body=put2icsbody, headers=puticsheaders)
        self.checkStatus(201)
        self.request('PUT', put3icspath, body=put3icsbody, headers=puticsheaders)
        self.checkStatus(201)
        self.request('PUT', put4icspath, body=put4icsbody, headers=puticsheaders)
        self.checkStatus(201)
        
    def recurringRun(self):
        
        # ------- Test 1.xml : basic VEVENT, summary "event 1" (tzid=US/Eastern) ------- #
        
        self.testStart('Test 1.xml : basic VEVENT, summary "event 1" (tzid=US/Eastern)')
        
        #Setup request 
        f = open('files/reports/multiget/1.xml')
        report1body = f.read()
        report1body = report1body.replace('multigetTestAccount','multigetTestAccount%s'%self.appendUser)
        self.request('REPORT', self.calpath, body=report1body, headers=self.headers)
        self.checkStatus(207)
        
        #Verify correct items in response
        self.verifyDAVResponseItems(['1.ics', '2.ics', '3.ics', '4.ics'], inelement='{DAV:}getetag', positive=[''])
        self.verifyDAVResponseItems(['1.ics', '2.ics', '3.ics', '4.ics'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', positive=['UID'])
        self.verifyDAVResponseInElement('1.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=['UID:54E181BC7CCC373042B28842@ninevah.local'])
        self.verifyDAVResponseInElement('2.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=['UID:9A6519F71822CD45840C3440@ninevah.local'])
        self.verifyDAVResponseInElement('3.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=['UID:DB3F97EF10A051730E2F752E@ninevah.local'])
        self.verifyDAVResponseInElement('4.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=['UID:A3217B429B4D2FF2DC2EEE66@ninevah.local'])
        
          
        # ------- Test 2.xml : basic multiget of 4 resources returning etag and only VCALENDAR property data (no embedded components) ------- #
        
        self.testStart('Test 2.xml : basic multiget of 4 resources returning etag and only VCALENDAR property data (no embedded components)')
        
        #Setup request 
        f = open('files/reports/multiget/2.xml')
        report2body = f.read()
        report2body = report2body.replace('multigetTestAccount','multigetTestAccount%s'%self.appendUser)
        self.request('REPORT', self.calpath, body=report2body, headers=self.headers)
        self.checkStatus(207)
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID:-//Cyrusoft International\, Inc.//Mulberry v4.0//EN', 'VERSION:2.0', 'END:VCALENDAR']
        
        self.verifyDAVResponseItems(['1.ics', '2.ics', '3.ics', '4.ics'], inelement='{DAV:}getetag', positive=[''])
        self.verifyDAVResponseItems(['1.ics', '2.ics', '3.ics', '4.ics'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems)
        
        # ------- Test 3.xml : basic multiget of 4 resources returning etag and only VTIMEZONE components ------- #
        
        self.testStart('Test 3.xml : basic multiget of 4 resources returning etag and only VTIMEZONE components')
        
        #Setup request 
        f = open('files/reports/multiget/3.xml')
        report3body = f.read()
        report3body = report3body.replace('multigetTestAccount','multigetTestAccount%s'%self.appendUser)
        self.request('REPORT', self.calpath, body=report3body, headers=self.headers)
        self.checkStatus(207)
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                      'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'END:VCALENDAR']
                      
        vcalnegative = ['BEGIN:VEVENT', 'SUMMARY', 'END:VEVENT']
        
        self.verifyDAVResponseItems(['1.ics', '2.ics', '3.ics', '4.ics'], inelement='{DAV:}getetag', positive=[''])
        self.verifyDAVResponseItems(['1.ics', '2.ics', '3.ics', '4.ics'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems, negative=vcalnegative)
                
        # ------- Test 4.xml : basic multiget of 4 resources returning etag and only SUMMARY/UID properties inside VEVENT components and VALARMs ------- #
        
        self.testStart('Test 4.xml : basic multiget of 4 resources returning etag and only SUMMARY/UID properties inside VEVENT components and VALARMs')
        
        #Setup request 
        f = open('files/reports/multiget/4.xml')
        report4body = f.read()
        report4body = report4body.replace('multigetTestAccount','multigetTestAccount%s'%self.appendUser)
        self.request('REPORT', self.calpath, body=report4body, headers=self.headers)
        self.checkStatus(207)
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VEVENT', 'SUMMARY:', 'UID', 'END:VEVENT', 'END:VCALENDAR']
                      
        vcalnegative = ['BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:', 
                      'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE']
                      
        self.verifyDAVResponseItems(['1.ics', '2.ics', '3.ics', '4.ics'], inelement='{DAV:}getetag', positive=[''])
        self.verifyDAVResponseItems(['1.ics', '2.ics', '3.ics', '4.ics'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems, negative=vcalnegative)
        self.verifyDAVResponseInElement('1.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=['SUMMARY:event 1'])
        self.verifyDAVResponseInElement('2.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=['SUMMARY:event 2'])
        self.verifyDAVResponseInElement('3.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=['SUMMARY:event 3'])
        self.verifyDAVResponseInElement('4.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=['SUMMARY:event 4'])
          
        # ------- Test 5.xml : has 4.txt except that the SUMMARY property value is not returned ------- #
        
        self.testStart('Test 5.xml : has 4.txt except that the SUMMARY property value is not returned')
        
        f = open('files/reports/multiget/5.xml')
        report5body = f.read()
        report5body = report5body.replace('multigetTestAccount','multigetTestAccount%s'%self.appendUser)
        self.request('REPORT', self.calpath, body=report5body, headers=self.headers)
        self.checkStatus(207)
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VEVENT', 'SUMMARY:', 'UID', 'END:VEVENT', 'END:VCALENDAR']
                      
        vcalnegative = ['BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:', 
                      'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'SUMMARY:event']
                      
        self.verifyDAVResponseItems(['1.ics', '2.ics', '3.ics', '4.ics'], inelement='{DAV:}getetag', positive=[''])
        self.verifyDAVResponseItems(['1.ics', '2.ics', '3.ics', '4.ics'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems, negative=vcalnegative)
        self.verifyDAVResponseInElement('1.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', negative=['SUMMARY:event 1'])
        self.verifyDAVResponseInElement('2.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', negative=['SUMMARY:event 2'])
        self.verifyDAVResponseInElement('3.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', negative=['SUMMARY:event 3'])
        self.verifyDAVResponseInElement('4.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', negative=['SUMMARY:event 4'])
        
        
if __name__ == "__main__":
    
    import sys
    
    host = 'localhost'
    port = '8080'
    path = '/cosmo'
    debug = 0
    counter = 10
    
    for arg in sys.argv:
        args = arg.split("=")
        if args[0] == "host":
            host = args[1]
        elif args[0] == "port":
            port = int(args[1])
        elif args[0] == "path":
            path = args[1]
        elif args[0] == "recurring":
            counter = int(args[1])
        elif args[0] == "debug":
            debug = int(args[1])
        
    print "host %s port %s recurring %s path %s" % (host, port, counter, path)
    cosmomultiget = CosmoMultiget(host=host, port=port, path=path, debug=debug)
    cosmomultiget.fullRun()
    
    