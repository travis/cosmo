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
# createaccounts.py: Create test accounts on a Cosmo server.
#


"""
Create Cosmo Test Accounts

Creates test user accounts on a Cosmo server. Each test account is created
with username "test{seqnum}" where {seqnum} is a number created from the
sequence determined by the first sequence number parameter and the number
of accounts parameter. The password for each account is the same as the
account's username.

Usage: python createaccounts.py [options]

Options:
  -s        servername (default is cosmo-test.osafoundation.org)
  -p        port (default is 8201)
  -u        username (default is root)
  -w        password (default is cosmo)
  -n        number of accounts to create (default is 100)
  -f        first account number in sequence (default is 1)
  -h        display this help text

Examples:
  createaccounts.py Create 100 accounts starting with test1.
  createaccounts.py -f 101 -n 100 Create accounts test101 through test200.
"""

from threading import Thread
import pycurl
import sys, getopt, time

def createAccount(server, port, username, password, adminuser, adminpass):
    xmlCreateAccount = """<?xml version="1.0" encoding="utf-8" ?> 
<user xmlns="http://osafoundation.org/cosmo/CMP">
  <username>%s</username>
  <password>%s</password>
  <firstName>Tommy</firstName>
  <lastName>Tester</lastName>
  <email>%s@nojunkmailplease.com</email>
</user>
""" % (username, password, username)

    body = StringAsFile(xmlCreateAccount.encode("utf-8"))
    url = "http://%s:%s@%s:%d/cmp/user/%s" % (adminuser, adminpass, server, port, username)
    response = StringAsFile()
    curl = pycurl.Curl()
    curl.setopt(pycurl.URL, url)
    curl.setopt(pycurl.UPLOAD, 1)
    curl.setopt(pycurl.NOSIGNAL, 1)
    curl.setopt(pycurl.READFUNCTION, body.read)
    curl.setopt(pycurl.HTTPHEADER, ["Content-Length: %d" % len(str(body)), "Content-Type: text/xml"])
    curl.setopt(pycurl.VERBOSE, 0)
    curl.setopt(pycurl.HEADERFUNCTION, response.write)
    curl.setopt(pycurl.TIMEOUT, 10)
    try:
        curl.perform()
    except:
        import traceback
        traceback.print_exc(file=sys.stderr, limit=1)
        sys.stderr.flush()
        return False

    code = curl.getinfo(pycurl.HTTP_CODE)
    if code == 201:
        print "Created user account %s" % username
        return True
    elif code == 204:
        print "WARNING: Account %s already exists." % username
        return True
    elif code == 401:
        print "\nERROR: Authorization error. Check username and password."
        return False
    else:
        print "Unhandled response code: %d" % code
        return False

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

def usage():
    print __doc__
    
def main(argv):
    try:
        opts, args = getopt.getopt(argv, "s:p:u:w:n:f:h")
    except getopt.GetoptError:
        usage()
        sys.exit(2)

    # establish defaults
    server = "cosmo-demo.osafoundation.org"
    port = 8201
    username = "root"
    password = "cosmo"
    start = 1
    count = 100
    
    for (opt, arg) in opts:
        if opt =="-s":      server = arg
        elif opt == "-p":   port = int(arg)
        elif opt == "-u":   username = arg
        elif opt == "-w":   password = arg
        elif opt == "-n":   count = int(arg)
        elif opt == "-f":   start = int(arg)
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

    for i in range(start, start + count):
        if createAccount(server, port, \
            "test" + str(i), "test" + str(i), \
            username, password) == False:
            sys.exit(2)

if __name__ == "__main__":
    main(sys.argv[1:])    
