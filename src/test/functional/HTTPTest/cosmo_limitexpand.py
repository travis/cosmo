from DAVTest import DAVTest    

class CosmoLimitExpand(DAVTest):
    
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
        cmppath = self.pathBuilder('/cmp/user/cosmo-limitexpandTestAccount%s' % self.appendUser)
        
        #Create testing account        
        bodycreateaccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-limitexpandTestAccount%s</username> \
                                 <password>limitexpand</password> \
                                 <firstName>cosmo-limitexpand</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-limitexpandTestAccount%s@osafoundation.org</email> \
                                 </user>' % (self.appendUser, self.appendUser)
                                 
        #Create account and check status
        self.request('PUT', cmppath, body=bodycreateaccount, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED
        
        # ------- Test Create Calendar ------- #
        
        #Add auth to global headers
        self.headers = self.headerAddAuth("cosmo-limitexpandTestAccount%s" % self.appendUser, "limitexpand")
        
        #Create Calendar on CalDAV server   
        self.calpath = self.pathBuilder('/home/cosmo-limitexpandTestAccount%s/calendar/' % self.appendUser)
        self.request('MKCALENDAR', self.calpath, body=None, headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test Creation of events view ICS ------- #
        
        #Construct headers & body
        puticsheaders = self.headerAdd({'Content-Type' : 'text/calendar'})      
        put1icspath = self.pathBuilder('/home/cosmo-limitexpandTestAccount%s/calendar/1.ics' % self.appendUser)
        put2icspath = self.pathBuilder('/home/cosmo-limitexpandTestAccount%s/calendar/2.ics' % self.appendUser)
        put3icspath = self.pathBuilder('/home/cosmo-limitexpandTestAccount%s/calendar/3.ics' % self.appendUser)
        put4icspath = self.pathBuilder('/home/cosmo-limitexpandTestAccount%s/calendar/4.ics' % self.appendUser)    
        put5icspath = self.pathBuilder('/home/cosmo-limitexpandTestAccount%s/calendar/5.ics' % self.appendUser)
        put6icspath = self.pathBuilder('/home/cosmo-limitexpandTestAccount%s/calendar/6.ics' % self.appendUser)
        put7icspath = self.pathBuilder('/home/cosmo-limitexpandTestAccount%s/calendar/7.ics' % self.appendUser) 
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
        
        # ------- Test 1.xml : time-range query with limit over same range ---------- #
        
        self.testStart('Test 1.xml : time-range query with limit over same range')
        
        #Setup request 
        f = open('files/reports/limitexpand/1.xml')
        report1body = f.read()
        self.request('REPORT', self.calpath, body=report1body, headers=self.headers)
        self.checkStatus(207)
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID:', 'VERSION:2.0', 'BEGIN:VTIMEZONE',
                      'LAST-MODIFIED:','TZID:', 'BEGIN:DAYLIGHT', 'DTSTART:', 'RRULE:', 'TZNAME:EDT',
                      'TZOFFSETFROM', 'TZOFFSETTO', 'END:DAYLIGHT', 'BEGIN:STANDARD', 'DTSTART:', 'RRULE:',
                      'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:', 'END:STANDARD', 'END:VTIMEZONE', 'BEGIN:VEVENT',
                      'DTSTAMP:', 'DTSTART;', 'DURATION:', 'RRULE:', 'SUMMARY:', 'UID:', 'END:VEVENT', 'END:VCALENDAR']
        
        self.verifyDAVResponseItems(['5.ics', '6.ics', '7.ics'], inelement='{DAV:}getetag', positive=[''])
        self.verifyDAVResponseItems(['5.ics', '6.ics', '7.ics'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems)
        
        
        # ------- Test 2.xml : time-range query with limit over different range ---------- #
        
        self.testStart('Test 2.xml : time-range query with limit over different range')
        
        #Setup request 
        f = open('files/reports/limitexpand/2.xml')
        report2body = f.read()
        self.request('REPORT', self.calpath, body=report2body, headers=self.headers)
        self.checkStatus(207)             
                      
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID:', 'VERSION:2.0', 'BEGIN:VTIMEZONE',
                      'LAST-MODIFIED:','TZID:', 'BEGIN:DAYLIGHT', 'DTSTART:', 'RRULE:', 'TZNAME:EDT',
                      'TZOFFSETFROM', 'TZOFFSETTO', 'END:DAYLIGHT', 'BEGIN:STANDARD', 'DTSTART:', 'RRULE:',
                      'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:', 'END:STANDARD', 'END:VTIMEZONE', 'BEGIN:VEVENT',
                      'DTSTAMP:', 'DTSTART;', 'DURATION:', 'RRULE:', 'SUMMARY:', 'UID:', 'END:VEVENT', 'END:VCALENDAR']         
                      
        #Check response elements for each response and verify the calendar-data element has the proper UID for each ics entry    
        self.verifyDAVResponseItems(['5.ics', '6.ics', '7.ics'], inelement='{DAV:}getetag', positive=[''])
        self.verifyDAVResponseItems(['5.ics', '6.ics', '7.ics'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems)
        self.verifyDAVResponseInElement('5.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', negative=['RECURRENCE-ID;'])
        self.verifyDAVResponseInElement('6.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=['RECURRENCE-ID;TZID=US/Eastern:20060104T140000'])
        self.verifyDAVResponseInElement('7.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=['RECURRENCE-ID;RANGE=THISANDFUTURE;TZID=US/Eastern:20060104T180000']) 
        
        # ------- Test 3.xml : time-range query with expand over same range ---------- #
        
        
        #expand-recurrence-set is depricated
        
        #Setup request 
        #f = open('files/reports/limitexpand/3.xml')
        #report3body = f.read()
        #self.request('REPORT', self.calpath, body=report3body, headers=self.headers)
        
        
        
        # ------- Test 4.xml : time-range query with expand over different range ---------- #
        
        #Setup request 
        #f = open('files/reports/limitexpand/4.xml')
        #report4body = f.read()
        #self.request('REPORT', self.calpath, body=report4body, headers=self.headers)
        
        
        
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
    
    cosmolimitexpand = CosmoLimitExpand(host=host, port=port, path=path)
    cosmolimitexpand.debug = debug
    cosmolimitexpand.startRun()
    cosmolimitexpand.end()