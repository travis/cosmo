var adminTests = {};

adminTests.test_setup = [
    {"params": {"url": "{$baseUrl}\/login"}, "method": "open"},
    {"params": {"id": "loginDialogUsernameInput"}, "method": "click"},
    {"params": {"text": "root", "id": "loginDialogUsernameInput"}, "method": "type"},
    {"params": {"text": "cosmo", "id": "loginDialogPasswordInput"}, "method": "type"},
    {"params": {"id": "loginSubmitButton"}, "method": "click"},
    {"params": {"link": "Users"}, "method": "click"},
];

adminTests.test_users = (function(){
    var tests = [];
    for (var i = 0x0020; i < 0x0025; i++){
        var c = String.fromCharCode(i);
        [{"params": {"link": "Create New User"}, "method": "click"},
         {"params": {"id": "createUserDialogUsername"}, "method": "click"},
         {"params": {"text": "test" + c, "id": "createUserDialogUsername"}, "method": "type"},
         {"params": {"text": "test" + c, "id": "createUserDialogFirstName"}, "method": "type"},
         {"params": {"text": "test" + c, "id": "createUserDialogLastName"}, "method": "type"},
         {"params": {"text": "test" + i + "@example.com", "id": "createUserDialogEmail"}, "method": "type"},
         {"params": {"text": "testing" + c, "id": "createUserDialogPassword"}, "method": "type"},
         {"params": {"text": "testing" + c, "id": "createUserDialogConfirm"}, "method": "type"},
         {"params": {"id": "createUserDialogSubmitButton"}, "method": "click"},
         {"params": {"timeout": 3000, "id": "test123Row"}, "method": "waits.forElement"},
         {"params": {"id": "test123Row"}, "method": "click"},
         {"params": {"id": "modifySelectedUserLink"}, "method": "click"},
         {"params": {"id": "modifyUserDialogUsername"}, "method": "click"},
         {"params": {"text": "nest123", "id": "modifyUserDialogUsername"}, "method": "type"},
         {"params": {"id": "modifyUserDialogFirstName"}, "method": "click"},
         {"params": {"text": "nest123", "id": "modifyUserDialogFirstName"}, "method": "type"},
         {"params": {"id": "modifyUserDialogLastName"}, "method": "click"},
         {"params": {"text": "nest123", "id": "modifyUserDialogLastName"}, "method": "type"},
         {"params": {"id": "modifyUserDialogEmail"}, "method": "click"},
         {"params": {"text": "nest123@example.com", "id": "modifyUserDialogEmail"}, "method": "type"},
         {"params": {"id": "modifyUserDialogPassword"}, "method": "click"},
         {"params": {"text": "foobar", "id": "modifyUserDialogPassword"}, "method": "type"},
         {"params": {"text": "foobar", "id": "modifyUserDialogConfirm"}, "method": "type"},
         {"params": {"id": "modifyUserDialogSubmitButton"}, "method": "click"},
         {"params": {"timeout": 3000, "id": "nest123Row"}, "method": "waits.forElement"},
         {"params": {"id": "nest123Row"}, "method": "click"},
         {"params": {"id": "deleteSelectedUsersLink"}, "method": "click"}
    ]}
})();

adminTests.test_teardown = [
    {"params": {"link": "Log out"}, "method": "click"}
];
