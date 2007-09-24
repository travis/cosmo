
var fixture_unsavedSetUpLozenges = [
  { method: "doubleClick", params: { id: "hourDiv0-1200" } },
  { method: "waits.sleep", params: { milliseconds: 3000 } },
  { method: "doubleClick", params: { id: "hourDiv1-1200" } },
  { method: "waits.sleep", params: { milliseconds: 3000 } }
];

var test_unsavedClickLozengeDiscard = [
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testWindow.cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  { method: "type", params: { id: "noteTitle", text: "Unsaved Lozenge" } },
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testWindow.cosmo.view.cal.itemRegistry.getAtPos(0).id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  function () {
    var dialogText = app.$('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[0].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  function () {
    var summaryText = app.$('noteTitle').value;
    jum.assertEquals(summaryText, 'New Event');
  }
];

var test_unsavedClickLozengeSave = [
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testWindow.cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  { method: "type", params: { id: "noteTitle", text: "Unsaved Lozenge" } },
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testWindow.cosmo.view.cal.itemRegistry.getAtPos(0).id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  function () {
    var dialogText = app.$('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[1].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  function () {
    var summaryText = app.$('noteTitle').value;
    jum.assertEquals(summaryText, 'Unsaved Lozenge');
  }
];

var test_unsavedViewChangeDiscard = [
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testWindow.cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  { method: "type", params: { id: "noteTitle", text: "Unsaved View Change" } },
  { method: "click", params: { jsid: "cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.viewToggle.buttonNodes[0].id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  function () {
    var dialogText = app.$('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[0].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  function () {
    var titleCol = app.$('listViewTitleHeader');
    jum.assertEquals(titleCol.innerHTML, 'Title');
  }
];

var test_unsavedViewChangeSave = [
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  { method: "click", params: { jsid: "cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.viewToggle.buttonNodes[1].id" } },
  { method: 'extensions.clickLozenge', params: { jsid: "windmill.testWindow.cosmo.view.cal.itemRegistry.getAtPos(1).id" } },
  { method: "type", params: { id: "noteTitle", text: "Unsaved View Change" } },
  { method: "click", params: { jsid: "cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.viewToggle.buttonNodes[0].id" } },
  { method: "waits.sleep", params: { milliseconds: 4000 } },
  function () {
    var dialogText = app.$('modalDialogContent').innerHTML;
    jum.assertEquals(dialogText, "You have unsaved changes in the item-detail form. What do you want to do?");
  },
  { method: "click", params: { jsid: "cosmo.app.modalDialog.btnsRight[1].domNode.id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  function () {
    var titleCol = app.$('listViewTitleHeader');
    jum.assertEquals(titleCol.innerHTML, 'Title');
  },
  { method: "click", params: { jsid: "cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.viewToggle.buttonNodes[1].id" } },
  { method: "waits.sleep", params: { milliseconds: 8000 } },
  function () {
    var summaryText = app.$('noteTitle').value;
    jum.assertEquals(summaryText, 'Unsaved View Change');
  }
];


