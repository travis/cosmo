#! /usr/bin/env python

# 
# Copyright 2005 Kapor Enterprises, Inc. and Todd Agulnick.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at 
# 
# 	http://www.apache.org/licenses/LICENSE-2.0
# 	
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 
# hammer.py: Stress-tests a Cosmo server.
#


"""Hammer Cosmo Tester

Executes stress tests the WebDAV portion of Cosmo.

Usage: python hammer.py [options]

Options:
  -t        number of threads to deploy (default is 10)
  -i        number of GET/PUTs to do on each thread (default is 100)
  -s        url (default is http://localhost:8080/cosmo)
  -l        size of datafile to GET/PUT in bytes (default is 10000)
  -u        username (default is test; thread number is appended to username)
  -w        password (default is test; thread number is appended to password)
  -m        timeout setting in seconds (default is 30)
  -a        test account creation (# created is -t * -i)
  -o	    initial offset for account creation
  -y        Use Python httplib instead of pycurl
  -h        display this help text

Examples:
  hammer.py runs test using default parameters
  hammer.py -s http://localhost:80 runs default test on localhost port 80

Tip:
  Use createaccounts.py to set up a batch of test accounts before running
  this script.
  
"""

from threading import Thread
from threading import Lock
import sys, getopt, datetime, time
import string, random, base64, socket, httplib
import createaccounts

try:
    import pycurl
except ImportError:
    pycurl = None


def LogEvent(*attr):
    s = "\t".join(attr)
    outputLock.acquire()
    print "%s\t%s" % (datetime.datetime.now().isoformat(' '), s)
    outputLock.release()

def request(tls, host, port, method, url, body=None, 
            username=None, password=None,
            headers={}, 
            autoheaders=('Content-Length', 'Content-Type', 'User-Agent',
                          'Host', 'Authorization')):
    """
    Helper function to make requests easier to make.
    """
    if not tls:
        c = httplib.HTTPConnection(host, port)
    else:
        c = httplib.HTTPSConnection(host, port)
    h = headers.copy()
    for header in autoheaders:
        if header == 'Content-Length' and body is not None:
            h[header] = '%d' % len(body)
        if header == 'Content-Type' and body is not None:
            h[header] = 'text/txt'
        if header == 'User-Agent':
            h[header] = 'Cosmo hammer'
        if header == 'Host':
            h[header] = '%s:%s' % (host, port)
        if header == 'Authorization' and username is not None and \
            password is not None:
            auth = 'Basic %s' % base64.encodestring('%s:%s' % (username, password)).strip()
            h[header] = auth
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
    
    return r

class TestCosmoPurePython(Thread):
    def __init__(self, tls, server, port, url, datalength, iterations, username, password):
        Thread.__init__(self)
        self.tls = tls
        self.server = server
        self.port = port
        self.url = url
        self.datalength = datalength
        self.iterations = iterations
        self.passed = False
        self.username = username
        self.password = password
    
    def run(self):
         for i in range(self.iterations):
            strToWrite = randomString(self.datalength)
            strRead = ''
            if self.write(strToWrite) is not True: return
            strRead = self.read()
            if strRead == "": return
            if strToWrite != strRead:
                print "\nDownloaded string didn't match uploaded string"
                return
            #print "#",
         self.passed = True

    def write(self, data):
        try:
            start = time.time()
            r = request(self.tls, self.server, self.port, "PUT", self.url, 
                        data, self.username, self.password)
            LogEvent("PUT", "%.3f" % (time.time() - start))
        except:
            exctype, value = sys.exc_info()[:2]
            LogEvent("PUT ERROR", str(exctype), str(value))
            return False
        return True

    def read(self):
        try:
            start = time.time()
            r = request(self.tls, self.server, self.port, "GET", self.url, 
                        username=self.username, password=self.password)
            data = r.read()
            LogEvent("GET", "%.3f" % (time.time() - start))
            return data
        except:
            exctype, value = sys.exc_info()[:2]
            LogEvent("GET ERROR", str(exctype), str(value))
            return ""
        return ""


if pycurl is not None:
    class TestCosmo(Thread):
        def __init__(self, url, datalength, iterations, timeout):
            Thread.__init__(self)
            self.url = url
            self.datalength = datalength
            self.iterations = iterations
            self.strToWrite = "";
            self.strRead = "";
            self.passed = False
            self.timeout = timeout
    
        def run(self):
             for i in range(self.iterations):
                self.strToWrite = StringAsFile(randomString(self.datalength))
                self.strRead = StringAsFile()
                if self.write(self.strToWrite) is not True: return
                if self.read(self.strRead) is not True: return
                if str(self.strToWrite) != str(self.strRead):
                    print "\nDownloaded string didn't match uploaded string"
                    return
                #print "#",
             self.passed = True
    
        def write(self, fp):
            curl = pycurl.Curl()
            curl.setopt(pycurl.URL, self.url)
            curl.setopt(pycurl.UPLOAD, 1)
            curl.setopt(pycurl.NOSIGNAL, 1)
            curl.setopt(pycurl.READFUNCTION, fp.read)
            if self.timeout > 0: curl.setopt(pycurl.TIMEOUT, self.timeout)
            try:
                start = time.time()
                curl.perform()
                LogEvent("PUT", "%.3f" % (time.time() - start))
                
            except:
                exctype, value = sys.exc_info()[:2]
                if exctype == pycurl.error:
                    LogEvent("PUT ERROR", value[1])
                else:
                    LogEvent("PUT ERROR", str(exctype), str(value))
                return False
            curl.close()
            return True
    
        def read(self, fp):
            curl = pycurl.Curl()
            curl.setopt(pycurl.URL, self.url)
            curl.setopt(pycurl.WRITEFUNCTION, fp.write)
            curl.setopt(pycurl.NOSIGNAL, 1)
            if self.timeout > 0: curl.setopt(pycurl.TIMEOUT, self.timeout)
            try:
                start = time.time()
                curl.perform()
                LogEvent("GET", "%.3f" % (time.time() - start))
            except:
                exctype, value = sys.exc_info()[:2]
                if exctype == pycurl.error:
                    LogEvent("GET ERROR", value[1])
                else:
                    LogEvent("GET ERROR", str(exctype), str(value))
                return False
            curl.close()
            return True


    class StringAsFile:
        def __init__(self, str = ""):
            self.str = str
            self.index = 0
    
        def read(self, size):
            p = self.index
            numchars = int(size / 2)
            self.index += numchars
            return self.str[p:p+numchars]
        
        def write(self, str):
            self.str += str
    
        def __repr__(self):
            return self.str


class TestCosmoAccountCreation(Thread):
    def __init__(self, tls, server, port, path, first, count):
        Thread.__init__(self)
        self.tls = tls
        self.server = server
        self.port = port
        self.path = path
        self.first = first
        self.count = count
        self.passed = False
        
    def run(self):
        for i in range(self.first, self.first + self.count):
            username = "test" + str(i)
            password = "test" + str(i)
            start = time.time()
            if createaccounts.createAccount(self.tls, self.server, self.port,
                                            self.path, username, 
                                            password) == False:
                return False
            else:
		LogEvent("Created Account %s" % username, "%.3f" % (time.time() - start))
        self.passed = True
               
        

def randomString(length = 10000):
    item = string.letters + string.digits
    return "".join([random.choice(item) for i in range(length)])


def usage():
    print __doc__

    
def main(argv):
    try:
        opts, args = getopt.getopt(argv, "t:i:s:p:l:u:w:m:o:ayh")
    except getopt.GetoptError:
        usage()
        sys.exit(2)

    # establish defaults
    threads = 10
    iterations = 100
    server = "localhost"
    port = 8080
    path = "/cosmo"
    url = "http://%s:%s%s" % (server, port, path)
    datalength = 10000
    username = "test"
    password = "test"
    timeout = 30
    accountCreation = False
    offset = 1
    pythonHammer = False
    
    for (opt, arg) in opts:
        if opt == "-t":      threads = int(arg)
        elif opt == "-i":    iterations = int(arg)
        elif opt == "-s":    url = arg
        elif opt == "-l":    datalength = int(arg)
        elif opt == "-u":    username = arg
        elif opt == "-w":    password = arg
        elif opt == "-m":    timeout = int(arg)
        elif opt == "-a":    accountCreation = True
        elif opt == "-o":    offset = int(arg)
        elif opt == "-y":    pythonHammer = True
        elif opt == "-h":
            usage()
            sys.exit()

    server, port, path, tls = createaccounts.parseURL(url)
    
    if pythonHammer or accountCreation:
        # This is nasty, but httplib.HTTP(S)Connection keeps
        # socket private, so we cannot set per socket timeouts. 
        socket.setdefaulttimeout(timeout)
    else:
        if pycurl is None:
            print "pycurl is not available. Either install pycurl,"
            print "or run hammer with -y to run the httplib version of hammer."
            sys.exit(1)
        # We should ignore SIGPIPE when using pycurl.NOSIGNAL - see
        # the libcurl tutorial for more info.
        try:
            import signal
            from signal import SIGPIPE, SIG_IGN
            signal.signal(signal.SIGPIPE, signal.SIG_IGN)
        except ImportError:
            pass

    global outputLock
    outputLock = Lock()
    
    LogEvent("Starting test with %d threads, %d iterations, %d bytes per file" % (threads, iterations, datalength))

    startTime = time.time()    

    # start up our threads
    cosmotesters = []
    first = offset
    for thread in range(offset, offset + threads):
        if accountCreation:
            c = TestCosmoAccountCreation(tls, server, port, path, first,
                                         iterations)
            first += iterations
        elif pythonHammer:
            u = username + str(thread)
            p = password + str(thread)
            url = "%s/home/%s/testfile.txt" % (path, u)
            c = TestCosmoPurePython(tls, server, port, url, datalength, 
                                    iterations, u, p)
        else:
            u = username + str(thread)
            p = password + str(thread)
            url = "http://%s:%s@%s:%d%s/home/%s/testfile.txt" % (u, p, server,
                                                                 port, path, u)
            c = TestCosmo(url, datalength, iterations, timeout)
        c.start()
        cosmotesters.append(c)

    failed = 0
    
    # wait for 'em all to finish
    for c in cosmotesters:
        c.join()
        if c.passed == False: failed += 1

    if failed > 0:
        sf = "FAILED"
    else:
        sf = "PASSED"
            
    endTime = time.time()

    elapsedTime = endTime - startTime
    dataTransfered = iterations * threads * datalength * 2 / 1024

    if failed: LogEvent("** WARNING: %d tests failed" % failed)
    LogEvent("%d tests passed" % (threads-failed))
    LogEvent("Success rate is %d%%" % ((threads-failed) / threads * 100))
    LogEvent("Total Elapsed Time: %d seconds" % elapsedTime)
    LogEvent("Total data transfered: %d KB" % dataTransfered)
    LogEvent("Throughput: %d KB/sec" % (dataTransfered / elapsedTime))
    LogEvent("Performance: %d req/sec" % (threads * iterations / elapsedTime))

    LogEvent("Ran %d threads through %d iterations." % (threads, iterations))
    LogEvent("Test completed: %s." % sf)

    if failed: sys.exit(2)    
        
if __name__ == "__main__":
    main(sys.argv[1:])    
