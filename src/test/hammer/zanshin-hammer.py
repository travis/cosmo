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
  -h        display this help text

Examples:
  hammer.py runs test using default parameters
  hammer.py -s http://localhost:80 runs default test on localhost port 80

Tip:
  Use createaccounts.py to set up a batch of test accounts before running
  this script.
  
"""

from twisted.internet import reactor, defer
import zanshin.util, zanshin.http
import sys, getopt, datetime, time
import string, random

import createaccounts

def LogEvent(*attr):
    s = "\t".join(attr)
    print "%s\t%s" % (datetime.datetime.now().isoformat(' '), s)
    
class TestCosmo(object):
    def __init__(self, url, desc, datalength, iterations, timeout):
        super(TestCosmo, self).__init__()
        self.url = url
        self.desc = desc
        self.datalength = datalength
        self.iterations = iterations
        self.strToWrite = ""
        self.strRead = ""
        self.iteration = 0
        self.timeout = timeout
        self.deferred = defer.Deferred()

    def run(self):
        self.put() # start off the cycle
         
    def put(self):
        if self.iteration >= self.iterations:
            self.deferred.callback(None)
        else:
            self.iteration += 1
            start = time.clock()

            d = sendRequest(self.url, 'PUT', randomString(self.datalength),
                            self.timeout)

            def putDone(resp):
                # @@@ Check response status & maybe raise
                LogEvent("PUT", "(%s) %.3f" % (self.desc, time.clock() - start))
                self.get()
                

            def putFailed(failure):
                LogEvent("PUT ERROR", "(%s) %s" % (self.desc, str(failure)))
                self.deferred.errback(failure)

            d.addCallback(putDone)
            d.addErrback(putFailed) # setting errback after callback allows
                                    # putDone to raise
             
    def get(self):
    
        start = time.clock()
        d = sendRequest(self.url, 'GET', None, self.timeout)

        def getDone(resp):
            # @@@ Check response status & maybe raise
            LogEvent("GET", "(%s) %.3f" % (self.desc, time.clock() - start))
            self.put()
            

        def getFailed(failure):
            LogEvent("GET ERROR", "(%s) %s" % (self.desc, str(failure)))
            self.deferred.errback(failure)

        d.addCallback(getDone)
        d.addErrback(getFailed) # setting errback after callback allows
                                # putDone to raise

def sendRequest(url, method, body, timeout):
    factory = zanshin.http.HTTPClientFactory()
    factory.useSSL, factory.username, factory.password, factory.host, \
       factory.port, path = zanshin.util.parseConfigFromUrl(url)

    request = zanshin.http.Request(method, path, {'connection':'close'}, body)
    request.timeout = timeout
    
    d = factory.addRequest(request)
    
    def cleanup(result):
        factory.stopFactory()
        return result
        
    d = d.addCallbacks(cleanup)
    
    return d



def randomString(length = 10000):
    item = string.letters + string.digits
    return "".join([random.choice(item) for i in range(length)])

def usage():
    print __doc__
    
startTime = None

failed = 0
    
def main(argv):
    try:
        opts, args = getopt.getopt(argv, "t:i:s:p:l:u:w:m:ch")
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
    
    for (opt, arg) in opts:
        if opt == "-t":      threads = int(arg)
        elif opt == "-i":    iterations = int(arg)
        elif opt == "-s":    server = arg
        elif opt == "-l":    datalength = int(arg)
        elif opt == "-u":    username = arg
        elif opt == "-w":    password = arg
        elif opt == "-m":    timeout = int(arg)
        elif opt == "-h":
            usage()
            sys.exit()

    server, port, path, tls = createaccounts.parseURL(url)

    def endTests(resultList):
        global failed
        failed = 0
        
        # Called once they're all finished
        for success, result in resultList:
            if not success: failed += 1
    
        if failed > 0:
            sf = "FAILED"
        else:
            sf = "PASSED"
                
        global startTime
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
        reactor.stop()
    

    def startTests():
        LogEvent("Starting test with %d threads, %d iterations, %d bytes per file" % (threads, iterations, datalength))

        global startTime
        startTime = time.time()
    
        # start up our tests
        deferreds = []
        for thread in range(1, threads + 1):
            u = username + str(thread)
            p = password + str(thread)
            url = "http://%s:%s@%s:%d%s/home/%s/testfile.txt" % (u, p, server, port, path, u)
            c = TestCosmo(url, str(thread), datalength, iterations, timeout)
            c.run()
            deferreds.append(c.deferred)
            
        defer.DeferredList(deferreds).addCallback(endTests)
            
            
    reactor.callLater(0, startTests)
    reactor.run()

    global failed
    if failed:
        sys.exit(2)
    else:
        sys.exit(0)


    
        
if __name__ == "__main__":
    main(sys.argv[1:])    
