
pimTest.shared.test_logout = new function () {
  this.test_clickLogout = [
    { method: "waits.sleep", params: { milliseconds : 2000 } },
    { method: "click", params: {  link: "Log out" } }
  ];
  // Tests have to reload
  this.test_loggedOut = [
    { method: "waits.forElement", params: { id: "loginSubmitButton", "timeout": 40000 } }
  ];
};

