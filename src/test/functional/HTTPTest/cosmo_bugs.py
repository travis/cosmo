from DAVTest import DAVTest   

class CosmoBugs(DAVTest):
    
    def startRun(self):
        
        #Set Headers and paths
        
        #Create Headers for CMP
        
        # ------- Test Create Account ------- #
           
        cmpheaders = self.headerAdd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headerAddAuth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathBuilder('/cmp/user/cosmo-bugsTestAccount')
        
        #Create testing account        
        bodycreateaccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-bugsTestAccount</username> \
                                 <password>cosmo-bugs</password> \
                                 <firstName>cosmo-bugs</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-bugsTestAccount@osafoundation.org</email> \
                                 </user>'
                                 
        #Create account and check status
        self.request('PUT', cmppath, body=bodycreateaccount, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED
        
        # ------- Test Create Calendar ------- #
        
        #Add auth to global headers
        self.headers = self.headerAddAuth("cosmo-bugsTestAccount", "cosmo-bugs")
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-bugsTestAccount/calendar/')
        self.request('MKCALENDAR', calpath, body=None, headers=self.headers)
        self.checkStatus(201)
        
        # -------------- Test for Bug 5175 -------------- #
        
        self.testStart('Test for Bug 5175')
        
        #Construct Headers & body for put of ics data for bug
        put5175icsheaders = self.headerAdd({'Content-Type' : 'text/calendar', 'Content-Length' : '183'})
        put5175icspath = self.pathBuilder('/home/cosmo-bugsTestAccount/calendar/5175.ics')
        f = open("files/reports/bugs/5175.ics")
        put5175icsbody = f.read()
        #Send request and check status for put of ics file
        self.request('PUT', put5175icspath, body=put5175icsbody, headers=put5175icsheaders)
        self.checkStatus(201)
    
        #Get REPORT bod
        f = open('files/reports/bugs/5175report.xml')
        report5175body = f.read()
        
        self.request('REPORT', calpath, body=report5175body, headers=self.headers)
        self.checkStatus(207) # check for multi-status
        
        # Verify that report for 5175.ics returns with proper url and UID:NEW_UID
        self.verifyDAVResponseItems(['5175.ics'], positive=['UID:NEW_UID'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data')
        
        # -------------- Test for Bug 5254 -------------- #
        
        self.testStart('Test for Bug 5254')
        
        #Build headers and body for ics put 
        put5254icsheaders = self.headerAdd({'Content-Type' : 'text/calendar', 'Content-Length' : '558'})
        put5254icspath = self.pathBuilder('/home/cosmo-bugsTestAccount/calendar/5254.ics')
        f = open("files/reports/bugs/5254.ics")
        put5254icsbody = f.read()

        #Send request and check status of put for ics
        self.request('PUT', put5254icspath, body=put5254icsbody, headers=put5254icsheaders)
        self.checkStatus(201)
        
        #Get REPORT body
        f = open('files/reports/bugs/5254report.xml')
        report5254body = f.read()
                            
        #Send REPORT request
        self.request('REPORT', calpath, body=report5254body, headers=self.headers)
        self.checkStatus(207) # check for multi-status
        
        
        # Verify that report for 5254.ics returns with proper url and UID:0F94FE7B-8E01-4B27-835E-CD1431FD6475
        self.verifyDAVResponseItems(['5254.ics'], positive=['UID:0F94FE7B-8E01-4B27-835E-CD1431FD6475'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data')
        
      
        # -------------- Test for Bug 5261 -------------- #
        
        self.testStart('Test for Bug 5261')
        
        #Build headers and body for ics put 
        put5261icsheaders = self.headerAdd({'Content-Type' : 'text/calendar', 'Content-Length' : '1002'})
        put5261icspath = self.pathBuilder('/home/cosmo-bugsTestAccount/calendar/5261.ics')
        f = open("files/reports/bugs/5261.ics")
        put5261icsbody = f.read()
        
        #Send request and check status of put for ics
        self.request('PUT', put5261icspath, body=put5261icsbody, headers=put5261icsheaders)
        self.checkStatus(201)
        
        f = open('files/reports/bugs/5261report.xml')
        report5261body = f.read()                    
                            
        #Send REPORT request
        self.request('REPORT', calpath, body=report5261body, headers=self.headers)
        self.checkStatus(207) # check for multi-status
        
        # Verify that report for 5254.ics returns with proper url and UID:20060223T151702Z-2256-500-23960-0@grenouille
        self.verifyDAVResponseItems(['5261.ics'], positive=['UID:20060223T151702Z-2256-500-23960-0@grenouille'], inelement='{urn:ietf:params:xml:ns:caldav}calendar-data')
        
        
        
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

    cosmobugs = CosmoBugs(host=host, port=port, path=path)
    cosmobugs.debug = debug
    cosmobugs.startRun()
    cosmobugs.end()





    
    