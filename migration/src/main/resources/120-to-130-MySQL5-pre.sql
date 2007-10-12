# 120-to-130-MySQL5-pre.sql

# migrate data
update event_stamp set isfloating=true where startdate not like '%T%';