
cal_cavas_setup_json = """{"params": {"timeout": 40000, "id": "cosmoViewToggleCalViewSelector"}, "method": "waits.forElement"}
{"params": {"id": "cosmoViewToggleCalViewSelector"}, "method": "click"}"""

from windmill.authoring import RunJsonFile

def setup_module(module):
    RunJsonFile('calcanva_setup.json', lines=cal_cavas_setup_json.splitlines())()
