# 120-to-130-Derby-pre.sql

# migrate data
update event_stamp set isfloating=1 where startdate not like '%T%'

# remove old data
delete from multistring_values where exists (select id from attribute where id=attributeid and namespace='org.osaf.cosmo.model.CalendarCollectionStamp' and localname='supportedComponentSet')
delete from attribute where namespace='org.osaf.cosmo.model.CalendarCollectionStamp' and localname='supportedComponentSet'