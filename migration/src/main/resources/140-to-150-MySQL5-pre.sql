# 140-to-150-MySQL5-pre.sql

# migrate data
alter table collection_item add column createdate bigint

# initialize createdate
update collection_item set createdate=1