# Generated by the windmill services transformer
from windmill.authoring import WindmillTestClient

client = WindmillTestClient(__name__)

def test():
    client.click(id=u'viewNavCenterRight')
    client.wait(milliseconds=2000)
    client.doubleClick(id=u'hourDiv1-1200')
    client.wait(milliseconds=2000)
    client.type(text=u'Properties Test', id=u'noteTitle')
    client.type(text=u'9:00', id=u'startTime')
    client.type(text=u'4:00', id=u'endTime')
    client.radio(id=u'startMeridianAM')
    client.radio(id=u'endMeridianPM')
    client.select(id=u'eventStatus', option=u'Tentative')
    client.type(text=u'A description for the properties test', id=u'noteDescription')
    client.click(jsid=u'windmill.testingApp.cosmo.app.pim.layout.baseLayout.mainApp.rightSidebar.detailViewForm.buttonSection.saveButton.widgetId')