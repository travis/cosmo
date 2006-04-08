#from HTTPTest import HTTPTest    
from DAVTest import DAVTest

class CosmoTimeRangeQuery(DAVTest):
    
    def startRun(self):
        
        #Set Headers and paths
        
        #Create Headers for CMP
        
        # ------- Test Create Account ------- #
        
        self.testStart('Setup Account')
           
        cmpheaders = self.headerAdd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headerAddAuth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathBuilder('/cmp/user/cosmo-timerangequeryTestAccount')
        
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
        
        self.testStart('Create Calendar')
        
        #Add auth to global headers
        self.headers = self.headerAddAuth("cosmo-timerangequeryTestAccount", "cosmo-timerange")
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-timerangequeryTestAccount/calendar/')
        self.request('MKCALENDAR', calpath, body=None, headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test Creation of events view ICS ------- #
        
        self.testStart('Put 1-7.ics')
        
        #Construct headers & body
        puticsheaders = self.headerAdd({'Content-Type' : 'text/calendar'})      
        put1icspath = self.pathBuilder('/home/cosmo-timerangequeryTestAccount/calendar/1.ics')
        put2icspath = self.pathBuilder('/home/cosmo-timerangequeryTestAccount/calendar/2.ics')
        put3icspath = self.pathBuilder('/home/cosmo-timerangequeryTestAccount/calendar/3.ics')
        put4icspath = self.pathBuilder('/home/cosmo-timerangequeryTestAccount/calendar/4.ics')    
        put5icspath = self.pathBuilder('/home/cosmo-timerangequeryTestAccount/calendar/5.ics')
        put6icspath = self.pathBuilder('/home/cosmo-timerangequeryTestAccount/calendar/6.ics')
        put7icspath = self.pathBuilder('/home/cosmo-timerangequeryTestAccount/calendar/7.ics') 
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
        
        # --------- Test 1.xml query for VEVENTs within time range
        
        self.testStart('Test 1.xml query for VEVENTs within time range')
        
        #Setup request 
        f = open('files/reports/timerangequery/1.xml')
        report1body = f.read()
        self.request('REPORT', calpath, body=report1body, headers=self.headers)
            
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                      'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'END:VCALENDAR',
                      'BEGIN:VEVENT', 'SUMMARY:event', 'END:VEVENT']
                      
        self.verifyItems(['5.ics', '6.ics', '7.ics'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems)
        
        # --------- Test 2.xml query for VEVENTs that have a DTSTART within time range
        
        self.testStart('Test 2.xml query for VEVENTs that have a DTSTART within time range')
        
        #Setup request 
        f = open('files/reports/timerangequery/2.xml')
        report2body = f.read()
        self.request('REPORT', calpath, body=report2body, headers=self.headers)
                        
        self.verifyItems(['1.ics', '2.ics', '3.ics', '4.ics'], positive=vcalitems, inelement='{urn:ietf:params:xml:ns:caldav}calendar-data')        
                                
        # --------- Test 3.xml query for VALARMS within time range
        
        self.testStart('Test 3.xml query for VALARMS within time range')
        
        #Setup request 
        f = open('files/reports/timerangequery/3.xml')
        report3body = f.read()
        self.request('REPORT', calpath, body=report3body, headers=self.headers)
            
        self.verifyItems(['4.ics'], positive=vcalitems, inelement='{urn:ietf:params:xml:ns:caldav}calendar-data') 
                               
        # --------- Test 4.xml
        
        self.testStart('Test 4.xml')
        
        #Setup request 
        f = open('files/reports/timerangequery/4.xml')
        report4body = f.read()
        self.request('REPORT', calpath, body=report4body, headers=self.headers)
        
        self.verifyItems(['1.ics', '2.ics', '3.ics', '4.ics', '5.ics', '6.ics', '7.ics'], positive=vcalitems, inelement='{urn:ietf:params:xml:ns:caldav}calendar-data')
        
        # --------- Test 5.xml
        
        self.testStart('Test 5.xml')
        
        #Setup request 
        f = open('files/reports/timerangequery/5.xml')
        report5body = f.read()
        self.request('REPORT', calpath, body=report5body, headers=self.headers)
        
        self.verifyItems(['1.ics', '2.ics', '3.ics', '4.ics', '5.ics', '6.ics', '7.ics'], positive=vcalitems, inelement='{urn:ietf:params:xml:ns:caldav}calendar-data')
        
        ##Blocked by bug 5551
        
        # -------------- More time range tests
        
        self.testStart('Uploading Float Events')
        
        # Put all float cals
        putfloat1icspath = self.pathBuilder('/home/cosmo-timerangequeryTestAccount/calendar/float1.ics') 
        putfloat2icspath = self.pathBuilder('/home/cosmo-timerangequeryTestAccount/calendar/float2.ics') 
        putfloat3icspath = self.pathBuilder('/home/cosmo-timerangequeryTestAccount/calendar/float3.ics') 
        
        f = open("files/reports/put/float1.ics")
        putfloat1icsbody = f.read()
        f = open("files/reports/put/float2.ics")
        putfloat2icsbody = f.read()
        f = open("files/reports/put/float3.ics")
        putfloat3icsbody = f.read()
        
        self.request('PUT', putfloat1icspath, body=putfloat1icsbody, headers=puticsheaders)
        self.request('PUT', putfloat2icspath, body=putfloat2icsbody, headers=puticsheaders)
        self.request('PUT', putfloat3icspath, body=putfloat3icsbody, headers=puticsheaders)
        
        # ---------------- oneInHonolulu.xml test
        
        self.testStart('oneInHonolulu.xml')
        
        #Build request
        f = open('files/reports/timerangequery/oneInHonolulu.xml')
        reportoneInHonolulubody = f.read()
        self.request('REPORT', calpath, body=reportoneInHonolulubody, headers=self.headers)
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID:-//Cyrusoft International\, Inc.//Mulberry v4.0//EN',
                      'VERSION:2.0', 'BEGIN:VEVENT', 'DTSTART:20060330T120000', 'DTEND:20060330T130000', 'SUMMARY:Floating Event One',
                      'DESCRIPTION: This event should appear in Honolulu, Mountain, and Eastern', 'UID:54E181BC7CCC373042B21884211@ninevah.local',
                      'END:VEVENT', 'END:VCALENDAR']
                      
        negcalitems = ['LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT','RRULE:', 'TZNAME:', 
                        'TZOFFSETFROM:', 'TZOFFSETTO:']
                        
        self.verifyItems(['float1.ics'], positive=vcalitems, negative=negcalitems, inelement='{urn:ietf:params:xml:ns:caldav}calendar-data')
        
        # ---------------- twoInMountain.xml test
        
        self.testStart('twoInMountain.xml')
        
        f = open('files/reports/timerangequery/twoInMountain.xml')
        reporttwoInMountainbody = f.read()
        self.request('REPORT', calpath, body=reporttwoInMountainbody, headers=self.headers)        
        
        self.verifyItems(['float1.ics','float2.ics'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', inelementexpectedcount=2)
        self.verifyInElement('float1.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems, negative=negcalitems)
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID:-//Cyrusoft International\, Inc.//Mulberry v4.0//EN',
                      'VERSION:2.0', 'BEGIN:VEVENT', 'DTSTART:20060330T150000', 'DTEND:20060330T160000', 'SUMMARY:Floating Event Two',
                      'DESCRIPTION: This event should appear in Honolulu and Mountain', 'UID:54E181BC7CCC373042B218842112@ninevah.local',
                      'END:VEVENT', 'END:VCALENDAR']

        self.verifyInElement('float2.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems, negative=negcalitems)
        
        # ----------------- threeInEastern.xml test
        
        self.testStart('threeInEastern.xml')

        f = open('files/reports/timerangequery/threeInEastern.xml')
        reportthreeInEasternbody = f.read()
        self.request('REPORT', calpath, body=reportthreeInEasternbody, headers=self.headers)
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID:-//Cyrusoft International\, Inc.//Mulberry v4.0//EN',
                      'VERSION:2.0', 'BEGIN:VEVENT', 'DTSTART:20060330T120000', 'DTEND:20060330T130000', 'SUMMARY:Floating Event One',
                      'DESCRIPTION: This event should appear in Honolulu, Mountain, and Eastern', 'UID:54E181BC7CCC373042B21884211@ninevah.local',
                      'END:VEVENT', 'END:VCALENDAR']
                      
        negcalitems = ['LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT','RRULE:', 'TZNAME:', 
                        'TZOFFSETFROM:', 'TZOFFSETTO:']
                        
        self.verifyItems(['float1.ics','float2.ics', 'float3.ics'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', inelementexpectedcount=3)
        self.verifyInElement('float1.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems, negative=negcalitems)
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID:-//Cyrusoft International\, Inc.//Mulberry v4.0//EN',
                      'VERSION:2.0', 'BEGIN:VEVENT', 'DTSTART:20060330T150000', 'DTEND:20060330T160000', 'SUMMARY:Floating Event Two',
                      'DESCRIPTION: This event should appear in Honolulu and Mountain', 'UID:54E181BC7CCC373042B218842112@ninevah.local',
                      'END:VEVENT', 'END:VCALENDAR']
        
        self.verifyInElement('float2.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems, negative=negcalitems)
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID:-//Cyrusoft International\, Inc.//Mulberry v4.0//EN',
                      'VERSION:2.0', 'BEGIN:VEVENT', 'DTSTART:20060330T170000', 'DTEND:20060330T180000', 'SUMMARY:Floating Event Three',
                      'DESCRIPTION: This event should appear in Honolulu', 'UID:54E181BC7CCC373042B218842113@ninevah.local',
                      'END:VEVENT', 'END:VCALENDAR']        
                      
        self.verifyInElement('float3.ics', '{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems, negative=negcalitems)
        
        # ----------------- invalid_nonUTC1 ---- specified by bkirsh via email on 4/5/2006
        
        self.testStart('invalid_nonUTC1')
        
        #Setup request 
        f = open('files/reports/timerangequery/invalid_nonUTC1.xml')
        report_invalid_nonUTC1body = f.read()
        self.request('REPORT', calpath, body=report_invalid_nonUTC1body, headers=self.headers)
        
        #Check Status = 400
        self.checkStatus(400)
        
        # ----------------- invalid_nonUTC2 ---- specified by bkirsh via email on 4/5/2006
        
        self.testStart('invalid_nonUTC2')
        
        #Setup request 
        f = open('files/reports/timerangequery/invalid_nonUTC2.xml')
        report_invalid_nonUTC2body = f.read()
        self.request('REPORT', calpath, body=report_invalid_nonUTC2body, headers=self.headers)
        
        #Check Status = 400
        self.checkStatus(400)
        
        # ----------------- invalid_nonUTC3 ---- specified by bkirsh via email on 4/5/2006
        
        self.testStart('invalid_nonUTC3')
        
        #Setup request 
        f = open('files/reports/timerangequery/invalid_nonUTC3.xml')
        report_invalid_nonUTC3body = f.read()
        self.request('REPORT', calpath, body=report_invalid_nonUTC3body, headers=self.headers)
        
        #Check Status = 400
        self.checkStatus(400)
        
        # ----------------- invalid_nonUTC4 ---- specified by bkirsh via email on 4/5/2006
        
        self.testStart('invalid_nonUTC4')
        
        #Setup request 
        f = open('files/reports/timerangequery/invalid_nonUTC4.xml')
        report_invalid_nonUTC4body = f.read()
        self.request('REPORT', calpath, body=report_invalid_nonUTC4body, headers=self.headers)
        
        #Check Status = 400
        self.checkStatus(400)
        
        # ----------------- invalid_nonUTC5 ---- specified by bkirsh via email on 4/5/2006
        
        self.testStart('invalid_nonUTC5')
        
        #Setup request 
        f = open('files/reports/timerangequery/invalid_nonUTC5.xml')
        report_invalid_nonUTC5body = f.read()
        self.request('REPORT', calpath, body=report_invalid_nonUTC5body, headers=self.headers)
        
        #Check Status = 400
        self.checkStatus(400)
        
        # ----------------- invalid_nonUTC6 ---- specified by bkirsh via email on 4/5/2006
        
        self.testStart('invalid_nonUTC6')
        
        #Setup request 
        f = open('files/reports/timerangequery/invalid_nonUTC6.xml')
        report_invalid_nonUTC6body = f.read()
        self.request('REPORT', calpath, body=report_invalid_nonUTC6body, headers=self.headers)
        
        #Check Status = 400
        self.checkStatus(400)
        
        # ----------------- timerange_01 ---- specified by bkirsh via email on 4/5/2006
        
        self.testStart('timerange_01')
        
        #Setup request 
        f = open('files/reports/timerangequery/timerange_01.xml')
        report_timerange_01body = f.read()
        self.request('REPORT', calpath, body=report_timerange_01body, headers=self.headers)
        
        #Check Status = 207 Multistatus
        self.checkStatus(207)        
        
        # ----------------- timerange_02 ---- specified by bkirsh via email on 4/5/2006
        
        self.testStart('timerange_02')
        
        #Setup request 
        f = open('files/reports/timerangequery/timerange_02.xml')
        report_timerange_02body = f.read()
        self.request('REPORT', calpath, body=report_timerange_02body, headers=self.headers)
        
        #Check Status = 207 Multistatus
        self.checkStatus(207)        
        
        # ----------------- timerange_03 ---- specified by bkirsh via email on 4/5/2006
        
        self.testStart('timerange_03')
        
        #Setup request 
        f = open('files/reports/timerangequery/timerange_03.xml')
        report_timerange_03body = f.read()
        self.request('REPORT', calpath, body=report_timerange_03body, headers=self.headers)
        
        #Check Status = 207 Multistatus
        self.checkStatus(207)           
        
        
        
        
        
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