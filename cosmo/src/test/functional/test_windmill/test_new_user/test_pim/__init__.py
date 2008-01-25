open_lab_json = """{"params": {"url": "\/pim"},  "method": "open"}"""
open_snarf_json = """{"params": {"url": "\/chandler\/pim"},  "method": "open"}"""


from windmill.authoring import RunJsonFile
import windmill

lab_urls = ['http://lab.osaf.us', 'http://next.osaf.us', 'http://trunk.osaf.us']

def setup_module(module):
    if windmill.settings['TEST_URL'] in lab_urls:
        json = open_lab_json
    else:
        json = open_snarf_json
    RunJsonFile('setup_test_pim.json', lines=[json])()
    