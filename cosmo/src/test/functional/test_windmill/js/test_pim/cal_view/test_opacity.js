windmill.jsTest.require('shared/test_nav_to_cal_view.js');
windmill.jsTest.require('shared/utilities.js');

pimTest.calView.test_opacity= new function () {
  var doLozengeClickByRegistryIndex =
    pimTest.shared.utilities.doLozengeClickByRegistryIndex; 

  this.setup = new function () {
    this.test_navToCalView = pimTest.shared.test_navToCalView;
    this.test_createCalEvents = [
      // Create an event, Sunday at noon
      { method: "doubleClick", params: { id: "hourDiv0-1200" } },
      { method: "waits.sleep", params: { milliseconds: 3000 } }
    ];
  };

  this.test_modalDialogMask = [
    // Select the first event
    function () { doLozengeClickByRegistryIndex(0); },
    // Click the Remove button
    { method: "click", params: { id: "detailRemoveButton" } },
    { method: "waits.forElement", params: { id: "removeConfirmRemoveButton", "timeout": 40000} },
    // Check the opacity of the UI mask
    function () {
      var mask = cosmo.app.modalDialog.uiFullMask;
      if (document.all) {
        if (mask.style.filter != 'alpha(opacity=70)') {
          return false;
        }
      }
      else {
        if (mask.style.opacity != 0.8) {
          return false;
        }
      }
    }
  ];

  this.test_detailViewButtonsDisabled = [
    // Click the Remove button in the confirmation dialog
    { method: "click", params: { id: "removeConfirmRemoveButton" } },
    { method: "waits.sleep", params: { milliseconds: 4000 } },
    // Check the opacity of the detail view buttons
    // Nothing should be selected, so buttons should be disabled
    function () {
      var rem = $('detailRemoveButton');
      if (document.all) {
        if (rem.style.filter != 'alpha(opacity=70)') {
          return false;
        }
      }
      else {
        if (rem.style.opacity != 0.8) {
          return false;
        }
      }
    }
  ];
  
  this.teardown = function () { return true; };

};
