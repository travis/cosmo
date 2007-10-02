
var fixture_unsavedSetUpLozenges = [
  // Create an event, Sunday at noon
  { method: "doubleClick", params: { id: "hourDiv0-1200" } },
  { method: "waits.sleep", params: { milliseconds: 3000 } },
  // Create an event, Monday at noon
  { method: "doubleClick", params: { id: "hourDiv1-1200" } },
  { method: "waits.sleep", params: { milliseconds: 3000 } }
];

var test_unsavedClickLozengeDiscard = [
  // Select the second event
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testingApp.cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Change the title
  { method: "type", params: { id: "noteTitle", text: "Unsaved: Click Lozenge" } },
  // Click back to the first event
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testWindow.cosmo.view.cal.itemRegistry.getAtPos(0).id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Verify that the Unsaved Changes dialog appears
  function () {
    var dialogText = app.$('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  // Click Discard Changes button
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[0].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Verify the event title didn't change
  function () {
    var summaryText = app.$('noteTitle').value;
    jum.assertNotEquals(summaryText, 'Unsaved: Click Lozenge');
  }
];

var test_unsavedClickLozengeSave = [
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Select the second event
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testingApp.cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Change the title
  { method: "type", params: { id: "noteTitle", text: "Unsaved: Click Lozenge" } },
  // Click back to the first event
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testWindow.cosmo.view.cal.itemRegistry.getAtPos(0).id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Verify that the Unsaved Changes dialog appears
  function () {
    var dialogText = app.$('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  // Click Save button
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[1].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Verify the event title changed
  function () {
    var summaryText = app.$('noteTitle').value;
    jum.assertEquals(summaryText, 'Unsaved: Click Lozenge');
  }
];

var test_unsavedViewChangeDiscard = [
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Select the second event
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testingApp.cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Change the title
  { method: "type", params: { id: "noteTitle", text: "Unsaved: Change View" } },
  // Change to list view
  { method: "click", params: { jsid: "cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.viewToggle.buttonNodes[0].id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Verify that the Unsaved Changes dialog appears
  function () {
    var dialogText = app.$('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  // Click Discard Changes button
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[0].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Verify the view switched to list view
  function () {
    var titleCol = app.$('listViewTitleHeader');
    jum.assertEquals(titleCol.innerHTML, 'Title');
  },
  // Change back to cal view
  { method: "click", params: { jsid: "cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.viewToggle.buttonNodes[1].id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Select the second event
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testingApp.cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Verify the event title didn't change
  function () {
    var summaryText = app.$('noteTitle').value;
    jum.assertNotEquals(summaryText, 'Unsaved: Change View');
  }
];

var test_unsavedViewChangeSave = [
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Select the second event
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testWindow.cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Change the title
  { method: "type", params: { id: "noteTitle", text: "Unsaved: Change View" } },
  // Change to list view
  { method: "click", params: { jsid: "cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.viewToggle.buttonNodes[0].id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Verify that the Unsaved Changes dialog appears
  function () {
    var dialogText = app.$('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  // Click Save button
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[1].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Verify the event title changed
  function () {
    var summaryText = app.$('noteTitle').value;
    jum.assertEquals(summaryText, 'Unsaved: Change View');
  }
];

// Leave this out for now -- requires there be at least two collections
var test_unsavedCollectionChangeDiscard = [
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Select the event
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testingApp.cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Change the Title
  { method: "type", params: { id: "noteTitle", text: "Unsaved: Change Collection" } },
  // Change to the second collection
  { method: "select", params: { id: 'calSelectElem', locatorType: 'VALUE', option: "1" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Verify the Unsaved Changes dialog appears
  function () {
    var dialogText = app.$('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  // Click Discard Changes button
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[0].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Verify the collection changed
  function () {
    var index = app.$('calSelectElem').selectedIndex;
    jum.assertEquals(index, 1);
  },
  // Return to the first collection
  { method: "select", params: { id: 'calSelectElem', locatorType: 'VALUE', option: "0" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Select the event
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testingApp.cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Verify the title of the event didn't change
  function () {
    var summaryText = app.$('noteTitle').value;
    jum.assertNotEquals(summaryText, 'Unsaved: Change Collection');
  }
];

// Leave this out for now -- requires there be at least two collections
var test_unsavedCollectionChangeSave = [
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Select the event
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testingApp.cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  // Change the title
  { method: "type", params: { id: "noteTitle", text: "Unsaved: Change Collection" } },
  // Change to the second collection
  { method: "select", params: { id: 'calSelectElem', locatorType: 'VALUE', option: "1" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  // Verify the Unsaved Changes dialog appears
  function () {
    var dialogText = app.$('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  // Click Save Changes button
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[1].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  // Verify the title of the event changed
  function () {
    var summaryText = app.$('noteTitle').value;
    jum.assertEquals(summaryText, 'Unsaved: Change Collection');
  }
];


