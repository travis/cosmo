ticket_view_lab_login = """
{"method": "waits.forElement", "params": {"link": "Sign up."}}
{"params": {"link": "Sign up."},  "method": "click"}
{"method": "waits.forElement", "params": {"id": "modalDialogTitle", "timeout": 40000}}
{"params": {"id": "tos"},  "method": "check"}
{"params": {"text": "{$random1}", "id": "username"},  "method": "type"}
{"params": {"text": "{$random1}", "id": "firstName"},  "method": "type"}
{"params": {"text": "{$random1}", "id": "lastName"},  "method": "type"}
{"params": {"text": "{$random1}@osafoundation.org", "id": "email"},  "method": "type"}
{"params": {"text": "{$random1}", "id": "password"},  "method": "type"}
{"params": {"text": "{$random1}", "id": "confirm"},  "method": "type"}
{"params": {"jsid": "{$btnsRight0}"},  "method": "click"}
{"method": "waits.forElement", "params": {"id": "modalDialogPrompt", "timeout": 40000}}
{"method": "waits.sleep", "params": {"milliseconds" : 1000}}
{"method": "click", "params": {"jsid" : "{$btnsCenter0}"}}
{"params": {"id": "loginDialogUsernameInput"},  "method": "click"}
{"params": {"text": "{$random1}", "id": "loginDialogUsernameInput"},  "method": "type"}
{"params": {"text": "{$random1}", "id": "loginDialogPasswordInput"},  "method": "type"}
{"method": "click", "params": {"id" : "loginSubmitButton"}}
"""

ticket_view_snarf_login = """
{"method": "waits.forElement", "params": {"link": "Sign up."}}
{"params": {"link": "Sign up."},  "method": "click"}
{"method": "waits.forElement", "params": {"id": "modalDialogTitle", "timeout": 40000}}
{"params": {"text": "{$random1}", "id": "username"},  "method": "type"}
{"params": {"text": "{$random1}", "id": "firstName"},  "method": "type"}
{"params": {"text": "{$random1}", "id": "lastName"},  "method": "type"}
{"params": {"text": "{$random1}@osafoundation.org", "id": "email"},  "method": "type"}
{"params": {"text": "{$random1}", "id": "password"},  "method": "type"}
{"params": {"text": "{$random1}", "id": "confirm"},  "method": "type"}
{"params": {"jsid": "{$btnsRight0}"},  "method": "click"}
{"method": "waits.forElement", "params": {"id": "modalDialogPrompt", "timeout": 40000}}
{"method": "waits.sleep", "params": {"milliseconds" : 1000}}
{"method": "click", "params": {"jsid" : "{$btnsCenter0}"}}
{"params": {"id": "loginDialogUsernameInput"},  "method": "click"}
{"params": {"text": "{$random1}", "id": "loginDialogUsernameInput"},  "method": "type"}
{"params": {"text": "{$random1}", "id": "loginDialogPasswordInput"},  "method": "type"}
{"method": "click", "params": {"id" : "loginSubmitButton"}}
"""

ticket_view_settings = """
{"params": {"milliseconds": 4000},  "method": "waits.sleep"}
{"method": "waits.forElement", "params": {"id": "_month2_day28", "timeout": 40000}}
{"params": {"link": "Settings"},  "method": "click"}
{"params": {"milliseconds": 3000},  "method": "waits.sleep"}
{"params": {"jsid": "{$dialogTab1}"},  "method": "click"}
{"params": {"id": "showAccountBrowser"},  "method": "check"}
{"params": {"milliseconds": 3000},  "method": "waits.sleep"}
{"params": {"jsid": "{$btnsRight0}"},  "method": "click"}"""

from windmill.authoring import enable_collector, RunJsonFile
import windmill
lab_urls = ['http://lab.osaf.us', 'http://next.osaf.us', 'http://trunk.osaf.us']

def setup_module(module):
    if windmill.settings['TEST_URL'] in lab_urls:
        json = ticket_view_lab_login
    else:
        json =  ticket_view_snarf_login
    RunJsonFile('create_user.json', lines=json.splitlines())()
    RunJsonFile('ticket_view_settings.json', lines=ticket_view_settings.splitlines())()

def teardown_module(module):
    RunJsonFile('log_out.json', lines=['{"method": "click", "params": {"link" : "Log in"}}'])()