from DAVTest import DAVTest    

class CosmoFreeBusy(DAVTest):
    
    def startRun(self):
        
        #Set Headers and paths
        
        #Create Headers for CMP
        
        # ------- Test Create Account ------- #
           
        cmpheaders = self.headerAdd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headerAddAuth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathBuilder('/cmp/user/cosmo-freebusyTestAccount')
        
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
        self.headers = self.headerAddAuth("cosmo-freebusyTestAccount", "cosmo-freebusy")
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-freebusyTestAccount/calendar/')
        self.request('MKCALENDAR', calpath, body=None, headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test Creation of events view ICS ------- #
        
        #Construct headers & body
        puticsheaders = self.headerAdd({'Content-Type' : 'text/calendar'})      
        put1icspath = self.pathBuilder('/home/cosmo-freebusyTestAccount/calendar/1.ics')
        put2icspath = self.pathBuilder('/home/cosmo-freebusyTestAccount/calendar/2.ics')
        put3icspath = self.pathBuilder('/home/cosmo-freebusyTestAccount/calendar/3.ics')
        put4icspath = self.pathBuilder('/home/cosmo-freebusyTestAccount/calendar/4.ics')    
        put5icspath = self.pathBuilder('/home/cosmo-freebusyTestAccount/calendar/5.ics')
        put6icspath = self.pathBuilder('/home/cosmo-freebusyTestAccount/calendar/6.ics')
        put7icspath = self.pathBuilder('/home/cosmo-freebusyTestAccount/calendar/7.ics') 
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
        self.checkStatus(207)
            
        vcalitems = ['FREEBUSY:', '20060101T150000Z/20060101T160000Z', '20060101T180000Z/20060101T190000Z',
                      '20060101T210000Z/20060101T220000Z', '20060101T230000Z/20060102T000000Z', '20060102T150000Z/20060102T160000Z',
                      '20060102T190000Z/20060102T200000Z', '20060102T230000Z/20060103T000000Z', '20060103T150000Z/20060103T160000Z',
                      '20060103T190000Z/20060103T200000Z', '20060103T230000Z/20060104T000000Z', '20060104T150000Z/20060104T160000Z',
                      '20060104T210000Z/20060104T220000Z', '20060105T010000Z/20060105T000000Z']
                      
        self.verifyItems([''], inelement='{DAV:}getetag', positive=[''])              
        self.verifyItems([''], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data', positive=vcalitems, )
  
  
  
  
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
    