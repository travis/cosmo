
pimTest.shared.test_newUserLogin = new function () {
  this.setup = function () {
    // Create a different random user for each new test run
    windmill.varRegistry.removeItem('{$random}');
    windmill.varRegistry.addItemCreateValue('{$random}');
  };
  this.test_create = new function () {
    // Create the array programmatically so we can do some
    // conditional-ness to deal with which server we're testing.
    // For now, just assume the login page as a start
    var arr = [
      { method: "click", params: { link: "Sign up." } },
      { method: "waits.forElement", params: { id: "loginDialogFormContainer" } },
      { method: "waits.forElement", params: { id: "signupSubmit", timeout: 40000 } },
      // Click for 'required field' error, click submit with no form imput
      { method: "click", params: { id: "signupSubmit"} },
      { method: "waits.sleep", params: { milliseconds : 2000 } },
      { method: "asserts.assertText", params: {validator:
        _('Signup.Error.RequiredField'), id: "modalDialogContent" } },
      // Input user info
      { method: "type", params: { text: "{$random}", id: "username" } },
      { method: "type", params: { text: "tester", id: "firstName" } },
      { method: "type", params: { text: "tester", id: "lastName" } },
      { method: "type", params: { id: "email", text: "{$random}@osafoundation.org" } },
      // Check for 'input exceeds max length' error with overly long passwrod
      { method: "type", params: { text: "12345678901234567890", id: "password" } },
      { method: "type",params: { text: "12345678901234567890", id: "confirm" } },
      { method: "click", params: { id: "signupSubmit" } },
      { method: "waits.sleep", params: {"milliseconds": 2000 } },
      { method: "asserts.assertText", params: {validator:
        _('Signup.Error.MaxLength') + " (16)", id: "modalDialogContent" } },
      // Check for error from confirmation not matching initial password input
      { method: "type",params: { text: "testers", id: "password" } },
      { method: "type", params: { text: "tester", id: "confirm" } },
      { method: "click", params: { id: "signupSubmit" } },
      { method: "waits.sleep", params: {"milliseconds": 2000 } },
      { method: "asserts.assertText", params: { validator:
        _('Signup.Error.MatchPassword'), id: "modalDialogContent" } },
      // Correct pass/confirm, submit form
      { method: "type", params: { text: "tester", id: "password" } },
      { method: "click", params: { id: "signupSubmit" } },
      { method: "waits.forElement", params: { id: "signupClose", "timeout": 40000 } },
      // Check for the account-creation success message
      { method: "asserts.assertText", params: {validator:
        _('Signup.Prompt.Success'), id: "modalDialogPrompt" } },
      { method: "click", params: { id: "signupClose" } }
    ]
    return arr;
  };
  // Log in to the app
  this.test_login = [
    { method : "type", params: { id: "loginDialogUsernameInput", text: "{$random}" } },
    { method: "waits.sleep", params: { milliseconds : 500 } },
    { method: "type", params: {  id: "loginDialogPasswordInput", text: "testers" } },
    { method: "click", params: {  id: "loginSubmitButton" } },
    { method: "waits.sleep", params: { milliseconds : 500 } },
    { method: "type", params: {  id: "loginDialogPasswordInput", text: "tester" } },
    { method: "click", params: {  id: "loginSubmitButton" } },
    { method: "waits.forElement", params: { id: "mainLogoContainer", "timeout": 40000} }
  ];
};

