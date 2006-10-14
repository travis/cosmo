#   Copyright (c) 2006 Open Source Applications Foundation
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

from pyselenium import seleniumunittest
import time

class CosmoRegister(seleniumunittest.SeleniumTestCase):
    """Registration test case"""
    _registration_ = True
    
    def test_cosmo(self):
        """Main registration test"""
        sel = self.selenium
        sel.open("/")
        sel.click(link='Create an Account')
        sel.type("//input[@id='username']", self.userinfo['username'])
        sel.type("//input[@id='firstName']", self.userinfo['username'].capitalize())
        sel.type("//input[@id='lastName']", self.userinfo['password'].capitalize())
        sel.type("//input[@id='eMail']", "%s@osafoundation.org" % self.userinfo['username'])
        sel.type("//input[@id='password']", self.userinfo['password'])
        sel.type("//input[@id='confirm']", self.userinfo['password'])
        sel.click("//input[@value='Submit']")
        time.sleep(2)
        sel.click("//input[@value='Close']")
        self.account_created = True
        
    def tearDown(self):
        """Teardown must remove user account"""
        import httplib, base64
        
        seleniumunittest.SeleniumTestCase.tearDown(self)
        
        # Create auth headers. If self.rootpass is set then use that, if not default to cosmo default rootpass
        if hasattr(self, 'rootpass'):
            auth = 'Basic %s' % base64.encodestring('%s:%s' % ('root', self.rootpass)).strip()
        else:
            auth = 'Basic %s' % base64.encodestring('%s:%s' % ('root', 'cosmo')).strip()
        
        # Figure out the hosname and port and create the connection object
        if self.url.find('https') is not -1:
            striped = self.url.strip('https://')
        else:
            striped = self.url.strip('https://')
        if striped.find(':') is not -1:
            address = striped.split(':')
            address[1] = int(address[1].strip('/'))
        else:
            address = [striped.strip('/'), 80]
        connection = httplib.HTTPConnection(address[0], address[1])
        
        headers = {"Authorization": auth}
        path = '/cosmo/cmp/user/%s' % self.userinfo['username']
        connection.request('DELETE', path, body=None, headers=headers)
        response = connection.getresponse()
        if response.status != 204:
            raise 'Failed to remove user account \n Status: %s' % response.status
        

# if __name__ == "__main__":
#     import sys
#     seleniumunittest.main([sys.argv[0]], options={'server':'~/tmp/selenium-remote-control-0.8.1/server/selenium-server.jar',
#                                                   'username':'test', 'password':'testpass'})