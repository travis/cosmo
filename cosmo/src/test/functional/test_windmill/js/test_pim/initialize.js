
var pimTest = new function () {
  this.shared = {};
  this.calView = {};
};

(function () {
  var str = '{"method": "commands.createVariables", "params":{"variables":["dvSaveButton|cosmo.app.pim.layout.baseLayout.mainApp.rightSidebar.detailViewForm.buttonSection.saveButton.widgetId","dvRemoveButton|cosmo.app.pim.layout.baseLayout.mainApp.rightSidebar.detailViewForm.buttonSection.removeButton.widgetId","listView|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.viewToggle.buttonNodes[0].id","calView|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.viewToggle.buttonNodes[1].id","calPrevious|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.calViewNav.navButtons.leftButtonNode.id","calNext|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.calViewNav.navButtons.rightButtonNode.id","listPrevious|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.listViewPager.prevButton.domNode.id","listNext|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.listViewPager.nextButton.domNode.id","quickCreateText|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.quickItemEntry.createTexBox.id","quickCreateButton|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.quickItemEntry.createButton.domNode.id","collectionNameTextBox|cosmo.app.modalDialog.content.collectionNameInput.id","collectionChangeNameButton|cosmo.app.modalDialog.content.collectionNameChangeButton.id","collectionCloseButton|cosmo.app.modalDialog.btnsLeft[0].domNode.id","dialogTab0|cosmo.app.modalDialog.content.tabNodes[0].id","dialogTab1|cosmo.app.modalDialog.content.tabNodes[1].id","dialogTab2|cosmo.app.modalDialog.content.tabNodes[2].id","btnsRight0|cosmo.app.modalDialog.btnsRight[0].domNode.id","btnsCenter0|cosmo.app.modalDialog.btnsCenter[0].domNode.id","btnsLeft0|cosmo.app.modalDialog.btnsLeft[0].domNode.id","miniCalGotoButton|cosmo.app.pim.layout.baseLayout.mainApp.leftSidebar.minical.goToButton.domNode.id","btnsRight1|cosmo.app.modalDialog.btnsRight[1].domNode.id"]}}';
  var obj = eval('(' + str + ')');
  var vars = obj.params.variables;
  for (var i = 0; i < vars.length; i++) {
    var v = vars[i].split('|');
    var key = v[0];
    var val = v[1];
    key = (key.indexOf('{$') != 0) ? '{$'+ key +'}' : key;
    windmill.varRegistry.addItem(key, val);
  }
})();

