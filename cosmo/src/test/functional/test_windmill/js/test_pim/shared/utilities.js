
pimTest.shared.utilities = new function () {
  this.getLozengeIdByIndex = function (i) {
    var itemId = cosmo.view.cal.itemRegistry.getAtPos(i).id;
    return 'eventDiv__' + itemId;
  };
}
