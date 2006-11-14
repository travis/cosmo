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

class CosmoCreateStacked(seleniumunittest.SeleniumTestCase):
    
    def test_cosmo(self):
        sel = self.selenium
        sel.open("/cosmo/pim/pim.page")
        
        for x in range(10):
            time.sleep(2)
            sel.dblclick(id='hourDiv3-1000')