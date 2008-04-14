# 140-to-150-Derby-pre.sql

# migrate data
alter table collection_item add column createdate bigint
alter table tickets add column createdate bigint
alter table tickets add column modifydate bigint
alter table tickets add column etag varchar(255)

# initialize createdate
update collection_item set createdate=1

# initialize new ticket fields
update tickets set createdate=1
update tickets set modifydate=1
update tickets set etag=''
