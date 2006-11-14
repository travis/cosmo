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

class CosmoRemoveEvents(seleniumunittest.SeleniumTestCase):
    
    def test_cosmo(self):
        sel = self.selenium
        sel.open('/cosmo/pim/pim.page')
        sel.storeId(1, 'did')
        sel.click(id='eventDivContent__${did}')
        time.sleep(2)
        sel.click(id='removeButtonText')
        time.sleep(2)
        sel.click(id='removeButtonDialogText')
        time.sleep(2)
        sel.open('/cosmo/pim/pim.page')
        time.sleep(2)
        sel.storeId(1, 'did')
        time.sleep(2)
        sel.click(id='eventDivContent__${did}')
        time.sleep(2)
        sel.click(id='removeButtonText')
        time.sleep(2)
        sel.click(id='removeButtonDialogText')
        time.sleep(2)
        sel.open('/cosmo/pim/pim.page')
        time.sleep(2)
        sel.storeId(1, 'did')
        time.sleep(2)
        sel.click(id='eventDivContent__${did}')
        time.sleep(2)
        sel.click(id='removeButtonText')
        time.sleep(2)
        sel.click(id='removeButtonDialogText')
        time.sleep(2)
        sel.open('/cosmo/pim/pim.page')
        time.sleep(2)
        sel.storeId(1, 'did')
        sel.click(id='eventDivContent__${did}')
        sel.click(id='removeButtonText')
        sel.click(id='removeButtonDialogText')
        sel.open('/cosmo/pim/pim.page')
        time.sleep(2)
        sel.storeId(1, 'did')
        sel.click(id='eventDivContent__${did}')
        time.sleep(2)
        sel.click(id='removeButtonText')
        sel.click(id='removeButtonDialogText')

        
if __name__ == "__main__":

    from pyselenium import seleniumunittest
    seleniumunittest.main(options={#'server': '~/tmp/selenium-remote-control-0.8.2-SNAPSHOT/server/selenium-server.jar',
                                   'selenium' : '~/Documents/projects/tools/selenium',},
                          testfiles=('cosmoregister.py', 'cosmoauth.py', 'cosmocreate.py', 'cosmormevents.py'),
                          )
    