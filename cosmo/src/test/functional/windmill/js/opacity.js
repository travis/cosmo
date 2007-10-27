var opacityTests = {};

opacityTests.test_setup = [
  // Create an event, Sunday at noon
  { method: "doubleClick", params: { id: "hourDiv0-1200" } },
  { method: "waits.sleep", params: { milliseconds: 3000 } },
  function () {
  }
];

opacityTests.test_modalDialogMask = [
  // Select the second event
  { method: 'extensions.clickLozenge', params: { jsid: "cosmo.view.cal.itemRegistry.getAtPos(0).id" } },
  // Click the Remove button
  { method: "click", params: { jsid: "{$dvRemoveButton}" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
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

opacityTests.test_detailViewButtonsDisabled = [
  // Click the Remove button in the confirmation dialog
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[0].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Check the opacity of the detail view buttons
  // Nothing should be selected, so buttons should be disabled
  function () {
    // Look up the jsid for the button id from the shortcut
    var remPath = windmill.varRegistry.getItem('{$dvRemoveButton}');
    // Get the DOM node's id
    var remId = eval(remPath);
    // The actual node
    var rem = $(remId);
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

