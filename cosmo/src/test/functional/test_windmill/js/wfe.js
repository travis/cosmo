var wfe = {};

wfe.test_setup = [
  {method: "type", params: {id : "loginDialogUsernameInput", text: "test"}},
  {params: {milliseconds: 500}, method: "waits.sleep"},
  {method: "type", params: {id : "loginDialogPasswordInput", text: "testtest"}},
  {method: "click", params: {id : "loginSubmitButton"}},
  {method: "waits.forElement", params: {id: "_month2_day28", timeout: 40000}},
  {method: "click", params: {link : "Settings"}}
];