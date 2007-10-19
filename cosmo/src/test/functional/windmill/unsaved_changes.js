var unsavedChangesTests = {};

unsavedChangesTests.test_setup = [
  // Create an event, Sunday at noon
  { method: "doubleClick", params: { id: "hourDiv0-1200" } },
  { method: "waits.sleep", params: { milliseconds: 3000 } },
  // Create an event, Monday at noon
  { method: "doubleClick", params: { id: "hourDiv1-1200" } },
  { method: "waits.sleep", params: { milliseconds: 3000 } }
];

unsavedChangesTests.test_clickLozengeDiscard = [
  // Select the second event
  { method: 'extensions.clickLozenge', params: { jsid: "cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Change the title
  { method: "type", params: { id: "noteTitle", text: "Unsaved: Click Lozenge" } },
  // Click back to the first event
  { method: 'extensions.clickLozenge', params: { jsid: "cosmo.view.cal.itemRegistry.getAtPos(0).id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Verify that the Unsaved Changes dialog appears
  function () {
    var dialogText = $('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  // Click Discard Changes button
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[0].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Verify the event title didn't change
  function () {
    var summaryText = $('noteTitle').value;
    jum.assertNotEquals(summaryText, 'Unsaved: Click Lozenge');
  }
];

unsavedChangesTests.test_clickLozengeSave = [
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Select the second event
  { method: 'extensions.clickLozenge', params: { jsid: "cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Change the title
  { method: "type", params: { id: "noteTitle", text: "Unsaved: Click Lozenge" } },
  // Click back to the first event
  { method: 'extensions.clickLozenge', params: { jsid: "cosmo.view.cal.itemRegistry.getAtPos(0).id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Verify that the Unsaved Changes dialog appears
  function () {
    var dialogText = $('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  // Click Save button
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[1].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Verify the event title changed
  function () {
    var summaryText = $('noteTitle').value;
    jum.assertEquals(summaryText, 'Unsaved: Click Lozenge');
  }
];

unsavedChangesTests.test_viewChangeDiscard = [
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Select the second event
  { method: 'extensions.clickLozenge', params: { jsid: "cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Change the title
  { method: "type", params: { id: "noteTitle", text: "Unsaved: Change View" } },
  // Change to list view
  { method: "click", params: { jsid: "cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.viewToggle.buttonNodes[0].id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Verify that the Unsaved Changes dialog appears
  function () {
    var dialogText = $('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  // Click Discard Changes button
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[0].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
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
  { method: "click", params: { jsid: "cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.viewToggle.buttonNodes[1].id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Select the second event
  { method: 'extensions.clickLozenge', params: { jsid: "cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Verify the event title didn't change
  function () {
    var summaryText = $('noteTitle').value;
    jum.assertNotEquals(summaryText, 'Unsaved: Change View');
  }
];

unsavedChangesTests.test_viewChangeSave = [
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Select the second event
  { method: 'extensions.clickLozenge', params: { jsid: "cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Change the title
  { method: "type", params: { id: "noteTitle", text: "Unsaved: Change View" } },
  // Change to list view
  { method: "click", params: { jsid: "cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.viewToggle.buttonNodes[0].id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Verify that the Unsaved Changes dialog appears
  function () {
    var dialogText = $('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  // Click Save button
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[1].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Verify the event title changed
  function () {
    var summaryText = $('noteTitle').value;
    jum.assertEquals(summaryText, 'Unsaved: Change View');
  }
];

// Leave this out for now -- requires there be at least two collections
/*
unsavedChangesTests.test_collectionChangeDiscard = [
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Select the event
  { method: 'extensions.clickLozenge', params: { jsid: "cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Change the Title
  { method: "type", params: { id: "noteTitle", text: "Unsaved: Change Collection" } },
  // Change to the second collection
  { method: "select", params: { id: 'calSelectElem', locatorType: 'VALUE', option: "1" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Verify the Unsaved Changes dialog appears
  function () {
    var dialogText = $('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  // Click Discard Changes button
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[0].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Verify the collection changed
  function () {
    var index = $('calSelectElem').selectedIndex;
    jum.assertEquals(index, 1);
  },
  // Return to the first collection
  { method: "select", params: { id: 'calSelectElem', locatorType: 'VALUE', option: "0" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Select the event
  { method: 'extensions.clickLozenge', params: { jsid: "cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Verify the title of the event didn't change
  function () {
    var summaryText = $('noteTitle').value;
    jum.assertNotEquals(summaryText, 'Unsaved: Change Collection');
  }
];

// Leave this out for now -- requires there be at least two collections
unsavedChangesTests.test_collectionChangeSave = [
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Select the event
  { method: 'extensions.clickLozenge', params: { jsid: "cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Change the title
  { method: "type", params: { id: "noteTitle", text: "Unsaved: Change Collection" } },
  // Change to the second collection
  { method: "select", params: { id: 'calSelectElem', locatorType: 'VALUE', option: "1" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Verify the Unsaved Changes dialog appears
  function () {
    var dialogText = $('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  // Click Save Changes button
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[1].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Verify the title of the event changed
  function () {
    var summaryText = $('noteTitle').value;
    jum.assertEquals(summaryText, 'Unsaved: Change Collection');
  }
];
*/

unsavedChangesTests.test_teardown = [
  // Select the second event
  { method: 'extensions.clickLozenge', params: { jsid: "cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Click the detail-view Remove button
  { method: "click", params: { jsid: "cosmo.app.pim.layout.baseLayout.mainApp.rightSidebar.detailViewForm.buttonSection.removeButton.widgetId" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Click the Remove confirmation button
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[0].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Click the first event
  { method: 'extensions.clickLozenge', params: { jsid: "cosmo.view.cal.itemRegistry.getAtPos(0).id" } },
  // Click the detail-view Remove button
  { method: "click", params: { jsid: "cosmo.app.pim.layout.baseLayout.mainApp.rightSidebar.detailViewForm.buttonSection.removeButton.widgetId" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Click the Remove confirmation button
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[0].domNode.id" } }
];


