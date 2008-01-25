
pimTest.shared.utilities = new function () {
  this.doLozengeClickByRegistryIndex = function (index) {
    var item = cosmo.view.cal.itemRegistry.getAtPos(index);
    var nodeId = (item && item.lozenge && item.lozenge.domNode) ?
      item.lozenge.domNode.id : null;
    if (nodeId) {
      return windmill.jsTest.actions.click({ id: nodeId });
    }
    else {
      return false;
    }
  };
}
