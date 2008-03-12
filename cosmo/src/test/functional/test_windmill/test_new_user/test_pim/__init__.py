open_lab_json = """{"params": {"url": "\/pim"},  "method": "open"}
{"method": "waits.forElement", "params": {"timeout": "40000", "id": "_month2_day28"}}"""

open_snarf_json = """{"params": {"url": "\/chandler\/pim"},  "method": "open"}
{"method": "waits.forElement", "params": {"timeout": "40000", "id": "_month2_day28"}}"""

from windmill.authoring import RunJsonFile
import windmill

lab_urls = ['http://lab.osaf.us', 'http://next.osaf.us', 'http://d10test.osaf.us', 'http://trunk.osaf.us']

def setup_module(module):
    if windmill.settings['TEST_URL'] in lab_urls:
        json = open_lab_json
    else:
        json = open_snarf_json
    RunJsonFile('setup_test_pim.json', lines=json.splitlines())()
    
