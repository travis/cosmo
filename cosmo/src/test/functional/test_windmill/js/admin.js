var adminTests = {};

adminTests.test_users = (function(){
    var tests = [
        {params: {url: "\/chandler\/logout"}, method: "open"},
        { method: "waits.sleep", params: { milliseconds: 10000 } },
        {params: {text: "root", id: "loginDialogUsernameInput"}, method: "type"},
        {params: {text: "cosmo", id: "loginDialogPasswordInput"}, method: "type"},
        {params: {id: "loginSubmitButton"}, method: "click"},
        { method: "waits.sleep", params: { milliseconds: 4000 } },
        {params: {"link": "Users"}, method: "click"},
        { method: "waits.sleep", params: { milliseconds: 4000 } }

    ];
    for (var i = 0x0020; i < 0x0025; i++){
        var c = String.fromCharCode(i);
        tests.concat([{params: {"link": "Create New User"}, method: "click"},
                      {params: {id: "createUserDialogUsername"}, method: "click"},
                      {params: {text: "test" + c, id: "createUserDialogUsername"}, method: "type"},
                      {params: {text: "test" + c, id: "createUserDialogFirstName"}, method: "type"},
                      {params: {text: "test" + c, id: "createUserDialogLastName"}, method: "type"},
                      {params: {text: "test" + i + "@example.com", id: "createUserDialogEmail"}, method: "type"},
                      {params: {text: "testing" + c, id: "createUserDialogPassword"}, method: "type"},
                      {params: {text: "testing" + c, id: "createUserDialogConfirm"}, method: "type"},
                      {params: {id: "createUserDialogSubmitButton"}, method: "click"},
                      { method: "waits.sleep", params: { milliseconds: 4000 } },
                      {params: {id: "test123Row"}, method: "click"},
                      {params: {id: "modifySelectedUserLink"}, method: "click"},
                      {params: {id: "modifyUserDialogUsername"}, method: "click"},
                      {params: {text: "nest123", id: "modifyUserDialogUsername"}, method: "type"},
                      {params: {id: "modifyUserDialogFirstName"}, method: "click"},
                      {params: {text: "nest123", id: "modifyUserDialogFirstName"}, method: "type"},
                      {params: {id: "modifyUserDialogLastName"}, method: "click"},
                      {params: {text: "nest123", id: "modifyUserDialogLastName"}, method: "type"},
                      {params: {id: "modifyUserDialogEmail"}, method: "click"},
                      {params: {text: "nest123@example.com", id: "modifyUserDialogEmail"}, method: "type"},
                      {params: {id: "modifyUserDialogPassword"}, method: "click"},
                      {params: {text: "foobar", id: "modifyUserDialogPassword"}, method: "type"},
                      {params: {text: "foobar", id: "modifyUserDialogConfirm"}, method: "type"},
                      {params: {id: "modifyUserDialogSubmitButton"}, method: "click"},
                      { method: "waits.sleep", params: { milliseconds: 4000 } },
                      {params: {id: "nest123Row"}, method: "click"},
                      {params: {id: "deleteSelectedUsersLink"}, method: "click"}
                     ]);
    }
    tests.push({params: {"link": "Log out"}, method: "click"});
    return tests;
})();
