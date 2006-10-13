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

class CosmoCreateMove(seleniumunittest.SeleniumTestCase):
    
    def test_cosmo(self):
        sel = self.selenium
        sel.open("/scooby/main.page")
        sel.dblclick("id=hourDiv0-900")
        time.sleep(2)
        did = sel.get_eval("this.browserbot.getCurrentWindow().Cal.eventRegistry.getFirst().id")
        sel.dragdrop_div_cosmo("id=eventDivContent__${did}", "id=hourDiv4-1200")
        time.sleep(2)
        sel.dragdrop_div_cosmo("id=eventDivContent__${did}", "id=hourDiv1-900")
        time.sleep(2)
        sel.dragdrop_div_cosmo("id=eventDivContent__${did}", "id=hourDiv5-1400")