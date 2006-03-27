import base64, time, md5, random
import sys, getopt, httplib, urlparse, socket, xml

class HTTPTest:
    
    def __init__(self, headers, host, port, path, tls=False):
        
        self.connection = {"host" : host, "port" : port, "path" : path, "tls" : tls}
        
        self.results = []
        self.test_response = self.request('OPTIONS', path, body=None, headers=headers)
        
    def checkStatus(self, status):
        
        out = self.test_response.status
        
        if out == status:
            self.results.append(True)
            return True
        else:
            self.results.append(False)
            print "Failure on Status Code Check :: expected %s :: received %s" % (status, out)
            return False
        
    def end(self):
        
        count = 0
        failures = 0
        passes = 0
        for result in self.results:
            if result == False:
                failures = failures + 1
            elif result == True:
                passes = passes +1
            
            count = count + 1
        
        print "Failures :: %s" % failures
        print "Passes :: %s" % passes 
        print "Total tests run :: %s" % count
            
    
    def request(self, method, url, body=None, headers={}, 
                autoheaders=('Content-Length', 'Content-Type', 'User-Agent',
                              'Host'),
                xmlExpectedStatusCodes=(200, 207,)):
        """
        Helper function to make requests easier to make.
        
        @return: Customized httplib.HTTPResponse object: read() will always return
                 full data that was received.
        """
        
        if not self.connection["tls"]:
            c = httplib.HTTPConnection(self.connection["host"], self.connection["port"])
        else:
            c = httplib.HTTPSConnection(self.connection["host"], self.connection["port"])
        h = headers.copy()
        for header in autoheaders:
            if header == 'Content-Length' and body is not None:
                h[header] = '%d' % len(body)
            if header == 'Content-Type' and body is not None:
                h[header] = 'text/xml'
            if header == 'User-Agent':
                h[header] = 'silmut'
            if header == 'Host':
                h[header] = '%s:%s' % (self.connection["host"], self.connection["port"])
        c.request(method, url, body, h)
        r = c.getresponse()
        
        # Automatically follow 302 GET (same host only)
        if method == 'GET' and r.status == 302:
            q = url.find('?')
            query = ''
            if q != -1:
                query = url[q:]
            redirectHost, redirectPort, url, redirectTLS = parseURL(r.getheader('Location'))
            if redirectHost != host or redirectPort != port:
                raise Exception('Redirect allowed to same server only')
            if url.find('?') == -1:
                url = '%s%s' % (url, query)
            return request(method, url, body, headers)
    
        # This is needed because once read(), more read()s would just return empty.
        r.body = r.read()
        r.read = lambda: r.body
        
        return r
    
    