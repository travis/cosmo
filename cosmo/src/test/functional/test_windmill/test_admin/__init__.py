login_with_root_lab_json = """{"method": "type", "params": {"id" : "loginDialogUsernameInput", "text": "root"}}
{"params": {"milliseconds": 500}, "method": "waits.sleep"}
{"method": "type", "params": {"id" : "loginDialogPasswordInput", "text": "cosmo"}}
{"params": {"milliseconds": 500}, "method": "waits.sleep"}
{"method": "click", "params": {"id" : "loginSubmitButton"}}
{"method":"reWriteAlert", "params":{}}
{"method": "waits.forElement", "params": {"id": "contentWrapper", "timeout": 40000}}"""

login_with_root_snarf_json = """{"method": "click", "params": {"link" : "Log in to Chandler Server"}}
{"method": "waits.forElement", "params": {"id": "loginDialogFormContainer", "timeout": 40000}}
{"params": {"milliseconds": 5000}, "method": "waits.sleep"}
{"method": "type", "params": {"id" : "loginDialogUsernameInput", "text": "root"}}
{"params": {"milliseconds": 500}, "method": "waits.sleep"}
{"method": "type", "params": {"id" : "loginDialogPasswordInput", "text": "cosmo"}}
{"params": {"milliseconds": 500}, "method": "waits.sleep"}
{"method": "click", "params": {"id" : "loginSubmitButton"}}
{"method":"reWriteAlert", "params":{}}
{"method": "waits.forElement", "params": {"id": "contentWrapper", "timeout": 40000}}"""

logout = """{"method": "click", "params": {"link" : "Log out"}}
{"params": {"url": "\/"}, "method": "open"}
{"method": "waits.forElement", "params": {"link" : "Log in to Chandler Server"}}
{"params": {"milliseconds": 1000}, "method": "waits.sleep"}
"""

from windmill.authoring import RunJsonFile
import windmill

lab_urls = ['http://lab.osaf.us', 'http://next.osaf.us', 'http://trunk.osaf.us']

def setup_module(module):
    if windmill.settings['TEST_URL'] in lab_urls:
        json = ""
    else:
        json = login_with_root_snarf_json
    RunJsonFile('login_with_root_user.json', lines=json.splitlines())()
    
def teardown_module(module):
  if windmill.settings['TEST_URL'] in lab_urls:
        json = ""
  else:
        RunJsonFile('log_out.json', lines=logout.splitlines())()

    
    

