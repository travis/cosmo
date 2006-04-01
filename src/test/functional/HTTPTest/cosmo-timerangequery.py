from HTTPTest import HTTPTest    

class CosmoTimeRangeQuery(HTTPTest):
    
    def startRun(self):
        
        #Set Headers and paths
        
        #Create Headers for CMP
        
        # ------- Test Create Account ------- #
           
        cmpheaders = self.headeradd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headeraddauth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathbuilder('/cmp/user/cosmo-timerangequeryTestAccount')
        
        #Create testing account        
        bodycreateaccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-timerangequeryTestAccount</username> \
                                 <password>cosmo-timerange</password> \
                                 <firstName>cosmo-timerangequery</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-timerangequeryTestAccount@osafoundation.org</email> \
                                 </user>'
                                 
        #Create account and check status
        self.request('PUT', cmppath, body=bodycreateaccount, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED
        
        # ------- Test Create Calendar ------- #
        
        #Add auth to global headers
        self.headers = self.headeraddauth("cosmo-timerangequeryTestAccount", "cosmo-timerange")
        
        #Create Calendar on CalDAV server   
        calpath = self.pathbuilder('/home/cosmo-timerangequeryTestAccount/calendar/')
        self.request('MKCALENDAR', calpath, body=None, headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test Creation of events view ICS ------- #
        
        #Construct headers & body
        puticsheaders = self.headeradd({'Content-Type' : 'text/calendar'})      
        put1icspath = self.pathbuilder('/home/cosmo-timerangequeryTestAccount/calendar/1.ics')
        put2icspath = self.pathbuilder('/home/cosmo-timerangequeryTestAccount/calendar/2.ics')
        put3icspath = self.pathbuilder('/home/cosmo-timerangequeryTestAccount/calendar/3.ics')
        put4icspath = self.pathbuilder('/home/cosmo-timerangequeryTestAccount/calendar/4.ics')    
        put5icspath = self.pathbuilder('/home/cosmo-timerangequeryTestAccount/calendar/5.ics')
        put6icspath = self.pathbuilder('/home/cosmo-timerangequeryTestAccount/calendar/6.ics')
        put7icspath = self.pathbuilder('/home/cosmo-timerangequeryTestAccount/calendar/7.ics') 
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
        
        # --------- Test 1.xml
        
        #Setup request 
        f = open('files/reports/timerangequery/1.xml')
        report1body = f.read()
        self.request('REPORT', calpath, body=report1body, headers=self.headers)
        
        #Set all success counters
        elementcount = 0
        etagcount = 0
        icscount = 0
        
        #Verify correct number of calendar-data elements
        self.xmlparse()
        test = self.xml_doc.findall('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
        for t in test:
            elementcount = elementcount + 1
        
        #Verify correct number of etags
        test = self.xml_doc.findall('.//{DAV:}getetag')
        for t in test:
            etagcount = etagcount + 1  
            
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                      'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'END:VCALENDAR',
                      'BEGIN:VEVENT', 'SUMMARY:event', 'END:VEVENT']
        
        #Check response elements for each response and verify the calendar-data element has the proper info
        test = self.xml_doc.findall('.//{DAV:}response')
        for t in test:
            if t[0].text.find('.ics') != -1:
                if t[0].text.find('5.ics') != -1 & t[0].text.find('6.ics') != -1 & t[0].text.find('7.ics') != -1:
                    print 'FAILED - element is not 5,6, or 7.ics'
                    icscount = icscount -1
                    break
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                icscount = icscount + 1
                for x in vcalitems:
                    if ctest.text.find(x) == -1:
                        print 'FAILED to get %s in %s' % (x, t[0].text)
                        icscount = icscount - 100
                        
                
                        
        #Run through all the elemenet and etag counts and make sure they match
        if elementcount == 3 & icscount == 3 & etagcount == 3:
            self.report(True, test='report/basicquery/1.xml REPORT return 3 caldata elements query for VEVENTs within time range', comment=None)
        else:
            self.report(False, test='report/basicquery/1.xml REPORT return 3 caldata elements query for VEVENTs within time range', comment='Returned %s elements & %s ics matches' % (elementcount, icscount))
        
        # --------- Test 2.xml
        
        #Setup request 
        f = open('files/reports/timerangequery/2.xml')
        report2body = f.read()
        self.request('REPORT', calpath, body=report2body, headers=self.headers)
        
        #Set all success counters
        elementcount = 0
        etagcount = 0
        icscount = 0
        
        #Verify correct number of calendar-data elements
        self.xmlparse()
        test = self.xml_doc.findall('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
        for t in test:
            elementcount = elementcount + 1
        
        #Verify correct number of etags
        test = self.xml_doc.findall('.//{DAV:}getetag')
        for t in test:
            etagcount = etagcount + 1  
            
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                      'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'END:VCALENDAR',
                      'BEGIN:VEVENT', 'SUMMARY:event', 'END:VEVENT']
        
        #Check response elements for each response and verify the calendar-data element has the proper info
        test = self.xml_doc.findall('.//{DAV:}response')
        for t in test:
            if t[0].text.find('.ics') != -1:
                if t[0].text.find('1.ics') != -1 & t[0].text.find('2.ics') != -1 & t[0].text.find('3.ics') != -1 & t[0].text.find('4.ics'):
                    print 'FAILED - element is not 1,2,3 or 4.ics'
                    icscount = icscount -1
                    break
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                icscount = icscount + 1
                for x in vcalitems:
                    if ctest.text.find(x) == -1:
                        print 'FAILED to get %s in %s' % (x, t[0].text)
                        icscount = icscount - 100
                        
                
                        
        #Run through all the elemenet and etag counts and make sure they match
        if elementcount == 4 & icscount == 4 & etagcount == 4:
            self.report(True, test='report/basicquery/2.xml REPORT return 4 caldata elements query for VEVENTs that have a DTSTART within time range', comment=None)
        else:
            self.report(False, test='report/basicquery/2.xml REPORT return 4 caldata elements query for VEVENTs that have a DTSTART within time range', comment='Returned %s elements & %s ics matches' % (elementcount, icscount))
 
        
        # --------- Test 3.xml
        
        #Setup request 
        f = open('files/reports/timerangequery/3.xml')
        report3body = f.read()
        self.request('REPORT', calpath, body=report3body, headers=self.headers)
        
        #Set all success counters
        elementcount = 0
        etagcount = 0
        icscount = 0
        
        #Verify correct number of calendar-data elements
        self.xmlparse()
        test = self.xml_doc.findall('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
        for t in test:
            elementcount = elementcount + 1
        
        #Verify correct number of etags
        test = self.xml_doc.findall('.//{DAV:}getetag')
        for t in test:
            etagcount = etagcount + 1  
            
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                      'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'END:VCALENDAR',
                      'BEGIN:VEVENT', 'SUMMARY:event', 'END:VEVENT']
        
        #Check response elements for each response and verify the calendar-data element has the proper info
        test = self.xml_doc.findall('.//{DAV:}response')
        for t in test:
            if t[0].text.find('4.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                icscount = icscount + 1
                for x in vcalitems:
                    if ctest.text.find(x) == -1:
                        print 'FAILED to get %s in %s' % (x, t[0].text)
                        icscount = icscount - 100
                        
        #Run through all the elemenet and etag counts and make sure they match
        if elementcount == 1 & icscount == 1 & etagcount == 1:
            self.report(True, test='report/basicquery/3.xml REPORT return 1 caldata elements query for VALARMS within time range', comment=None)
        else:
            self.report(False, test='report/basicquery/3.xml REPORT return 1 caldata elements query for VALARMS within time range', comment='Returned %s elements & %s ics matches' % (elementcount, icscount))
        
        # --------- Test 4.xml
        
        #Setup request 
        f = open('files/reports/timerangequery/4.xml')
        report4body = f.read()
        self.request('REPORT', calpath, body=report4body, headers=self.headers)
        
        ##Blocked by bug 5551
        
        # --------- Test 5.xml
        
        #Setup request 
        f = open('files/reports/timerangequery/5.xml')
        report5body = f.read()
        self.request('REPORT', calpath, body=report5body, headers=self.headers)
        
        ##Blocked by bug 5551
        
        
        
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
    
    cosmotimerangequery = CosmoTimeRangeQuery(host=host, port=port, path=path)
    cosmotimerangequery.debug = debug
    cosmotimerangequery.startRun()
    cosmotimerangequery.end()