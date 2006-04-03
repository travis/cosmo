from HTTPTest import HTTPTest    

class CosmoInvalid(HTTPTest):
    
    def startRun(self):
        
        #Set Headers and paths
        
        #Create Headers for CMP
        
        # ------- Test Create Account ------- #
           
        cmpheaders = self.headeradd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headeraddauth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathbuilder('/cmp/user/cosmo-invalidTestAccount')
        
        #Create testing account        
        bodycreateaccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-invalidTestAccount</username> \
                                 <password>cosmo-invalid</password> \
                                 <firstName>cosmo-invalid</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-invalidTestAccount@osafoundation.org</email> \
                                 </user>'
                                 
        #Create account and check status
        self.request('PUT', cmppath, body=bodycreateaccount, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED
        
        # ------- Test Create Calendar ------- #
        
        #Add auth to global headers
        self.headers = self.headeraddauth("cosmo-invalidTestAccount", "cosmo-invalid")
        
        #Create Calendar on CalDAV server   
        calpath = self.pathbuilder('/home/cosmo-invalidTestAccount/calendar/')
        self.request('MKCALENDAR', calpath, body=None, headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test Creation of events view ICS ------- #
        
        #Construct headers & body
        puticsheaders = self.headeradd({'Content-Type' : 'text/calendar'})      
        put1icspath = self.pathbuilder('/home/cosmo-invalidTestAccount/calendar/1.ics')
        put2icspath = self.pathbuilder('/home/cosmo-invalidTestAccount/calendar/2.ics')
        put3icspath = self.pathbuilder('/home/cosmo-invalidTestAccount/calendar/3.ics')
        put4icspath = self.pathbuilder('/home/cosmo-invalidTestAccount/calendar/4.ics')    
        put5icspath = self.pathbuilder('/home/cosmo-invalidTestAccount/calendar/5.ics')
        put6icspath = self.pathbuilder('/home/cosmo-invalidTestAccount/calendar/6.ics')
        put7icspath = self.pathbuilder('/home/cosmo-invalidTestAccount/calendar/7.ics') 
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
        
        # ------- Test 1.xml : noSubComp ---------- #
        
        #Setup request 
        f = open('files/reports/invalid/noSubComp.xml')
        report1body = f.read()
        self.request('REPORT', calpath, body=report1body, headers=self.headers)
        
        self.checkStatus(501)
        

                
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
    
    cosmoinvalid = CosmoInvalid(host=host, port=port, path=path)
    cosmoinvalid.debug = debug
    cosmoinvalid.startRun()
    cosmoinvalid.end()