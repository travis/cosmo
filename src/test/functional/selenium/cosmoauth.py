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
import time, re

class CosmoAuth(seleniumunittest.SeleniumTestCase):
    """Cosmo Authorization Test Case"""
    _authorization_ = True

    def test_cosmo(self):
        """Main method for authorization to cosmo"""
        sel = self.selenium
        sel.open("/")
        sel.waitForPageToLoad(3 * 1000) # Wait 3 seconds
        sel.clickAndWait(link="Log in to Cosmo")
        # sel.wait_for_page_to_load("30000")
        sel.type("j_username", self.userinfo['username'])
        sel.type("j_password", self.userinfo['password'])
        sel.clickAndWait(id="submitButtonText")
        self.authorized = True
