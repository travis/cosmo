// Some environment setup
var app = windmill.testWindow;
var cosmo = app.cosmo;

// The tests, in order
windmill.jsTest.registerTests([
  'fixture_unsavedSetUpLozenges',
  'test_unsavedClickLozengeDiscard',
  'test_unsavedClickLozengeSave',
  'test_unsavedViewChangeDiscard',
  'test_unsavedViewChangeSave'
]);
