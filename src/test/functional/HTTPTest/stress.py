
"""
stress.py defines functions for executing stress test class definitions inherited from TestObject
"""

import threading
#import code
#import string

class TestExecutionThread(threading.Thread):

   def __init__(self, importDict, optsDict, threadNum):
       
       threading.Thread.__init__(self)
       self.opts = ''
       self.tresults = []
       self.tresultNames = []
       self.importDict = importDict
       self.threadNum = threadNum
       self.alive = True
       
       joinlist = [] 
       
       for o, v in optsDict.items():
               joinlist.append('%s=%s' % (o, v))
               
       self.opts = ', '.join(joinlist)
                  

   def run (self):
       
       instanceList = []
       classList = []
       
       for l, c in self.importDict.items():
           
           importExec = 'from %s import %s' % (l, c)
           exec '%s' % (importExec)
           classList.append(c)
       
       for c in classList:
           
           cl = c.lower()
           instanceExec = '%s = %s(%s)' % (cl, c, self.opts)     
           exec(compile(instanceExec, '', 'exec'))
          
           listExec = 'instanceList.append(%s)' % cl
           print listExec
           exec '%s' % (listExec)
           
       for i in instanceList:
           
           i.fullRun()

           for r in range(len(i.results)):
               self.tresultNames.append('Thread %s :: Class %s :: %s' % (str(self.threadNum), i.__class__.__name__, i.resultNames[r]))
               self.tresults.append(i.results[r])
               
       self.alive = False

       
def checkAlive(threadList):
    
    for thread in threadList:
        
        if thread.alive is True:
            time.sleep(1)
            checkAlive(threadList)
            


def runThreaded(importDict, threads, debug, optsDict={}):
    """
    runThreaded
    """
    
    results = []
    resultNames = []
    threadList = []
    
    for r in range(threads):
        print r
        
        optsDict.update({'appendDict':'{"username":%s}' % r, 'threadNum':r})
        test = TestExecutionThread(importDict=importDict, optsDict=optsDict, threadNum=r)
        test.start()
        threadList.append(test)
        
        
#        for t in test.results:
#            resultNames.append('Thread %s :: Class %s :: %s' % (r, test.__class__.__name__, test.resultNames[t]))
#            results.append(test.results[t])
        
    
    passes = 0
    failures = 0    

    checkAlive(threadList)
    
    for t in threadList:
        for r in range(len(t.tresults)):
           results.append(t.tresults[r]) 
           resultNames.append(t.tresultNames[r])
        
    for s in range(len(results)):
        print results[s]
        
    for s in range(len(results)):
        if results[s] is False:
            print "Failure :: %s" % resultNames[s]
            failures = failures+1
        if results[s] is True:
            print "Success :: %s" % resultNames[s]
            passes = passes+1
    
    print "PASSED %s :: FAILED %s :: CLASSES %s :: THREADS %s :: RECURRENCES %s :: %s TOTAL TESTS RUN" % (passes, failures, len(importDict), threads, optsDict['recurrence'], passes+failures)        
    
    
    
if __name__ == "__main__":
    
    import sys
    import time
    
    host = 'localhost'
    port = '8080'
    path = '/cosmo'
    debug = 1
    recurrence = 1
    threads = 2
    wait = 60
    
    testsDict = {'cosmo_500events':'Cosmo500Events',
                  'cosmo_basicquery':'CosmoBasicQuery',
                  'cosmo_bugs':'CosmoBugs',
                  'cosmo_chandler061':'CosmoChandlerZeroPointSixPointOne',
                  'cosmo_freebusy':'CosmoFreeBusy',
                  'cosmo_invalid':'CosmoInvalid',
                  'cosmo_limitexpand':'CosmoLimitExpand',
                  'cosmo_mkcalendar':'CosmoMkcalendar',
                  'cosmo_multiget':'CosmoMultiget',
                  'cosmo_ticket':'CosmoTicket',
                  'cosmo_timerangequery':'CosmoTimeRangeQuery'
                  }
    
    def parseTests(string):
        
        l = ','.split(string)
        tests = {}
        for test in l:
            t = ':'.split(string)
            tests.add['%s'%t[0]] = '%s' % t[1]
            
        return tests 
    
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
        elif args[0] == "threads":
            threads = int(args[1])  
        elif args[0] == "tests":
            testsDict = parseTests(args[1])
    
    optsDict = {'host':'\'%s\'' % host, 'port':'%s' % port, 'path':'\'%s\'' % path, 'mask':'0', 'recurrence':recurrence}
    
    runThreaded(testsDict, threads=threads, optsDict=optsDict, debug=debug)

    
    
    
    
    
    
    
    
    
    
    
    
    