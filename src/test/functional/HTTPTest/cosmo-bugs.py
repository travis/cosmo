from HTTPTest import HTTPTest    

class CosmoBugs(HTTPTest):
    
    def startRun(self):
        
        #Set Headers and paths
        
        #Create Headers for CMP
        
        # ------- Test Create Account ------- #
           
        cmpheaders = self.headeradd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headeraddauth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathbuilder('/cmp/user/cosmo-bugsTestAccount')
        
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
        self.headers = self.headeraddauth("cosmo-bugsTestAccount", "cosmo-bugs")
        
        #Create Calendar on CalDAV server   
        calpath = self.pathbuilder('/home/cosmo-bugsTestAccount/calendar/')
        self.request('MKCALENDAR', calpath, body=None, headers=self.headers)
        self.checkStatus(201)
        
        # -------------- Test for Bug 5175 -------------- #
        
        #Construct Headers & body for put of ics data for bug
        put5175icsheaders = self.headeradd({'Content-Type' : 'text/calendar', 'Content-Length' : '183'})
        put5175icspath = self.pathbuilder('/home/cosmo-bugsTestAccount/calendar/5175.ics')
        f = open("files/bugs/5175.ics")
        put5175icsbody = f.read()
        #Send request and check status for put of ics file
        self.request('PUT', put5175icspath, body=put5175icsbody, headers=put5175icsheaders)
        self.checkStatus(201)
    
        #Contruct body for REPORT
        report5175body = '<?xml version="1.0"?> \
                          <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav"> \
                              <D:prop xmlns:D="DAV:"> \
                                  <D:getetag/> \
                                  <C:calendar-data/> \
                          </D:prop> \
                          <C:filter> \
                              <C:comp-filter name="VCALENDAR"> \
                                  <C:comp-filter name="VEVENT"> \
                                      <C:prop-filter name="UID"> \
                                          <C:text-match caseless="no">NEW_UID</C:text-match> \
                                      </C:prop-filter> \
                                  </C:comp-filter> \
                              </C:comp-filter> \
                          </C:filter> \
                          </C:calendar-query> '
                          
        #Send request for REPORT
        self.request('REPORT', calpath, body=report5175body, headers=self.headers)
        #Pase XML and query for calendar-data element
        self.xmlparse()
        test = self.xml_doc.find('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
        #Check to see if element contains 'UID:NEW_UID'
        test = test.text.find('UID:NEW_UID')
        if test == -1:
            self.report(False, test="Checking for UID:NEW_UID", comment="String UID:NEW_UID not found")
        else:
            self.report(True, test="Checking for UID:NEW_UID", comment=None)
        
        # -------------- Test for Bug 5254 -------------- #
        
        #Build headers and body for ics put 
        put5254icsheaders = self.headeradd({'Content-Type' : 'text/calendar', 'Content-Length' : '558'})
        put5254icspath = self.pathbuilder('/home/cosmo-bugsTestAccount/calendar/5254.ics')
        f = open("files/bugs/5254.ics")
        put5254icsbody = f.read()

        #Send request and check status of put for ics
        self.request('PUT', put5254icspath, body=put5254icsbody, headers=put5254icsheaders)
        self.checkStatus(201)
        
        #Construct body for REPORT
        report5254body = '<?xml version="1.0"?> \
                            <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav"> \
                              <D:prop xmlns:D="DAV:"> \
                                <C:calendar-data/> \
                              </D:prop> \
                              <C:filter> \
                                <C:comp-filter name="VCALENDAR"> \
                                  <C:comp-filter name="VEVENT"> \
                                    <C:time-range end="20060109T000000Z" start="20060101T000000Z"/> \
                                  </C:comp-filter> \
                                </C:comp-filter> \
                              </C:filter> \
                            </C:calendar-query> '
        #Send REPORT request
        self.request('REPORT', calpath, body=report5254body, headers=self.headers)
        #Parse XML from body
        self.xmlparse()
        #Get calendar-data element
        test = self.xml_doc.findall('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
        #Check to see if element contains 'UID:0F94FE7B-8E01-4B27-835E-CD1431FD6475'
        for t in test:
            if t.text.find('UID:0F94FE7B-8E01-4B27-835E-CD1431FD6475') != -1:
                self.report(True, test='Checking for UID:0F94FE7B-8E01-4B27-835E-CD1431FD6475', comment=None)
            
        if self.resultnames[-1] != 'Checking for UID:0F94FE7B-8E01-4B27-835E-CD1431FD6475':
            self.report(False, test='Checking for UID:0F94FE7B-8E01-4B27-835E-CD1431FD6475', comment=None)
      
        # -------------- Test for Bug 5261 -------------- #
        
        #Build headers and body for ics put 
        put5261icsheaders = self.headeradd({'Content-Type' : 'text/calendar', 'Content-Length' : '1002'})
        put5261icspath = self.pathbuilder('/home/cosmo-bugsTestAccount/calendar/5261.ics')
        f = open("files/bugs/5261.ics")
        put5261icsbody = f.read()
        
        #Send request and check status of put for ics
        self.request('PUT', put5261icspath, body=put5261icsbody, headers=put5261icsheaders)
        self.checkStatus(201)
        
        #Construct body for REPORT
        report5261body = '<?xml version="1.0"?> \
                            <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav"> \
                              <D:prop xmlns:D="DAV:"> \
                                <D:getetag/> \
                                <C:calendar-data/> \
                              </D:prop> \
                              <C:filter> \
                                <C:comp-filter name="VCALENDAR"> \
                                  <C:comp-filter name="VEVENT"> \
                                    <C:prop-filter name="UID"> \
                                      <C:text-match caseless="no">20060223T151702Z-2256-500-23960-0@grenouille</C:text-match> \
                                    </C:prop-filter> \
                                  </C:comp-filter> \
                                </C:comp-filter> \
                              </C:filter> \
                            </C:calendar-query>'
                            
        #Send REPORT request
        self.request('REPORT', calpath, body=report5261body, headers=self.headers)
        #Parse XML from body
        self.xmlparse()
        #Get calendar-data element
        test = self.xml_doc.findall('.//{urn:ietf:params:xml:ns:caldav}calendar-data')
        #Check to see if element contains 'UID:20060223T151702Z-2256-500-23960-0@grenouille'
        for t in test:
            if t.text.find('UID:20060223T151702Z-2256-500-23960-0@grenouille') != -1:
                self.report(True, test='Checking for UID:20060223T151702Z-2256-500-23960-0@grenouille', comment=None)
            
        if self.resultnames[-1] != 'Checking for UID:20060223T151702Z-2256-500-23960-0@grenouille':
            self.report(False, test='Checking for UID:20060223T151702Z-2256-500-23960-0@grenouille', comment=None)
        
        
        
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
    cosmobugs = CosmoBugs(host=host, port=port, path=path)
    print 'instance created, start run begin'
    cosmobugs.debug = debug
    cosmobugs.startRun()
    cosmobugs.end()





    
    