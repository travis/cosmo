test_settings_setup_lab_json = """{"params": {"link": "Settings"}, "method": "click"}
{"params": {"id": "modalDialogContent"}, "method": "waits.forElement"}
{"params": {"jsid": "{$dialogTab1}"}, "method": "click"}
{"params": {"id": "showAccountBrowser"}, "method": "check"}
{"params": {"id": "showAccountBrowser"}, "method": "asserts.assertChecked"}
{"params": {"jsid": "{$btnsRight0}"}, "method": "click"}
{"method": "waits.sleep", "params": {"milliseconds" : 3000}}
{"params": {"link": "Account Browser"}, "method": "asserts.assertNode"}
{"params": {"jsid": "{$calView}"},  "method": "click"}
{"params": {"milliseconds": "3000"},  "method": "waits.sleep"}
{"params": {"id": "hourDiv1-1000"},  "method": "doubleClick"}
{"params": {"milliseconds": "3000"},  "method": "waits.sleep"}
{"params": {"id": "hourDiv1-1200"},  "method": "doubleClick"}
{"params": {"milliseconds": "3000"},  "method": "waits.sleep"}
{"params": {"id": "hourDiv4-1200"},  "method": "doubleClick"}
{"params": {"milliseconds": "3000"},  "method": "waits.sleep"}
{"params": {"url": "\/browse\/{$random}"},  "method": "open"}
{"params": {"id": "contentWrapper"}, "method": "waits.forElement"}"""

test_settings_setup_snarf_json = """{"params": {"link": "Settings"}, "method": "click"}
{"params": {"id": "modalDialogContent"}, "method": "waits.forElement"}
{"params": {"jsid": "{$dialogTab1}"}, "method": "click"}
{"params": {"id": "showAccountBrowser"}, "method": "check"}
{"params": {"id": "showAccountBrowser"}, "method": "asserts.assertChecked"}
{"params": {"jsid": "{$btnsRight0}"}, "method": "click"}
{"method": "waits.sleep", "params": {"milliseconds" : 3000}}
{"params": {"link": "Account Browser"}, "method": "asserts.assertNode"}
{"params": {"jsid": "{$calView}"},  "method": "click"}
{"params": {"milliseconds": "3000"},  "method": "waits.sleep"}
{"params": {"id": "hourDiv1-1000"},  "method": "doubleClick"}
{"params": {"milliseconds": "3000"},  "method": "waits.sleep"}
{"params": {"id": "hourDiv1-1200"},  "method": "doubleClick"}
{"params": {"milliseconds": "3000"},  "method": "waits.sleep"}
{"params": {"id": "hourDiv4-1200"},  "method": "doubleClick"}
{"params": {"milliseconds": "3000"},  "method": "waits.sleep"}
{"params": {"url": "\/chandler\/browse\/{$random}"},  "method": "open"}
{"params": {"id": "contentWrapper"}, "method": "waits.forElement"}"""

test_settings_teardown_json = """{"params": {"url": "\/pim"},  "method": "open"}
{"params": {"link": "Settings"},  "method": "click"}
{"params": {"milliseconds": "2000"},  "method": "waits.sleep"}
{"params": {"jsid": "{$dialogTab1}"},  "method": "click"}
{"params": {"id": "showAccountBrowser"},  "method": "check"}
{"params": {"jsid": " {$btnsRight0}"},  "method": "click"}
{"params": {"milliseconds": "3000"},  "method": "waits.sleep"}
{"method": "waits.sleep", "params": {"milliseconds" : 2000}}"""

from windmill.authoring import RunJsonFile
import windmill

lab_urls = ['http://lab.osaf.us', 'http://next.osaf.us', 'http://trunk.osaf.us']

def setup_module(module):
    if windmill.settings['TEST_URL'] in lab_urls:
        json = test_settings_setup_lab_json
    else:
        json = test_settings_setup_snarf_json
    RunJsonFile('setup_test_browser.json', lines=json.splitlines())()

def teardwon_module(module):
    RunJsonFile('teardown_test_browser.json', lines=test_settings_teardown_json.splitlines())()
