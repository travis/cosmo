import base64, time, md5, random, HTTPTest
import sys, getopt, httplib, urlparse, socket, xml

from HTTPTest import HTTPTest    
    
class FoxCloud(HTTPTest):


    def startRun(self):
        
        self.foxHeaders = {'Host' : "sync.foxcloud.com",
           'User-Agent': "Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.0.1) Gecko/20060111 Firefox/1.5.0.1 Foxmarks/0.56",
           'Accept' : "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5",
           'Accept-Language' : "en-us,en;q=0.5",
           'Accept-Encoding' : "gzip,deflate",
           'Accept-Charset' : "ISO-8859-1,utf-8;q=0.7,*;q=0.7",
           'Keep-Alive' : "300",
           'Connection' : "keep-alive",
           'Content-Length' : "255",
           'Content-Type' : "text/xml; charset=UTF-8"}
           
        self.cmp = '%s/api/signup' % self.connection["path"]
                
        self.rMikealAccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo"> \
                                 <username>mikeal</username> \
                                 <password>mikeal</password> \
                                 <firstName>Mikeal</firstName> \
                                 <lastName>Rogers</lastName> \
                                 <email>mikeal@osafoundation.org</email> \
                                 </user>'
        
        f = open("files/foxmarks.xml", "r")    
        self.rPutFox = '<?xml version="1.0" encoding="UTF-8"?> %s' % f.read()
        
        self.test_response = self.request('PUT', self.cmp, body=self.rMikealAccount, headers=self.foxHeaders)
        self.checkStatus(431)
    
    def recurringRun(self, counter):
        
        
        self.test_response = self.request('PUT', self.cmp, body=self.rMikealAccount, headers=self.foxHeaders)
        self.checkStatus(431) #username in use
        
        auth = 'Basic %s' % base64.encodestring('%s:%s' % ("testAccount%s" % counter, "testAccount")).strip()
        
        putFoxHeaders = {'Host' : "sync.foxcloud.com",
                   'User-Agent': "Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.0.1) Gecko/20060111 Firefox/1.5.0.1 Foxmarks/0.56",
                   'Accept' : "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5",
                   'Accept-Language' : "en-us,en;q=0.5",
                   'Accept-Encoding' : "gzip,deflate",
                   'Accept-Charset' : "ISO-8859-1,utf-8;q=0.7,*;q=0.7",
                   'Keep-Alive' : "300",
                   'Connection' : "keep-alive",
                   'Content-Length' : "12789",
                   'Content-Type' : "text/xml; charset=UTF-8",
                   'Authorization': auth}
                   
        getFoxHeaders = {'Host' : "sync.foxcloud.com",
                   'User-Agent': "Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.0.1) Gecko/20060111 Firefox/1.5.0.1 Foxmarks/0.56",
                   'Accept' : "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5",
                   'Accept-Language' : "en-us,en;q=0.5",
                   'Accept-Encoding' : "gzip,deflate",
                   'Accept-Charset' : "ISO-8859-1,utf-8;q=0.7,*;q=0.7",
                   'Keep-Alive' : "300",
                   'Connection' : "keep-alive",
                   'Content-Type' : "text/xml; charset=UTF-8",
                   'Authorization' : auth,
                   'If-None-Match' : "15847-1143250968808",
                   'Pragma' : 'no-cache',
                   'Cache-Control' : 'no=cache'}
        
        rCMPNewCurreEmail = '<?xml version="1.0" encoding="utf-8" ?> \
                             <user xmlns="http://osafoundation.org/cosmo"> \
                             <username>testAccount%s</username> \
                             <password>testAccount</password> \
                             <firstName>testAccount</firstName> \
                             <lastName>Rogers</lastName> \
                             <email>testAccount%s@osafoundation.org</email> \
                             </user>' % (counter, counter)
                             
        putfox = "%s/home/testAccount%s/files/foxmarks.xml" % (self.connection["path"], counter)
        
    
        #print self.results[0]
        self.test_response = self.request('PUT', self.cmp, body=rCMPNewCurreEmail, headers=self.foxHeaders)
        self.checkStatus(201)
        
        #print self.results[1]
        
        self.test_response = self.request('MKCALENDAR', putfox, body=self.rPutFox, headers=putFoxHeaders)
        self.checkStatus(201)
        #print self.results[2]
        
        self.test_response = self.request('GET', putfox, body=None, headers=getFoxHeaders)
        
        self.checkStatus(200)
        #print self.results[3]


if __name__ == "__main__":
    
    import sys
    
    host = 'localhost'
    port = '8080'
    path = ''
    counter = 10
    
    for arg in sys.argv:
        args = arg.split("=")
        if args[0] == "host":
            host = args[1]
        elif args[0] == "port":
            port = args[1]._int()
        elif args[0] == "path":
            path = args[1]
        elif args[0] == "recurring":
            counter = int(args[1])
        
    print "host %s port %s recurring %s path %s" % (host, port, counter, path)
    
    foxHeaders = {'Host' : "sync.foxcloud.com",
           'User-Agent': "Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.0.1) Gecko/20060111 Firefox/1.5.0.1 Foxmarks/0.56",
           'Accept' : "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5",
           'Accept-Language' : "en-us,en;q=0.5",
           'Accept-Encoding' : "gzip,deflate",
           'Accept-Charset' : "ISO-8859-1,utf-8;q=0.7,*;q=0.7",
           'Keep-Alive' : "300",
           'Connection' : "keep-alive",
           'Content-Length' : "255",
           'Content-Type' : "text/xml; charset=UTF-8"}
    
    foxcloudTest = FoxCloud(headers=foxHeaders, host=host, port=port, path=path)
    foxcloudTest.startRun()
    x = 0
    
    while x < counter:
        
        foxcloudTest.recurringRun(x)
        x = x + 1



    foxcloudTest.end()





    
    

    
    


    
    

    
    
