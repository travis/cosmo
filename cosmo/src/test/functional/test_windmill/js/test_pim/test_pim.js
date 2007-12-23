windmill.jsTest.require('shared/test_new_user_login.js');
windmill.jsTest.require('cal_view/test_unsaved_changes.js');
windmill.jsTest.require('cal_view/test_opacity.js');
windmill.jsTest.require('shared/test_logout.js');

var test_pim = new function () {
  this.setup = pimTest.shared.test_newUserLogin;
  this.test_calView = new function () {
    this.test_unsavedChanges = pimTest.calView.test_unsavedChanges;
    this.test_opacity = pimTest.calView.test_opacity;
  }
  this.teardown = pimTest.shared.test_logout;
};
