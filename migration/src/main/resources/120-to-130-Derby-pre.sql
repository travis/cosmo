# 120-to-130-Derby-pre.sql

# migrate data
update event_stamp set isfloating=1 where startdate not like '%T%'