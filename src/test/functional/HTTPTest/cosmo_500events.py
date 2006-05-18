import random
from DAVTest import DAVTest

class Cosmo500Events(DAVTest):
    
    def startRun(self):
        
        # ------- Test Create Account ------- #
        
        self.testStart('Setup Accounts')
        
        try:
            self.appendUser = self.appendDict['username']
        except KeyError:
            self.appendUser = ''
           
        cmpheaders = self.headerAdd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headerAddAuth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathBuilder('/cmp/user/cosmo-500eventsTestAccount%s' % self.appendUser)
        
        #Create testing account        
        bodycreateaccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-500eventsTestAccount%s</username> \
                                 <password>cosmo-500events</password> \
                                 <firstName>cosmo-500events</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-500eventsTestAccount%s@osafoundation.org</email> \
                                 </user>' % (self.appendUser, self.appendUser)
                                 
        #Create account and check status
        self.request('PUT', cmppath, body=bodycreateaccount, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED
        
        # ------- Test Create Calendar ------- #
        #Add auth to global headers
        self.headers = self.headerAddAuth("cosmo-500eventsTestAccount%s" % self.appendUser, "cosmo-500events")
        
        #Create Calendar on CalDAV server   
        self.calpath = self.pathBuilder('/home/cosmo-500eventsTestAccount%s/calendar/' % self.appendUser)
        self.request('MKCALENDAR', self.calpath, body=None, headers=self.headers)
        self.checkStatus(201)
      
    def recurringRun(self):
        
        
        # ------- Test Creation of events view ICS ------- #
        
        #Construct headers & body
        puticsheaders = self.headerAdd({'Content-Type' : 'text/calendar'})      
        
        #Acquire properties from the file
        f = open("files/reports/put/1.ics")
        import copy
        puticsbody = copy.copy(f.read())
        
        #Figure out where we start numbering
        
        #if(self.recurrence is 0):
        #    start_numbering = 0
        #if (self.recurrence is 1):
        #    start_numbering = 0
        #else:
        #    start_numbering = self.recurrence * 500
        if (self.currentRecurrence == 1):
            start_numbering = 0
        else:
            start_numbering = (self.currentRecurrence * 500) - 500
        
        #Now we will loop through 500 time
        for i in range(500):
            identifier = self.currentRecurrence+1 * 500 * (i * 500)
            puticspath = self.pathBuilder('/home/cosmo-500eventsTestAccount%s/calendar/%s.ics' % (self.appendUser, identifier))
 
            #Construct the puticsbody variable for this iteration
            first_int = random.randint(0,5000000000)
            second_int = random.randint(0,5000000000)
            alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            new_uid = str(first_int) + str(alphabet[random.randint(0,25)]) +  str(second_int) + str(alphabet[random.randint(0,25)]) + str(identifier) + 'ninevah.local'
            currentputicsbody = puticsbody.replace('54E181BC7CCC373042B28842@ninevah.local', new_uid)
            
            #Add the event
            self.request('PUT', puticspath, body=currentputicsbody, headers=puticsheaders)
            
            #Check status
            self.checkStatus(201)
            
        self.testStart('Report test for all ics events')
        
        #Setup request 
        f = open('files/reports/basicquery/2.xml')
        report2body = f.read()
        self.request('REPORT', self.calpath, body=report2body, headers=self.headers)
        self.checkStatus(207) # Verify multi-status
        
        vcalitems = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                      'BEGIN:VTIMEZONE', 'LAST-MODIFIED:', 'TZID', 'BEGIN:DAYLIGHT',
                      'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                      'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'END:VCALENDAR',
                      'BEGIN:VEVENT', 'SUMMARY:event 1', 'END:VEVENT']
                      
        self.xmlParse()
        eventsTest = self.xml_doc.findall('.//{DAV:}response')
        print len(eventsTest)
        if len(eventsTest) == 500*(self.currentRecurrence+1):
            self.report(True, 'Testing DAV:Response count == %s' % len(eventsTest))
        else:
            self.report(False, 'Testing DAV:Response count == %s' % len(eventsTest))
        
        
        
        
        
if __name__ == "__main__":
    
    import sys
    
    host = 'localhost'
    port = '8080'
    path = '/cosmo'
    debug = 0
    recurrence = 1
    
    for arg in sys.argv:
        args = arg.split("=")
        if args[0] == "host":
            host = args[1]
        elif args[0] == "port":
            port = int(args[1])
        elif args[0] == "path":
            path = args[1]
        elif args[0] == "recurrence":
            recurrence = int(args[1])
        elif args[0] == "debug":
            debug = int(args[1])
        
    print "host %s port %s recurring %s path %s" % (host, port, recurrence, path)
    
    Cosmo500Events = Cosmo500Events(host=host, port=port, path=path, recurrence=recurrence, debug=debug)
    Cosmo500Events.fullRun()