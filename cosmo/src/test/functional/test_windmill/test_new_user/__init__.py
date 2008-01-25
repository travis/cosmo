create_user_json = """{"method": "click", "params": {"link" : "Create a new account"}}
{"method": "waits.forElement", "params": {"id": "loginDialogFormContainer"}}
{"params": {"milliseconds": 6000},  "method": "waits.sleep"}
{"method": "waits.forElement", "params": {"id": "modalDialogTitle", "timeout": 40000}}
{"params": {"id": "modalDialogTitle"},  "method": "asserts.assertNode"}
{"params": {"jsid": "{$btnsRight0}"},  "method": "click"}
{"params": {"milliseconds": 4000},  "method": "waits.sleep"}
{"params": {"text": "{$random}", "id": "username"},  "method": "type"}
{"params": {"text": "tester", "id": "firstName"},  "method": "type"}
{"params": {"text": "tester", "id": "lastName"},  "method": "type"}
{"method": "type", "params": {"id" : "email", "text": "{$random}@osafoundation.org"}}
{"params": {"text": "12345678901234567890", "id": "password"},  "method": "type"}
{"params": {"text": "12345678901234567890", "id": "confirm"},  "method": "type"}
{"params": {"jsid": "{$btnsRight0}"},  "method": "click"}
{"params": {"milliseconds": 2000},  "method": "waits.sleep"}
{"params": {"validator": "Maximum number of characters exceeded. (16)", "id": "modalDialogContent"},  "method": "asserts.assertText"}
{"params": {"text": "testers", "id": "password"},  "method": "type"}
{"params": {"text": "tester", "id": "confirm"},  "method": "type"}
{"params": {"jsid": "{$btnsRight0}"},  "method": "click"}
{"params": {"milliseconds": 2000},  "method": "waits.sleep"}
{"params": {"validator": "Passwords do not match.", "id": "modalDialogContent"},  "method": "asserts.assertText"}
{"params": {"text": "tester", "id": "password"},  "method": "type"}
{"params": {"jsid": "{$btnsRight0}"},  "method": "click"}
{"method": "waits.forElement", "params": {"id": "modalDialogPrompt", "timeout": 40000}}
{"params": {"validator": "You have successfully created your Chandler Server account.", "id": "modalDialogPrompt"},  "method": "asserts.assertText"}
{"params": {"jsid": "{$btnsCenter0}"},  "method": "click"}"""

create_user_json_lab = """{"params": {"link": "Sign up."},  "method": "click"}
{"params": {"id": "modalDialogTitle"},  "method": "asserts.assertNode"}
{"params": {"jsid": "{$btnsRight0}"},  "method": "click"}
{"params": {"id": "tos"},  "method": "check"}
{"params": {"text": "{$random}", "id": "username"},  "method": "type"}
{"params": {"text": "tester", "id": "firstName"},  "method": "type"}
{"params": {"text": "tester", "id": "lastName"},  "method": "type"}
{"method": "type", "params": {"id" : "email", "text": "{$random}@osafoundation.org"}}
{"params": {"text": "12345678901234567890", "id": "password"},  "method": "type"}
{"params": {"text": "12345678901234567890", "id": "confirm"},  "method": "type"}
{"params": {"jsid": "{$btnsRight0}"},  "method": "click"}
{"params": {"milliseconds": 2000},  "method": "waits.sleep"}
{"params": {"validator": "Maximum number of characters exceeded. (16)", "id": "modalDialogContent"},  "method": "asserts.assertText"}
{"params": {"text": "testers", "id": "password"},  "method": "type"}
{"params": {"text": "tester", "id": "confirm"},  "method": "type"}
{"params": {"jsid": "{$btnsRight0}"},  "method": "click"}
{"params": {"validator": "Passwords do not match.", "id": "modalDialogContent"},  "method": "asserts.assertText"}
{"params": {"text": "tester", "id": "password"},  "method": "type"}
{"params": {"jsid": "{$btnsRight0}"},  "method": "click"}
{"method": "waits.forElement", "params": {"id": "modalDialogPrompt", "timeout": 40000}}
{"params": {"validator": "You have successfully created your Chandler Hub account.", "id": "modalDialogPrompt"},  "method": "asserts.assertText"}
{"params": {"jsid": "{$btnsCenter0}"},  "method": "click"}"""

login_with_user_json = """{"method": "type", "params": {"id" : "loginDialogUsernameInput", "text": "{$random}"}}
{"params": {"milliseconds": 500}, "method": "waits.sleep"}
{"method": "type", "params": {"id" : "loginDialogPasswordInput", "text": "testers"}}
{"method": "click", "params": {"id" : "loginSubmitButton"}}
{"params": {"milliseconds": 500}, "method": "waits.sleep"}
{"method": "type", "params": {"id" : "loginDialogPasswordInput", "text": "tester"}}
{"method": "click", "params": {"id" : "loginSubmitButton"}}
{"method":"reWriteAlert", "params":{}}
{"method": "waits.forElement", "params": {"id": "_month2_day28", "timeout": 40000}}"""

from windmill.authoring import RunJsonFile
import windmill

lab_urls = ['http://lab.osaf.us', 'http://next.osaf.us', 'http://trunk.osaf.us']

def setup_module(module):
    if windmill.settings['TEST_URL'] in lab_urls:
        json = create_user_json_lab
    else:
        json = create_user_json
    RunJsonFile('create_user.json', lines=json.splitlines())()
    RunJsonFile('login_with_user_json.json', lines=login_with_user_json.splitlines())()
    
def teardown_module(module):
    RunJsonFile('log_out.json', lines=['{"method": "click", "params": {"link" : "Log out"}}'])()

    
    

