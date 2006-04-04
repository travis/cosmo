from HTTPTest import HTTPTest    

class CosmoFreeBusy(HTTPTest):
    
    def startRun(self):
        
        #Set Headers and paths
        
        #Create Headers for CMP
        
        # ------- Test Create Account ------- #
           
        cmpheaders = self.headeradd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headeraddauth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathbuilder('/cmp/user/cosmo-freebusyTestAccount')
        
        #Create testing account        
        bodycreateaccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-freebusyTestAccount</username> \
                                 <password>cosmo-freebusy</password> \
                                 <firstName>cosmo-freebusy</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-freebusyTestAccount@osafoundation.org</email> \
                                 </user>'
                                 
        #Create account and check status
        self.request('PUT', cmppath, body=bodycreateaccount, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED
        
        # ------- Test Create Calendar ------- #
        
        #Add auth to global headers
        self.headers = self.headeraddauth("cosmo-freebusyTestAccount", "cosmo-freebusy")
        
        #Create Calendar on CalDAV server   
        calpath = self.pathbuilder('/home/cosmo-freebusyTestAccount/calendar/')
        self.request('MKCALENDAR', calpath, body=None, headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test Creation of events view ICS ------- #
        
        #Construct headers & body
        puticsheaders = self.headeradd({'Content-Type' : 'text/calendar'})      
        put1icspath = self.pathbuilder('/home/cosmo-freebusyTestAccount/calendar/1.ics')
        put2icspath = self.pathbuilder('/home/cosmo-freebusyTestAccount/calendar/2.ics')
        put3icspath = self.pathbuilder('/home/cosmo-freebusyTestAccount/calendar/3.ics')
        put4icspath = self.pathbuilder('/home/cosmo-freebusyTestAccount/calendar/4.ics')    
        put5icspath = self.pathbuilder('/home/cosmo-freebusyTestAccount/calendar/5.ics')
        put6icspath = self.pathbuilder('/home/cosmo-freebusyTestAccount/calendar/6.ics')
        put7icspath = self.pathbuilder('/home/cosmo-freebusyTestAccount/calendar/7.ics') 
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
        
        # ------- Test 1.xml : REPORT query freebusy time-range start="20060101T000000Z" end="20060105T000000Z" ---------- #
        
        #Setup request 
        f = open('files/reports/freebusy/1.xml')
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
            
        vcalitems = ['FREEBUSY:', '20060101T150000Z/20060101T160000Z', '20060101T180000Z/20060101T190000Z',
                      '20060101T210000Z/20060101T220000Z', '20060101T230000Z/20060102T000000Z', '20060102T150000Z/20060102T160000Z',
                      '20060102T190000Z/20060102T200000Z', '20060102T230000Z/20060103T000000Z', '20060103T150000Z/20060103T160000Z',
                      '20060103T190000Z/20060103T200000Z', '20060103T230000Z/20060104T000000Z', '20060104T150000Z/20060104T160000Z',
                      '20060104T210000Z/20060104T220000Z', '20060105T010000Z/20060105T000000Z']
                      
                      
        
        #Check response elements for each response and verify the calendar-data element has the proper info
        test = self.xml_doc.findall('.//{DAV:}response')
        for t in test:
            ctest = t.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
            icscount = icscount + 1
            ctest.text = ctest.text.replace('\n ', '')
            for x in vcalitems:
                if ctest.text.find(x) == -1:
                    self.printout('FAILED to get %s in %s' % (x, t[0].text))
                    icscount = icscount - 100
                        
        #Run through all the elemenet and etag counts and make sure they match
        if elementcount == 1 & icscount == 1:
            self.report(True, test='report/freebusy/1.xml REPORT query freebusy time-range start="20060101T000000Z" end="20060105T000000Z"', comment=None)
        else:
            self.report(False, test='report/freebusy/1.xml REPORT query freebusy time-range start="20060101T000000Z" end="20060105T000000Z"', comment='Returned %s elements & %s ics matches' % (elementcount, icscount))
  
  
  
  
  
  
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
    
    cosmofreebusy = CosmoFreeBusy(host=host, port=port, path=path)
    cosmofreebusy.debug = debug
    cosmofreebusy.startRun()
    cosmofreebusy.end()
    