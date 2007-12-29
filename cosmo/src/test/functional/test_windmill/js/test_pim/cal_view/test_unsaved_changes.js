windmill.jsTest.require('shared/test_nav_to_cal_view.js');
windmill.jsTest.require('shared/utilities.js');

pimTest.calView.test_unsavedChanges = new function () {
  var doLozengeClickByRegistryIndex =
    pimTest.shared.utilities.doLozengeClickByRegistryIndex; 

  this.setup = new function () {
    // Make sure cal view is displayed
    this.test_navToCalView = pimTest.shared.test_navToCalView;
    // Create two events to toggle back and forth between
    this.test_createCalEvents = [
      // Create an event, Sunday at noon
      { method: "doubleClick", params: { id: "hourDiv0-1200" } },
      { method: "waits.sleep", params: { milliseconds: 3000 } },
      // Create an event, Monday at noon
      { method: "doubleClick", params: { id: "hourDiv1-1200" } },
      { method: "waits.sleep", params: { milliseconds: 3000 } }
    ];
  };

  this.test_clickLozengeDiscard = [
    // Select the second event
    function () { doLozengeClickByRegistryIndex(1); },
    // Change the title
    { method: "type", params: { id: "noteTitle", text: "Unsaved: Click Lozenge" } },
    // Click back to the first event
    function () { doLozengeClickByRegistryIndex(0); },
    // Verify that the Unsaved Changes dialog appears
    { method: "waits.forElement", params: { id: "modalDialogContent" } },
    { method: "asserts.assertText", params: { validator:
      _('Main.Prompt.UnsavedChangesConfirm'), id: "modalDialogContent" } },
    // Click Discard Changes button
    { method: "click", params: { id: "unsavedChangesDialogDiscardButton" } },
    { method: "waits.forNotElement", params: { id: "modalDialogContent" } },
    // Verify the event title didn't change
    function () {
      var summaryText = $('noteTitle').value;
      jum.assertNotEquals(summaryText, 'Unsaved: Click Lozenge');
    }
  ];

  this.test_clickLozengeSave = [
    { method: "waits.sleep", params: { milliseconds: 4000 } },
    // Select the second event
    function () { doLozengeClickByRegistryIndex(1); },
    // Change the title
    { method: "type", params: { id: "noteTitle", text: "Unsaved: Click Lozenge" } },
    // Click back to the first event
    function () { doLozengeClickByRegistryIndex(0); },
    // Verify that the Unsaved Changes dialog appears
    { method: "waits.forElement", params: { id: "modalDialogContent" } },
    { method: "asserts.assertText", params: { validator:
      _('Main.Prompt.UnsavedChangesConfirm'), id: "modalDialogContent" } },
    // Click Save button
    { method: "click", params: { id: "unsavedChangesDialogSaveButton" } },
    { method: "waits.forNotElement", params: { id: "modalDialogContent" } },
    // Verify the event title changed
    function () {
      var summaryText = $('noteTitle').value;
      jum.assertEquals(summaryText, 'Unsaved: Click Lozenge');
    }
  ];

  this.test_viewChangeDiscard = [
    { method: "waits.sleep", params: { milliseconds: 4000 } },
    // Select the second event
    function () { doLozengeClickByRegistryIndex(1); },
    // Change the title
    { method: "type", params: { id: "noteTitle", text: "Unsaved: Change View" } },
    // Change to list view
    { method: "click", params: { id: "viewToggle_button0" } },
    // Verify that the Unsaved Changes dialog appears
    { method: "waits.forElement", params: { id: "modalDialogContent" } },
    { method: "asserts.assertText", params: { validator:
      _('Main.Prompt.UnsavedChangesConfirm'), id: "modalDialogContent" } },
    // Click Discard Changes button
    { method: "click", params: { id: "unsavedChangesDialogDiscardButton" } },
    { method: "waits.forNotElement", params: { id: "modalDialogContent" } },
    // Verify the view switched to list view
    function () {
      var listContainer = $('listViewContainer');
      var display = '';
      if (listContainer) {
        display = listContainer.style.display;
      }
      jum.assertEquals(display, 'block');
    },
    // Change back to cal view
    { method: "click", params: { id: "viewToggle_button1" } },
    { method: "waits.forElement", params: { id: "calViewNav" } },
    // Select the second event
    function () { doLozengeClickByRegistryIndex(1); },
    // Verify the event title didn't change
    function () {
      var summaryText = $('noteTitle').value;
      jum.assertNotEquals(summaryText, 'Unsaved: Change View');
    }
  ];

  this.test_viewChangeSave = [
    { method: "waits.sleep", params: { milliseconds: 4000 } },
    // Select the second event
    function () { doLozengeClickByRegistryIndex(1); },
    // Change the title
    { method: "type", params: { id: "noteTitle", text: "Unsaved: Change View" } },
    // Change to list view
    { method: "click", params: { id: "viewToggle_button0" } },
    // Verify that the Unsaved Changes dialog appears
    { method: "waits.forElement", params: { id: "modalDialogContent" } },
    { method: "asserts.assertText", params: { validator:
      _('Main.Prompt.UnsavedChangesConfirm'), id: "modalDialogContent" } },
    // Click Save button
    { method: "click", params: { id: "unsavedChangesDialogSaveButton" } },
    { method: "waits.forNotElement", params: { id: "modalDialogContent" } },
    // Verify the event title changed
    function () {
      var summaryText = $('noteTitle').value;
      jum.assertEquals(summaryText, 'Unsaved: Change View');
    }
  ];

  this.teardown = [
    // Select the second event
    function () { doLozengeClickByRegistryIndex(1); },
    // Click the Remove button
    { method: "click", params: { id: "detailRemoveButton" } },
    { method: "waits.sleep", params: { milliseconds: 4000 } },
    // Click Remove button on the confirmation dialog
    { method: "click", params: { id: "removeConfirmRemoveButton" } },
    { method: "waits.sleep", params: { milliseconds: 4000 } },
    // Select the first event
    function () { doLozengeClickByRegistryIndex(0); },
    // Click the Remove button
    { method: "click", params: { id: "detailRemoveButton" } },
    { method: "waits.sleep", params: { milliseconds: 4000 } },
    // Click Remove button on the confirmation dialog
    { method: "click", params: { id: "removeConfirmRemoveButton" } },
    { method: "waits.sleep", params: { milliseconds: 4000 } }
  ];

};



