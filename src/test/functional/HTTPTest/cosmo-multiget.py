from HTTPTest import HTTPTest    

def info(object, spacing=10, collapse=1):
    """Print methods and doc strings.

    Takes module, class, list, dictionary, or string."""
    methodList = [e for e in dir(object) if callable(getattr(object, e))]
    processFunc = collapse and (lambda s: " ".join(s.split())) or (lambda s: s)
    print "\n".join(["%s %s" %
                     (method.ljust(spacing),
                      processFunc(str(getattr(object, method).__doc__)))
                     for method in methodList])

class CosmoMultiget(HTTPTest):
    
    def startRun(self):
        
        #Set Headers and paths
        
        #Create Headers for CMP
        
        # ------- Test Create Account ------- #
           
        cmpheaders = self.headeradd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headeraddauth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathbuilder('/cmp/user/cosmo-multigetTestAccount')
        
        #Create testing account        
        bodycreateaccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-multigetTestAccount</username> \
                                 <password>cosmo-multiget</password> \
                                 <firstName>cosmo-multiget</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-multigetTestAccount@osafoundation.org</email> \
                                 </user>'
                                 
        #Create account and check status
        self.request('PUT', cmppath, body=bodycreateaccount, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED
        
        # ------- Test Create Calendar ------- #
        
        #Add auth to global headers
        self.headers = self.headeraddauth("cosmo-multigetTestAccount", "cosmo-multiget")
        
        #Create Calendar on CalDAV server   
        calpath = self.pathbuilder('/home/cosmo-multigetTestAccount/calendar/')
        self.request('MKCALENDAR', calpath, body=None, headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test Creation of events view ICS ------- #
        
        #Construct headers & body
        puticsheaders = self.headeradd({'Content-Type' : 'text/calendar'})      
        put1icspath = self.pathbuilder('/home/cosmo-multigetTestAccount/calendar/1.ics')
        put2icspath = self.pathbuilder('/home/cosmo-multigetTestAccount/calendar/2.ics')
        put3icspath = self.pathbuilder('/home/cosmo-multigetTestAccount/calendar/3.ics')
        put4icspath = self.pathbuilder('/home/cosmo-multigetTestAccount/calendar/4.ics')        
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
        
        # ------- Test 1.xml : basic VEVENT, summary "event 1" (tzid=US/Eastern) ------- #
        
        #Setup request 
        f = open('files/reports/multiget/1.xml')
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
        
        #Check response elements for each response and verify the calendar-data element has the proper UID for each ics entry    
        test = self.xml_doc.findall('.//{DAV:}response')
        for t in test:
            if t[0].text.find('1.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                if ctest.text.find('54E181BC7CCC373042B28842@ninevah.local') != -1:
                    icscount = icscount + 1
            elif t[0].text.find('2.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                if ctest.text.find('9A6519F71822CD45840C3440@ninevah.local') != -1:
                    icscount = icscount + 1
            elif t[0].text.find('3.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                if ctest.text.find('UID:DB3F97EF10A051730E2F752E@ninevah.local') != -1:
                    icscount = icscount + 1
            elif t[0].text.find('4.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                if ctest.text.find('UID:A3217B429B4D2FF2DC2EEE66@ninevah.local') != -1:
                    icscount = icscount + 1
        
        #Run through all the elemenet and etag counts and make sure they match 
        if elementcount == 4 & etagcount == 4 & icscount == 4:
            self.report(True, test='multiget/1.xml REPORT return 4 elements & 4 etags & 1,2,3,4.ics', comment=None)
        else:
            self.report(False, test='multiget/1.xml REPORT return 4 elements & 4 etags & 1,2,3,4.ics', comment='Returned %s elements & %s etags %s ics matches' % (elementcount, etagcount, icscount))
        
        # ------- Test 2.xml : basic VEVENT, summary "event 2" (tzid=US/Mountain), has description ------- #
        
        #Setup request 
        f = open('files/reports/multiget/2.xml')
        report2body = f.read()
        self.request('REPORT', calpath, body=report2body, headers=self.headers)
        
        #Set all success counters
        elementcount = 0
        icscount = 0
        etagcount = 0
               
        #Verify correct number of etags
        test = self.xml_doc.findall('.//{DAV:}getetag')
        for t in test:
            etagcount = etagcount + 1         
       
        #Verify correct number of calendar-data elements
        self.xmlparse()
        test = self.xml_doc.findall('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
        for t in test:
            elementcount = elementcount + 1
        
        f = open('files/multiget-2.response')
        vcalprop = f.read()

        #Check response elements for each response and verify the calendar-data element has the proper info
        test = self.xml_doc.findall('.//{DAV:}response')
        for t in test:
            if t[0].text.find('1.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                if ctest.text.find(vcalprop) != -1:
                    icscount = icscount + 1
            if t[0].text.find('2.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                if ctest.text.find(vcalprop) != -1:
                    icscount = icscount + 1
            if t[0].text.find('3.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                if ctest.text.find(vcalprop) != -1:
                    icscount = icscount + 1
            if t[0].text.find('4.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                if ctest.text.find(vcalprop) != -1:
                    icscount = icscount + 1
        
        #Run through all the elemenet and etag counts and make sure they match 
        if elementcount == 4 & icscount == 4 & etagcount == 4:
            self.report(True, test='multiget/2.xml REPORT return 4 caldata elements with just VCAL info', comment=None)
        else:
            self.report(False, test='multiget/2.xml REPORT return 4 caldata elements with just VCAL info', comment='Returned %s elements & %s ics matches' % (elementcount, icscount))
        
        # ------- Test 3.xml : basic VEVENT, summary "event 3" (tzid=US/Pacific) ------- #
        
        #Setup request 
        f = open('files/reports/multiget/3.xml')
        report3body = f.read()
        self.request('REPORT', calpath, body=report3body, headers=self.headers)
        
        #Set all success counters
        elementcount = 0
        icscount = 0
        etagcount = 0
        
        #Verify correct number of etags
        test = self.xml_doc.findall('.//{DAV:}getetag')
        for t in test:
            etagcount = etagcount + 1  
        
        #Verify correct number of calendar-data elements
        self.xmlparse()
        test = self.xml_doc.findall('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
        for t in test:
            elementcount = elementcount + 1
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                      'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'END:VCALENDAR']
                      
        vcalnegative = ['BEGIN:VEVENT', 'SUMMARY', 'END:VEVENT']
        
        #Check response elements for each response and verify the calendar-data element has the proper info
        test = self.xml_doc.findall('.//{DAV:}response')
        for t in test:
            if t[0].text.find('.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                icscount = icscount + 1
                for x in vcalitems:
                    if ctest.text.find(x) == -1:
                        icscount = icscount - 1
                for x in vcalnegative:
                    if ctest.text.find(x) != -1:
                        icscount = icscount - 1                   
                
                
        
        #Run through all the elemenet and etag counts and make sure they match
        if elementcount == 4 & icscount == 4 & etagcount == 4:
            self.report(True, test='multiget/3.xml REPORT return 4 caldata elements with just VTIMEZONE info', comment=None)
        else:
            self.report(False, test='multiget/3.xml REPORT return 4 caldata elements with just VTIMEZONE info', comment='Returned %s elements & %s ics matches' % (elementcount, icscount))
        
        # ------- Test 4.xml : basic VEVENT with VALARM, summary "event 4" ------- #
        
        #Setup request 
        f = open('files/reports/multiget/4.xml')
        report4body = f.read()
        self.request('REPORT', calpath, body=report4body, headers=self.headers)
        
        #Set all success counters
        elementcount = 0
        icscount = 0
        etagcount = 0
        summarycount = 0
        
        #Verify correct number of etags
        test = self.xml_doc.findall('.//{DAV:}getetag')
        for t in test:
            etagcount = etagcount + 1  
        
        #Verify correct number of calendar-data elements
        self.xmlparse()
        test = self.xml_doc.findall('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
        for t in test:
            elementcount = elementcount + 1
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VEVENT', 'SUMMARY:', 'UID', 'END:VEVENT', 'END:VCALENDAR']
                      
        vcalnegative = ['BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:', 
                      'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE']
        
        #Check response elements for each response and verify the calendar-data element has the proper info
        test = self.xml_doc.findall('.//{DAV:}response')
        
        for t in test:
            if t[0].text.find('1.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                if ctest.text.find('SUMMARY:event 1') != -1:
                    summarycount = summarycount + 1
            if t[0].text.find('2.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                if ctest.text.find('SUMMARY:event 2') != -1:
                    summarycount = summarycount + 1
            if t[0].text.find('3.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                if ctest.text.find('SUMMARY:event 3') != -1:
                    summarycount = summarycount + 1
            if t[0].text.find('4.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                if ctest.text.find('SUMMARY:event 4') != -1:
                    summarycount = summarycount + 1
        
        for t in test:
            if t[0].text.find('.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                icscount = icscount + 1
                for x in vcalitems:
                    if ctest.text.find(x) == -1:
                        icscount = icscount - 1
                for x in vcalnegative:
                    if ctest.text.find(x) != -1:
                        icscount = icscount - 1
                
                
        #Run through all the elemenet and etag counts and make sure they match
        if elementcount == 4 & icscount == 4 & etagcount == 4 & summarycount == 4:
            self.report(True, test='multiget/4.xml REPORT return 4 caldata elements with just VEVENT & SUMMARY/UID info', comment=None)
        else:
            self.report(False, test='multiget/4.xml REPORT return 4 caldata elements with just VEVENT & SUMMARY/UID info', comment='Returned %s elements & %s ics matches & %s summarycount' % (elementcount, icscount, summarycount))
        
        
        # ------- Test 5.xml : recurring VEVENT (5 consecutive days), summary "event 5" ------- #
        
        f = open('files/reports/multiget/5.xml')
        report5body = f.read()
        
        self.request('REPORT', calpath, body=report5body, headers=self.headers)
        
        #Set all success counters
        elementcount = 0
        icscount = 0
        etagcount = 0
        
        #Verify correct number of etags
        test = self.xml_doc.findall('.//{DAV:}getetag')
        for t in test:
            etagcount = etagcount + 1  
        
        #Verify correct number of calendar-data elements
        self.xmlparse()
        test = self.xml_doc.findall('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
        for t in test:
            elementcount = elementcount + 1
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VEVENT', 'SUMMARY:', 'UID', 'END:VEVENT', 'END:VCALENDAR']
                      
        vcalnegative = ['BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:', 
                      'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'SUMMARY:event']
        
        #Check response elements for each response and verify the calendar-data element has the proper info
        test = self.xml_doc.findall('.//{DAV:}response')
        
        for t in test:
            if t[0].text.find('.ics') != -1:
                ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
                icscount = icscount + 1
                for x in vcalitems:
                    if ctest.text.find(x) == -1:
                        icscount = icscount - 1
                for x in vcalnegative:
                    if ctest.text.find(x) != -1:
                        icscount = icscount - 1
                
                
        #Run through all the elemenet and etag counts and make sure they match
        if elementcount == 4 & icscount == 4 & etagcount == 4:
            self.report(True, test='multiget/5.xml REPORT return 4 caldata elements with just VEVENT & SUMMARY-no event & UID info', comment=None)
        else:
            self.report(False, test='multiget/5.xml REPORT return 4 caldata elements with just VEVENT & SUMMARY-no event & UID info', comment='Returned %s elements & %s ics matches' % (elementcount, icscount))
        
        
        
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
    
    print 'Creating instance of XML_Test class'
    cosmomultiget = CosmoMultiget(host=host, port=port, path=path)
    print 'instance created, start run begin'
    cosmomultiget.debug = debug
    cosmomultiget.startRun()
    cosmomultiget.end()
    
    