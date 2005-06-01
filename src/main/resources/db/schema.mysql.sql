--    Copyright 2005 Open Source Applications Foundation

--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at

--        http://www.apache.org/licenses/LICENSE-2.0

--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.

DROP TABLE IF EXISTS userrole;
CREATE TABLE userrole (
  roleid INT NOT NULL,
  userid INT NOT NULL,
  PRIMARY KEY (roleid, userid)
);

INSERT INTO userrole VALUES (1, 1);

DROP TABLE IF EXISTS role;
CREATE TABLE role (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(32) NOT NULL,
  dateCreated DATETIME NOT NULL,
  dateModified DATETIME NOT NULL,
  CONSTRAINT name UNIQUE (name)
);

INSERT INTO role VALUES (1, 'root', NOW(), NOW());
INSERT INTO role VALUES (2, 'user', NOW(), NOW());

DROP TABLE IF EXISTS user;
CREATE TABLE user (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(32) NOT NULL,
  password VARCHAR(32) NOT NULL,
  email VARCHAR(32) NOT NULL,
  dateCreated DATETIME NOT NULL,
  dateModified DATETIME NOT NULL,
  CONSTRAINT username UNIQUE (username),
  CONSTRAINT email UNIQUE (email)
);

INSERT INTO user VALUES (1, 'root', '32a8bd4d676f4fef0920c7da8db2bad7', 'root@localhost', NOW(), NOW());

--
-- add foreign key constraints at the end so we can drop and re-add
-- existing tables
--
ALTER TABLE userrole ADD FOREIGN KEY (roleid) REFERENCES role (id);
ALTER TABLE userrole ADD FOREIGN KEY (userid) REFERENCES user (id);
