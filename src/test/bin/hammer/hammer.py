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
  -s        servername (default is cosmo-test.osafoundation.org)
  -p        port (default is 8201)
  -l        size of datafile to GET/PUT in bytes (default is 10000)
  -u        username (default is test; thread number is appended to username)
  -w        password (default is test; thread number is appended to password)
  -m        timeout setting in seconds (default is 30)
  -h        display this help text

Examples:
  hammer.py runs test using default parameters
  hammer.py -p 80 runs default test on port 80

Tip:
  Use createaccounts.py to set up a batch of test accounts before running
  this script.
  
"""

from threading import Thread
from threading import Lock
import pycurl
import sys, getopt, datetime, time
import string, random

def LogEvent(*attr):
    s = "\t".join(attr)
    outputLock.acquire()
    print "%s\t%s" % (datetime.datetime.now().isoformat(' '), s)
    outputLock.release()
    
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
#            print "#",
         self.passed = True

    def write(self, fp):
        curl = pycurl.Curl()
        curl.setopt(pycurl.URL, self.url)
        curl.setopt(pycurl.UPLOAD, 1)
        curl.setopt(pycurl.NOSIGNAL, 1)
        curl.setopt(pycurl.READFUNCTION, fp.read)
        if self.timeout > 0: curl.setopt(pycurl.TIMEOUT, self.timeout)
        try:
            start = time.clock()
            curl.perform()
            LogEvent("PUT", "%.3f" % (time.clock() - start))
            
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
            start = time.clock()
            curl.perform()
            LogEvent("GET", "%.3f" % (time.clock() - start))
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

def randomString(length = 10000):
    item = string.letters + string.digits
    return "".join([random.choice(item) for i in range(length)])

def usage():
    print __doc__
    
def main(argv):
    try:
        opts, args = getopt.getopt(argv, "t:i:s:p:l:u:w:m:ch")
    except getopt.GetoptError:
        usage()
        sys.exit(2)

    # establish defaults
    threads = 10
    iterations = 100
    server = "cosmo-demo.osafoundation.org"
    port = 8201
    datalength = 10000
    username = "test"
    password = "test"
    timeout = 30
    
    for (opt, arg) in opts:
        if opt == "-t":      threads = int(arg)
        elif opt == "-i":    iterations = int(arg)
        elif opt == "-s":    server = arg
        elif opt == "-p":    port = int(arg)
        elif opt == "-l":    datalength = int(arg)
        elif opt == "-u":    username = arg
        elif opt == "-w":    password = arg
        elif opt == "-m":    timeout = int(arg)
        elif opt == "-h":
            usage()
            sys.exit()

                
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
    for thread in range(1, threads + 1):
        u = username + str(thread)
        p = password + str(thread)
        url = "http://%s:%s@%s:%d/home/%s/testfile.txt" % (u, p, server, port, u)
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
    
    LogEvent("Ran %d threads through %d iterations." % (threads, iterations))
    LogEvent("Test completed: %s." % sf)

    if failed: sys.exit(2)    
        
if __name__ == "__main__":
    main(sys.argv[1:])    
