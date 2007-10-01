# 110-to-120-Derby-pre.sql

# alter existing tables
# subscription
alter table subscription add column etag varchar(255)

# users
alter table users add column etag varchar(255)

# user_preferences
alter table user_preferences add column etag varchar(255)

#item
alter table item add column etag varchar(255)

#stamp
alter table stamp add column etag varchar(255)

#attribute
alter table attribute add column etag varchar(255)

# migrate data
update attribute set etag='';
update stamp set etag='';

update item set icaluid=null where modifiesitemid is not null