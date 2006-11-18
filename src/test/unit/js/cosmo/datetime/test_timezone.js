dojo.require("cosmo.datetime.*");

//Initialization.
//TODO - once Dojo implements setUp() and tearDown() move this code there.
var registry = new cosmo.datetime.timezone.SimpleTimezoneRegistry(cosmo.env.getBaseUrl() + "/js/lib/olson-tzdata/");
registry.init(["northamerica"]);
cosmo.datetime.timezone.setTimezoneRegistry(registry);

function getNyTz(){
    var timezone = cosmo.datetime.timezone.getTimezone("America/New_York");
    return timezone;
}

function test_getTimezone(){
    var timezone = getNyTz();
    jum.assertTrue(timezone != null);
}

function test_getDateField(){
    var getDateField = cosmo.datetime.timezone.Timezone.prototype._getDateField;
    var scoobyDate = new ScoobyDate(2006, 11, 10, 12, 33, 30);
    jum.assertEquals(2006, getDateField(scoobyDate, "year"));
    jum.assertEquals(11, getDateField(scoobyDate, "month"));
    jum.assertEquals(10, getDateField(scoobyDate, "date"));
    jum.assertEquals(12, getDateField(scoobyDate, "hours"));
    jum.assertEquals(33, getDateField(scoobyDate, "minutes"));
    jum.assertEquals(30, getDateField(scoobyDate, "seconds"));

    var jsDate = new Date(2006, 11, 10, 12, 33, 30);
    jum.assertEquals(2006, getDateField(jsDate, "year"));
    jum.assertEquals(11, getDateField(jsDate, "month"));
    jum.assertEquals(10, getDateField(jsDate, "date"));
    jum.assertEquals(12, getDateField(jsDate, "hours"));
    jum.assertEquals(33, getDateField(jsDate, "minutes"));
    jum.assertEquals(30, getDateField(jsDate, "seconds"));

    var fullHashDate = { year: 2006,
                         month: 11,
                         date: 10,
                         hours: 12,
                         minutes: 33,
                         seconds: 30};

    jum.assertEquals(2006, getDateField(fullHashDate, "year"));
    jum.assertEquals(11, getDateField(fullHashDate, "month"));
    jum.assertEquals(10, getDateField(fullHashDate, "date"));
    jum.assertEquals(12, getDateField(fullHashDate, "hours"));
    jum.assertEquals(33, getDateField(fullHashDate, "minutes"));
    jum.assertEquals(30, getDateField(fullHashDate, "seconds"));

    var sparseHashDate = { year: 2006,
                           month: 11 };

    jum.assertEquals(2006, getDateField(sparseHashDate, "year"));
    jum.assertEquals(11, getDateField(sparseHashDate, "month"));
    jum.assertEquals(1, getDateField(sparseHashDate, "date"));
    jum.assertEquals(0, getDateField(sparseHashDate, "hours"));
    jum.assertEquals(0, getDateField(sparseHashDate, "minutes"));
    jum.assertEquals(0, getDateField(sparseHashDate, "seconds"));

}

function test_compareDates(){
    var compareDates = dojo.lang.hitch(getNyTz(),cosmo.datetime.timezone.Timezone.prototype._compareDates);
    var jsDate1 = new Date(2006, 11, 10, 12, 33, 30);
    var jsDate2 = new Date(2007, 11, 10, 12, 33, 30);
    jum.assertTrue(compareDates(jsDate1, jsDate2) < 0);

    jsDate1 = new Date(2006, 11, 10, 12, 33, 30);
    jsDate2 = new Date(2006, 11, 10, 12, 33, 30);
    jum.assertTrue(compareDates(jsDate1, jsDate2) == 0);

    jsDate1 = new Date(2006, 11, 10, 12, 33, 31);
    jsDate2 = new Date(2006, 11, 10, 12, 33, 30);
    jum.assertTrue(compareDates(jsDate1, jsDate2)  > 0);

    jsDate1 = new Date(2006, 11, 10, 13, 33, 31);
    jsDate2 = new Date(2006, 11, 10, 12, 33, 31);
    jum.assertTrue(compareDates(jsDate1, jsDate2)  > 0);

    var sparseHashDate = { year: 2006,
                           month: 11 };
    jsDate2 = new Date(2006, 11, 1, 1, 1, 1, 1);
    jum.assertTrue(compareDates(sparseHashDate, jsDate2) < 0);
}

function test_getZoneItemForDate(){
    var tz = getNyTz();
    var date = new Date(2006, 1, 1);
    var zoneItem = tz._getZoneItemForDate(date);
    jum.assertEquals(null, zoneItem.untilDate);

    date = new Date(1966, 11, 31);
    zoneItem = tz._getZoneItemForDate(date);
    jum.assertEquals(1967, zoneItem.untilDate.year);

    date = new Date(1800, 1, 1);
    zoneItem = tz._getZoneItemForDate(date);
    jum.assertEquals(1883, zoneItem.untilDate.year);

    date = new Date(1920, 1, 1);
    zoneItem = tz._getZoneItemForDate(date);
    jum.assertEquals(1942, zoneItem.untilDate.year);
}