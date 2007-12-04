open_json = """{"params": {"url": "\/pim"},  "method": "open"}"""

from windmill.authoring import RunJsonFile

def setup_module(module):
    RunJsonFile('setup_test_pim.json', lines=[open_json])()
    