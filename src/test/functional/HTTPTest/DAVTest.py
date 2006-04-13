from HTTPTest import HTTPTest

class DAVTest(HTTPTest):
    
#    def pathiter(self, elemlist, path):
#        """
#        Returns a list containing all only elemlist.name matches in final path
#        """
#        
#        finalelems = []
#        
#        for elem in elemlist:
#            if elem.name == path[0]:
#                finalelems.append(elem)
#            
#            if elem is elemlist[-1]:
#                if finalelems[0] is None:
#                    print "Failed because final in last elem in current path did not have any name matches"
#                    return False
#                
#        if path[0] is path[-1]:
#             if finalelems[0] is not None:
#                 return finalelems
#             else:
#                 print "Failed because last path did not find any matching elements"
#                 return False
#             
#        if path[0] is not path[-1]:
#            if finalelems[0] is not None:
#                path.pop([0])
#                pathiter(finalelems)
#                
#
#    def elemiter(self, elem, strings):
#        """
#        Takes an element list and checks for string matches until it finds one or it fails
#        """
#        for s in strings:
#            if elem[0].text.find(s) != -1:
#                return elem
#            else:
#                if elem[0] is elem[-1]:
#                    print "Last element in elemiter did not match."
#                    return False
#                else:
#                    elem.pop([0])
#                    elemiter(elem, strings)
                    
#    def verifyInElementTree(self, elemlist, ):                
    
    def verifyDAVResponseItems(self, args=None, positive=None, negative=None, test=None, comment=None, inelement=None, inelementexpectedcount=None):
        """
        Method to verify elements exist and, if specified, verify that all strings in dict are in inelement from each {DAV:}response
        
        Required Attributes;
        args = single strings representing all the expected items in the response url
        
        Optional attributes;
        test = overwrite self.test
        comment = overwrite self.comment
        inelement = string represent find within each response element to search for positive list strings
            Optional attributes if inelement and positive are specified;
            positive = list of strings to search for in inelement
            negative = list of strings to search for not existing in inelement
            inelementexpectedcount = amount of inelement items, used if elements in response are greater than elements matching inelement
        """
        
        self.xmlParse()
        
        # ----- Set all necessary counts and attributes
        
        responseelementcount = 0
        itemexpectedcount = 0
        itemreturnedcount = 0
        positiveexpectedcount = 0
        positivepasscount = 0
        negativeexpectedcount = 0
        negativepasscount = 0
        inelementcount = 0
        inelementexpectedcount = 0
        
        if test is None:
            try:
                test = self.test
            except:
                test = None
            
        for arg in args:
            itemexpectedcount = itemexpectedcount+1    
            
        if positive is not None:
            for p in positive:
                positiveexpectedcount = positiveexpectedcount+1
            positiveexpectedcount = (positiveexpectedcount*itemexpectedcount)
       
        if negative is not None:
            for p in negative:
                negativeexpectedcount = negativeexpectedcount+1
            negativeexpectedcount = (negativeexpectedcount*itemexpectedcount)
            
        # ------ Super magic logic to verify all counts and matches
        
        rtest = self.xml_doc.findall('.//{DAV:}response')
        for t in rtest:
            responseelementcount = responseelementcount+1
            # -- For every arg match response url
            for item in args:
                if t[0].text.find('%s' % item) != -1:
                    itemreturnedcount = itemreturnedcount+1
                    # -- For each element matcing inelement
                    if inelement is not None:
                        ctest = t.find('.//%s' % inelement)
                        ctest.text = ctest.text.replace('\n ', '')
                        inelementcount = inelementcount+1
                        # -- For each element matching inelement verify text contains strings in positive list
                        if positive is not None:
                            for p in positive:
                                if ctest.text.find(p) != -1:
                                    positivepasscount = positivepasscount+1
                                else:
                                    self.printOut('FAILED to get %s in %s' % (p, t[0].text))
                        if negative is not None:
                            for p in negative:
                                if ctest.text.find(p) == -1:
                                    negativepasscount = negativepasscount+1
                                else:
                                    self.printOut('FAILED to get %s in %s' % (p, t[0].text))                        
                                    
        if inelementexpectedcount is not None:
            inelementexpectedcount = inelementcount
                    
        if responseelementcount == itemexpectedcount == itemreturnedcount \
                           and positiveexpectedcount == positivepasscount \
                           and negativeexpectedcount == negativepasscount \
                           and inelementcount == inelementexpectedcount:
            self.report(True, test='VerifyDAVResponseItems :: %s' % test , comment=comment)
        else:
            self.report(False, test='VerifyDAVResponseItems :: %s' % test, comment='Response_elements :: %s; Expected_item_matches :: %s; Returned_item_matches :: %s; Positive_expected_count :: %s; Positive_passed_count :: %s; Negative_expected_count :: %s; Negative_passed_count :: %s; InElement_expected_count :: %s; InElement_passed_count :: %s;' % 
                                                                            (responseelementcount, itemexpectedcount, itemreturnedcount,
                                                                              positiveexpectedcount, positivepasscount,
                                                                              negativeexpectedcount, negativepasscount,
                                                                              inelementexpectedcount, inelementcount))
                                                                              
                                                                              
    def verifyDAVResponseInElement(self, inresponse, inelement, positive=None, negative=None, test=None, comment=None):
        """
        Method to verify positive and/or negative string list exists in single response.element match
        """
        
        self.xmlParse()
        
        itemreturnedcount = 0
        positivepasscount = 0
        negativepasscount = 0
        inelementcount = 0
        
        if test is None:
            test = self.test
        
        rtest = self.xml_doc.findall('.//{DAV:}response')
        for t in rtest:
            if t[0].text.find('%s' % inresponse) != -1:
                itemreturnedcount = itemreturnedcount+1
                # -- For each element matcing inelement
                ctest = t.find('.//%s' % inelement)
                inelementcount = inelementcount+1
                # -- For each element matching inelement verify text contains strings in positive list
                if positive is not None:
                    for p in positive:
                        if ctest.text.find(p) != -1:
                            positivepasscount = positivepasscount+1
                        else:
                            self.printOut('FAILED to get %s in %s' % (p, t[0].text))
                if negative is not None:
                    for p in negative:
                        if ctest.text.find(p) == -1:
                            negativepasscount = negativepasscount+1
                        else:
                            self.printOut('FAILED to get %s in %s' % (p, t[0].text))            
                                
        if positive is None:
            positive = []
            
        if negative is None:
            negative = []

        if itemreturnedcount == inelementcount == 1 and positivepasscount == len(positive) and negativepasscount == len(negative):
            self.report(True, test='VerifyDAVResponseItemsInElement :: %s; %s in %s;' % (test, inelement, inresponse), comment=comment)
        else:
            self.report(False, test='VerifyDAVResponseItemsInElement :: %s; %s in %s;' % (test, inelement, inresponse), comment='Positive_expected_count :: %s; Positive_passed_count :: %s; Negative_expected_count :: %s; Negative_passed_count :: %s; Item_returned_count :: %s; InElement_returned_count :: %s' % 
                                                                            (len(positive), positivepasscount,
                                                                             len(negative), negativepasscount,
                                                                             itemreturnedcount, inelementcount))
   
   
#    def verifyElementInTree(self, treestring, positive=None, negative=None, test=None, comment=None):           
#        """
#        Method that iterates through elementtree object and verifies that all every string in positive exits in at least one element in specified
#            treestring.
#        """
        #### -------- This doesn't work yet
        
#        self.xmlParse()
#        
#        itemreturnedcount = 0
#        positivepasscount = 0
#        negativepasscount = 0
#        inelementcount = 0
#        
#        if test is None:
#            test = self.test
#            
#        treelist = treestring.split('.')
#            
#        def iteration(self, treeref, treelist, iterator, positive=None, negative=None):
#            """
#            Function loop for iteration
#            """
#             treeref = treeref.findall('.//%s' % treelist[iterator])
#             for tr in treeref:
#                 for tl in treelist:
#                     if positive is not None:
#                         positivecount = 0
#                         for p in positive:
#                             if treeref.text.find(p) != -1:
#                                    positivepasscount = positivepasscount+1
#                                else:
#                                    self.printOut('FAILED to get %s in %s' % (p, t[0].text))
#                         

                                                                                                                            
                                                                             
#                                                                             
#    def verifyDAVPropInElement(self, inelement, positive=None, negative=None, test=None, comment=None):
#        """
#        Method to verify positive and/or negative string list exists in single response.element match
#        """
#        
#        self.xmlParse()
#        
#        itemreturnedcount = 0
#        positivepasscount = 0
#        negativepasscount = 0
#        inelementcount = 0
#        
#        if test is None:
#            test = self.test
#        
#        print self.xml_doc.getiterator()
#        print self.xml_doc.attrib.get('{DAV:}href')
#        
#        rtest = self.xml_doc.findall('.//{DAV:}prop')
#        for t in rtest:
#            # -- For each element matcing inelement
#            ctest = t.find('.//%s' % inelement)
#            inelementcount = inelementcount+1
#            # -- For each element matching inelement verify text contains strings in positive list
#            if positive is not None:
#                for p in positive:
#                    if ctest.text.find(p) != -1:
#                        positivepasscount = positivepasscount+1
#                    else:
#                        self.printOut('FAILED to get %s in %s' % (p, t[0].text))
#            if negative is not None:
#                for p in negative:
#                    if ctest.text.find(p) == -1:
#                        negativepasscount = negativepasscount+1
#                    else:
#                        self.printOut('FAILED to get %s in %s' % (p, t[0].text))            
#                                
#        if positive is None:
#            positive = []
#            
#        if negative is None:
#            negative = []
#
#        if itemreturnedcount == inelementcount == 1 and positivepasscount == len(positive) and negativepasscount == len(negative):
#            self.report(True, test='VerifyDAVItemsInElement :: %s; %s;' % (test, inelement), comment=comment)
#        else:
#            self.report(False, test='VerifyDAVItemsInElement :: %s; %s' % (test, inelement), comment='Positive_expected_count :: %s; Positive_passed_count :: %s; Negative_expected_count :: %s; Negative_passed_count :: %s; Item_returned_count :: %s; InElement_returned_count :: %s' % 
#                                                                            (len(positive), positivepasscount,
#                                                                             len(negative), negativepasscount,
#                                                                             itemreturnedcount, inelementcount))
