

class TestObject:
    
    def __init__(self, debug=0, mask=0, recurrence=1, appendVar='', printAppend='', appendDict={}, appendList=[], threadNum=None):
        
        self.debug = debug
        self.mask = mask
        self.results = []
        self.resultNames = []
        self.resultComments = []
        self.recurrence = recurrence
        self.appendVar = str(appendVar)
        self.printAppend = printAppend
        self.threadNum = threadNum
        self.appendDict = appendDict
        self.appendList = appendList
        # ctype=xml

    def printOut(self, string):
        
        if self.mask == 0:
            if self.threadNum is None:
                print '%s%s' % (self.printAppend, string)
            if self.threadNum is not None:
                print 'Thread %s :: %s%s' % (self.threadNum, self.printAppend, string)
            
    def testStart(self, testname):
        """
        Set test name
        """
        
        self.test = testname
        if self.debug > 0:
                self.printOut('Starting New Test :: %s' % self.test)
            
    def report(self, result, test=None, comment=None):
        
        if test is None:
            test = self.test
        
        self.results.append(result)
        self.resultNames.append(test)
        self.resultComments.append(comment)
        if result == True:
            if self.debug > 0:
                self.printOut("Passed :: Test %s :: %s" % (test, comment))
        if result == False:
            self.printOut("Failure :: Test %s :: %s" % (test, comment))
            
            
    def runRecurring(self):
        
        for r in range(self.recurrence):
            
            self.currentRecurrence = r
            self.recurringRun()
            
    def fullRun(self):
        
        self.startRun()
        try:
            self.runRecurring()
        except AttributeError:
            pass
        self.end()
            
    
            
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
        
        if self.mask < 3:
            
            if self.appendVar != '':
                print("UserAppend :: %s" % self.appendVar)
                
            if self.recurrence != 1:
                print("Recurrences :: %s" % str(self.recurrence))
            
            print("Failures :: %s" % failures)
            print("Passes :: %s" % passes)
            print("Total tests run :: %s" % count)
            
        