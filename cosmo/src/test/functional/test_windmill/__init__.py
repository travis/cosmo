#   Copyright (c) 2006-2007 Open Source Applications Foundation
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

from windmill.authoring import setup_module as windmill_setup
from windmill.authoring import teardown_module as windmill_teardown
from windmill.authoring import enable_collector, RunJsonFile
import os

enable_collector()

registry_line = '{"method": "commands.createVariables", "params":{"variables":["dvSaveButton|cosmo.app.pim.layout.baseLayout.mainApp.rightSidebar.detailViewForm.buttonSection.saveButton.widgetId","dvRemoveButton|cosmo.app.pim.layout.baseLayout.mainApp.rightSidebar.detailViewForm.buttonSection.removeButton.widgetId","listView|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.viewToggle.buttonNodes[0].id","calView|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.viewToggle.buttonNodes[1].id","calPrevious|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.calViewNav.navButtons.leftButtonNode.id","calNext|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.calViewNav.navButtons.rightButtonNode.id","listPrevious|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.listViewPager.prevButton.domNode.id","listNext|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.listViewPager.nextButton.domNode.id","quickCreateText|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.quickItemEntry.createTexBox.id","quickCreateButton|cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.navBar.quickItemEntry.createButton.domNode.id","collectionNameTextBox|cosmo.app.modalDialog.content.collectionNameInput.id","collectionChangeNameButton|cosmo.app.modalDialog.content.collectionNameChangeButton.id","collectionCloseButton|cosmo.app.modalDialog.btnsLeft[0].domNode.id","dialogTab0|cosmo.app.modalDialog.content.tabNodes[0].id","dialogTab1|cosmo.app.modalDialog.content.tabNodes[1].id","dialogTab2|cosmo.app.modalDialog.content.tabNodes[2].id","btnsRight0|cosmo.app.modalDialog.btnsRight[0].domNode.id","btnsCenter0|cosmo.app.modalDialog.btnsCenter[0].domNode.id","btnsLeft0|cosmo.app.modalDialog.btnsLeft[0].domNode.id","miniCalGotoButton|cosmo.app.pim.layout.baseLayout.mainApp.leftSidebar.minical.goToButton.domNode.id","btnsRight1|cosmo.app.modalDialog.btnsRight[1].domNode.id"]}}'

def setup_module(module):
    windmill_setup(module)
    from windmill.bin import shell_objects
    shell_objects.load_extensions_dir(os.path.join(os.path.abspath(os.path.dirname(__file__)), 'extensions'))
    RunJsonFile('create_registry.json', lines=[registry_line])()
    



