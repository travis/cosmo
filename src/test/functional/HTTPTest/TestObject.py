

class TestObject:
    
    def __init__(self, debug=0, mask=0):
        
        self.debug = debug
        self.mask = mask
        self.results = []
        self.resultNames = []
        self.resultComments = []

    def printOut(self, string):
        
        if self.mask == 0:
            print string
            
    def testStart(self, testname):
        """
        Set test name
        """
        
        self.test = testname
        if self.debug > 0 and self.mask == 0:
            print 'Starting New Test :: %s' % self.test
            
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
        
        print("Failures :: %s" % failures)
        print("Passes :: %s" % passes)
        print("Total tests run :: %s" % count)
        
    