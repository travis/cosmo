

class TestObject:
    
    def __init__(self, debug=0, mask=0):
        
        self.debug = debug
        self.mask = mask
        self.results = []
        self.resultnames = []
        self.resultcomments = []

    def printout(self, string):
        
        if self.mask == 0:
            print string
            
    def teststart(self, testname):
        """
        Set test name
        """
        
        self.test = testname
        if self.debug > 0 and self.mask == 0:
            print 'Starting New Test :: %s' % self.test
            
    def report(self, result, test=None, comment=None):
        
        self.results.append(result)
        self.resultnames.append(test)
        self.resultcomments.append(comment)
        if result == True:
            if self.debug > 0:
                self.printout("Passed :: Test %s :: %s" % (test, comment))
        if result == False:
            self.printout("Failure :: Test %s :: %s" % (test, comment))
            
    
            
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
        
    