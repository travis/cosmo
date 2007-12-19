
cal_cavas_setup_json = """{"method": "waits.sleep", "params": {"milliseconds" : 1000}}
{"method": "click", "params": {"jsid": "{$calView}"}}"""

from windmill.authoring import RunJsonFile

def setup_module(module):
    RunJsonFile('calcanva_setup.json', lines=cal_cavas_setup_json.splitlines())()
