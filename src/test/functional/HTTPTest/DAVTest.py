from HTTPTest import HTTPTest

class DAVTest(HTTPTest):
    
    def verifyItems(self, args=None, positive=None, negative=None, test=None, comment=None, inelement=None, inelementexpectedcount=None):
        """
        Method to verify elements exist and, if specified, verify that all strings in dict are in inelement.
        
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
        
        self.xmlparse()
        
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
        
        if test == None:
            test = self.test
            
        for arg in args:
            itemexpectedcount = itemexpectedcount+1    
            
        if positive != None:
            for p in positive:
                positiveexpectedcount = positiveexpectedcount+1
            positiveexpectedcount = (positiveexpectedcount*itemexpectedcount)
       
        if negative != None:
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
                    if inelement != None:
                        ctest = t.find('.//%s' % inelement)
                        inelementcount = inelementcount+1
                        # -- For each element matching inelement verify text contains strings in positive list
                        if positive != None:
                            for p in positive:
                                if ctest.text.find(p) != -1:
                                    positivepasscount = positivepasscount+1
                                else:
                                    self.printout('FAILED to get %s in %s' % (p, t[0].text))
                        if negative != None:
                            for p in negative:
                                if ctest.text.find(p) == -1:
                                    negativepasscount = negativepasscount+1
                                else:
                                    self.printout('FAILED to get %s in %s' % (p, t[0].text))                        
                                    
        if inelementexpectedcount != None:
            inelementexpectedcount = inelementcount
                    
        if responseelementcount == itemexpectedcount == itemreturnedcount \
                           and positiveexpectedcount == positivepasscount \
                           and negativeexpectedcount == negativepasscount \
                           and inelementcount == inelementexpectedcount:
            self.report(True, test='VerifyItems :: %s' % test , comment=comment)
        else:
            self.report(False, test='VerifyItems :: %s' % test, comment='Response_elements :: %s; Expected_item_matches :: %s; Returned_item_matches :: %s; Positive_expected_count :: %s; Positive_passed_count :: %s; Negative_expected_count :: %s; Negative_passed_count :: %s; InElement_expected_count :: %s; InElement_passed_count :: %s;' % 
                                                                            (responseelementcount, itemexpectedcount, itemreturnedcount,
                                                                              positiveexpectedcount, positivepasscount,
                                                                              negativeexpectedcount, negativepasscount,
                                                                              inelementexpectedcount, inelementcount))
                                                                              
                                                                              
    def verifyInElement(self, response, inelement, positive, negative=None, test=None, comment=None):
        """
        Method to verify positive and negative string list exists in single response.element match
        """
        
        itemreturnedcount = 0
        positivepasscount = 0
        negativepasscount = 0
        inelementcount = 0
        
        if test == None:
            test = self.test
        
        rtest = self.xml_doc.findall('.//{DAV:}response')
        for t in rtest:
            if t[0].text.find('%s' % response) != -1:
                itemreturnedcount = itemreturnedcount+1
                # -- For each element matcing inelement
                ctest = t.find('.//%s' % inelement)
                inelementcount = inelementcount+1
                # -- For each element matching inelement verify text contains strings in positive list
                for p in positive:
                    if ctest.text.find(p) != -1:
                        positivepasscount = positivepasscount+1
                    else:
                        self.printout('FAILED to get %s in %s' % (p, t[0].text))
                if negative != None:
                    for p in negative:
                        if ctest.text.find(p) == -1:
                            negativepasscount = negativepasscount+1
                        else:
                            self.printout('FAILED to get %s in %s' % (p, t[0].text))                

        if itemreturnedcount == inelementcount == 1 and positivepasscount == len(positive) and negativepasscount == len(negative):
            self.report(True, test='VerifyItemsInElement :: %s; %s in %s;' % (test, inelement, response), comment=comment)
        else:
            self.report(False, test='VerifyItemsInElement :: %s; %s in %s;' % (test, inelement, response), comment='Positive_expected_count :: %s; Positive_passed_count :: %s; Negative_expected_count :: %s; Negative_passed_count :: %s; Item_returned_count :: %s; InElement_returned_count :: %s' % 
                                                                            (len(positive), positivepasscount,
                                                                             len(negative), negativepasscount,
                                                                             itemreturnedcount, inelementcount))

