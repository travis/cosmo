from HTTPTest import HTTPTest    

class CosmoInvalid(HTTPTest):
    
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
        cmppath = self.pathBuilder('/cmp/user/cosmo-invalidTestAccount%s' % self.appendUser)
        
        #Create testing account        
        bodycreateaccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-invalidTestAccount%s</username> \
                                 <password>cosmo-invalid</password> \
                                 <firstName>cosmo-invalid</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-invalidTestAccount%s@osafoundation.org</email> \
                                 </user>' % (self.appendUser, self.appendUser)
                                 
        #Create account and check status
        self.request('PUT', cmppath, body=bodycreateaccount, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED
        
        # ------- Test Create Calendar ------- #
        
        #Add auth to global headers
        self.headers = self.headerAddAuth("cosmo-invalidTestAccount%s" % self.appendUser, "cosmo-invalid")
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-invalidTestAccount%s/calendar/' % self.appendUser)
        self.request('MKCALENDAR', calpath, body=None, headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test Creation of events view ICS ------- #
        
        #Construct headers & body
        puticsheaders = self.headerAdd({'Content-Type' : 'text/calendar'})      
        put1icspath = self.pathBuilder('/home/cosmo-invalidTestAccount%s/calendar/1.ics' % self.appendUser)
        put2icspath = self.pathBuilder('/home/cosmo-invalidTestAccount%s/calendar/2.ics' % self.appendUser)
        put3icspath = self.pathBuilder('/home/cosmo-invalidTestAccount%s/calendar/3.ics' % self.appendUser)
        put4icspath = self.pathBuilder('/home/cosmo-invalidTestAccount%s/calendar/4.ics' % self.appendUser)    
        put5icspath = self.pathBuilder('/home/cosmo-invalidTestAccount%s/calendar/5.ics' % self.appendUser)
        put6icspath = self.pathBuilder('/home/cosmo-invalidTestAccount%s/calendar/6.ics' % self.appendUser)
        put7icspath = self.pathBuilder('/home/cosmo-invalidTestAccount%s/calendar/7.ics' % self.appendUser) 
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
        
        self.headers = self.headerAdd({'Depth':'1'})
        
        # ------- Test 1.xml : noSubComp ---------- #
        
        self.testStart('Test 1.xml : noSubComp')
        
        #Setup request 
        f = open('files/reports/invalid/noSubComp.xml')
        report1body = f.read()
        report1body = report1body.replace('invalidTestAccount','invalidTestAccount%s'%self.appendUser)
        self.request('REPORT', calpath, body=report1body, headers=self.headers)
        
        self.checkStatus(400)
        

                
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