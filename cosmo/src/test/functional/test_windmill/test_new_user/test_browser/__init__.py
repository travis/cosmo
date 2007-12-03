
test_settings_json = """{"params": {"link": "Settings"}, "method": "click"}
{"params": {"id": "password"}, "method": "click"}
{"params": {"text": "bobber", "id": "password"}, "method": "type"}
{"params": {"text": "bobbers", "id": "confirm"}, "method": "type"}
{"params": {"jsid": "{$btnsRight0}"}, "method": "click"}
{"method": "waits.sleep", "params": {"milliseconds" : 3000}}
{"params": {"id": "modalDialogContent"}, "method": "asserts.assertNode"}
{"params": {"validator": "Passwords do not match.", "id": "modalDialogContent"}, "method": "asserts.assertText"}
{"params": {"id": "confirm"}, "method": "click"}
{"params": {"text": "bobber", "id": "confirm"}, "method": "type"}
{"params": {"jsid": "{$btnsRight0}"}, "method": "click"}
{"method": "waits.sleep", "params": {"milliseconds" : 3000}}
{"params": {"link": "Settings"}, "method": "click"}
{"method": "waits.sleep", "params": {"milliseconds" : 2000}}
{"params": {"jsid": "{$dialogTab1}"}, "method": "click"}
{"params": {"id": "showAccountBrowser"}, "method": "check"}
{"params": {"id": "showAccountBrowser"}, "method": "asserts.assertChecked"}
{"params": {"jsid": "{$btnsRight0}"}, "method": "click"}
{"method": "waits.sleep", "params": {"milliseconds" : 3000}}
{"params": {"link": "Account Browser"}, "method": "asserts.assertNode"}
{"params": {"link": "Settings"}, "method": "click"}
{"params": {"jsid": "{$dialogTab1}"}, "method": "click"}
{"params": {"id": "showAccountBrowser"}, "method": "asserts.assertChecked"}
{"params": {"id": "showAccountBrowser"}, "method": "check"}
{"params": {"jsid": "{$btnsRight0}"}, "method": "click"}
{"method": "waits.sleep", "params": {"milliseconds" : 2000}}"""

from windmill.authoring import RunJsonFile

def setup_module(module):
    RunJsonFile('settings.json', lines=test_settings_json.splitlines())()
