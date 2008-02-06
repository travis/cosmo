# 120-to-130-MySQL5-pre.sql

# migrate data
update event_stamp set isfloating=true where startdate not like '%T%';

# remove old data
delete from multistring_values where exists (select id from attribute where id=attributeid and namespace='org.osaf.cosmo.model.CalendarCollectionStamp' and localname='supportedComponentSet');
delete from attribute where namespace='org.osaf.cosmo.model.CalendarCollectionStamp' and localname='supportedComponentSet';
