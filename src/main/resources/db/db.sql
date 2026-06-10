-- CREATE SCHEMA IF NOT EXISTS DEV;
-- SET SCHEMA DEV;

DROP TABLE IF EXISTS USERS;
CREATE TABLE USERS(
  ID                      VARCHAR(50)  NOT NULL,
  CONSTRAINT PK_USERS     PRIMARY KEY (ID),
  ACCOUNT_NAME            VARCHAR(255) NOT NULL,
  CONSTRAINT UK_USERS_ACCOUNT_NAME UNIQUE (ACCOUNT_NAME),
  PASSWORD_HASH           VARCHAR(255) NOT NULL,
  ROLE                    VARCHAR(50) NOT NULL,
  INDEX IX_USERS_ROLE (ROLE),
  REFRESH_TOKEN           TEXT DEFAULT NULL,
  CREATED_BY              VARCHAR(255) NOT NULL,
  CREATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_BY              VARCHAR(255) NOT NULL,
  UPDATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO USERS(ID,ACCOUNT_NAME,PASSWORD_HASH,ROLE,REFRESH_TOKEN,CREATED_BY,CREATED_AT,UPDATED_BY,UPDATED_AT) VALUES
('US000001', 'username1', '$2b$12$9MvpqVYIQDfm7yO6te/2..xvcvEbsY/hsXPtZPMg2Kfjn7HT6O6zm', 'ADMIN',  null, 'system', '2025-01-01 09:00:00',  'user','2025-02-01 09:00:00'),
('US000002', 'username2', '$2b$12$4qwE4vJlBDbpM/CQK5hug.FbpEZX6W028lW/9dv7p02fbLyrMypEC', 'ADMIN',  null,   'user', '2025-01-03 10:00:00','system','2025-02-03 10:00:00'),
('US000003', 'username3', '$2b$12$e8bQib9HafDZZLsiiTaGqO4D0zlIMhO.9AbNpv1POEQGBoPYmy1Sm', 'ADMIN',  null, 'system', '2025-02-02 11:00:00',  'user','2025-03-02 11:00:00'),
('US000004', 'username4', '$2b$12$PvBfZqEkIITnLVqN5w7zhOcHar30Zh7WVIzPPOxO1K7JLTHukDgTq', 'MEMBER',  null,   'user', '2025-01-02 12:00:00','system','2025-02-02 12:00:00'),
('US000005', 'username5', '$2b$12$P8PhCv6StfjcQJ8TJcfbG.Y7eofm54yEl6.tRM9VdKkzy7Mm77Jmm', 'MEMBER',  null,   'user', '2025-02-03 13:00:00','system','2025-03-03 13:00:00'),
('US000006', 'username6', '$2b$12$L9p/Zhq13BnCE8hCpFN5JurzrGSjvaI3Rgj1nEgNJ862c2j2jCmka', 'MEMBER' , null, 'system', '2025-02-01 14:00:00',  'user','2025-03-01 14:00:00');

--
DROP TABLE IF EXISTS USERS_AUDIT;
CREATE TABLE USERS_AUDIT(
  AUDIT_ID                BIGINT AUTO_INCREMENT,
  CONSTRAINT PK_USERS_AUDIT PRIMARY KEY (AUDIT_ID),
  ACTION                  VARCHAR(1) NOT NULL,
  ID                      VARCHAR(50)  NOT NULL,
  INDEX IX_USERS_AUDIT_ID (ID),
  ACCOUNT_NAME            VARCHAR(255) NOT NULL,
  PASSWORD_HASH           VARCHAR(255) NOT NULL,
  ROLE                    VARCHAR(50) NOT NULL,
  REFRESH_TOKEN           TEXT DEFAULT NULL,
  CREATED_BY              VARCHAR(255) NOT NULL,
  CREATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_BY              VARCHAR(255) NOT NULL,
  UPDATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO USERS_AUDIT(ACTION,ID,ACCOUNT_NAME,PASSWORD_HASH,ROLE,REFRESH_TOKEN,CREATED_BY,CREATED_AT,UPDATED_BY,UPDATED_AT) VALUES
('A','US000001', 'username1', '$2b$12$9MvpqVYIQDfm7yO6te/2..xvcvEbsY/hsXPtZPMg2Kfjn7HT6O6zm', 'ADMIN',  null, 'system', '2025-01-01 09:00:00',  'user','2025-02-01 09:00:00'),
('A','US000002', 'username2', '$2b$12$4qwE4vJlBDbpM/CQK5hug.FbpEZX6W028lW/9dv7p02fbLyrMypEC', 'ADMIN',  null,   'user', '2025-01-03 10:00:00','system','2025-02-03 10:00:00'),
('A','US000003', 'username3', '$2b$12$e8bQib9HafDZZLsiiTaGqO4D0zlIMhO.9AbNpv1POEQGBoPYmy1Sm', 'ADMIN',  null, 'system', '2025-02-02 11:00:00',  'user','2025-03-02 11:00:00'),
('A','US000004', 'username4', '$2b$12$PvBfZqEkIITnLVqN5w7zhOcHar30Zh7WVIzPPOxO1K7JLTHukDgTq', 'MEMBER',  null,   'user', '2025-01-02 12:00:00','system','2025-02-02 12:00:00'),
('A','US000005', 'username5', '$2b$12$P8PhCv6StfjcQJ8TJcfbG.Y7eofm54yEl6.tRM9VdKkzy7Mm77Jmm', 'MEMBER',  null,   'user', '2025-02-03 13:00:00','system','2025-03-03 13:00:00'),
('A','US000006', 'username6', '$2b$12$L9p/Zhq13BnCE8hCpFN5JurzrGSjvaI3Rgj1nEgNJ862c2j2jCmka', 'MEMBER' , null, 'system', '2025-02-01 14:00:00',  'user','2025-03-01 14:00:00');

--

DROP TABLE IF EXISTS STUDENTS;
CREATE TABLE STUDENTS(
  ID                      VARCHAR(50) NOT NULL,
  CONSTRAINT PK_DOCUMENT PRIMARY KEY (ID),
  NAME                    VARCHAR(200) NOT NULL,
  AGE                     INT,
  HEIGHT                  DECIMAL(5,2),
  BIRTHDAY                DATE,
  CREATED_BY              VARCHAR(200) NOT NULL,
  INDEX IX_STUDENTS_CREATED_BY (CREATED_BY),
  CREATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_BY              VARCHAR(200) NOT NULL,
  UPDATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO STUDENTS(ID,NAME,AGE,BIRTHDAY,HEIGHT,CREATED_BY,CREATED_AT,UPDATED_BY,UPDATED_AT) VALUES
-- for crud test
('ST000001','Name 1', 18, '2002-02-03', 168.5,   'system', '2025-01-01 09:00:00',       'user','2025-02-01 09:00:00'),
('ST000002','Name 2', 19, '2002-03-15', 171.5,     'user', '2025-01-03 10:00:00',     'system','2025-02-03 10:00:00'),
('ST000003','Name 3', 20, '2002-06-09', 174.8,   'system', '2025-02-02 11:00:00',       'user','2025-03-02 11:00:00'),
('ST000004','Name 4', 21, '2002-10-10', 180.2,     'user', '2025-01-02 12:00:00',     'system','2025-02-02 12:00:00'),
('ST000005','Name 5', 22, '2002-01-28', 169.7,     'user', '2025-02-03 13:00:00',     'system','2025-03-03 13:00:00'),
('ST000006','Name 6', 23, '2002-09-13', 178.3,   'system', '2025-02-01 14:00:00',       'user','2025-03-01 14:00:00'),
-- for biz logic test
('ST000007','Name 7', 25, '2002-09-13', 177,    'US000002', '2025-02-01 14:00:00',  'US000002','2025-03-01 14:00:00');


DROP TABLE IF EXISTS STUDENTS_AUDIT;
CREATE TABLE STUDENTS_AUDIT(
  AUDIT_ID                BIGINT AUTO_INCREMENT,
  CONSTRAINT PK_STUDENTS_AUDIT PRIMARY KEY (AUDIT_ID),
  ACTION                  VARCHAR(1) NOT NULL,
  ID                      VARCHAR(50) NOT NULL,
  INDEX IX_STUDENTS_AUDIT_ID (ID),
  NAME                    VARCHAR(200) NOT NULL,
  AGE                     INT NULL,
  HEIGHT                  DECIMAL(5,2),
  BIRTHDAY                DATE,
  CREATED_BY              VARCHAR(200) NOT NULL,
  CREATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_BY              VARCHAR(200) NOT NULL,
  UPDATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO STUDENTS_AUDIT(ACTION,ID,NAME,AGE,BIRTHDAY,HEIGHT,CREATED_BY,CREATED_AT,UPDATED_BY,UPDATED_AT) VALUES
('A', 'ST000001','Name 1', 18, '2002-02-03', 168.5,  'system', '2025-01-01 09:00:00',       'user','2025-02-01 09:00:00'),
('A', 'ST000002','Name 2', 19, '2002-03-15', 171.5,    'user', '2025-01-03 10:00:00',     'system','2025-02-03 10:00:00'),
('A', 'ST000003','Name 3', 20, '2002-06-09', 174.8,  'system', '2025-02-02 11:00:00',       'user','2025-03-02 11:00:00'),
('A', 'ST000004','Name 4', 21, '2002-10-10', 180.2,    'user', '2025-01-02 12:00:00',     'system','2025-02-02 12:00:00'),
('A', 'ST000005','Name 5', 22, '2002-01-28', 169.7,    'user', '2025-02-03 13:00:00',     'system','2025-03-03 13:00:00'),
('A', 'ST000006','Name 6', 23, '2002-09-13', 178.3,  'system', '2025-02-01 14:00:00',       'user','2025-03-01 14:00:00'),
-- for biz logic test
('A', 'ST000007','Name 7', 25, '2002-09-13', 177,    'US000002', '2025-02-01 14:00:00',  'US000002','2025-03-01 14:00:00');

--
DROP TABLE IF EXISTS ROLES;
CREATE TABLE ROLES(
  ID                      VARCHAR(50) NOT NULL,
  CONSTRAINT PK_ROLES PRIMARY KEY (ID),
  AUTHORITY               TEXT DEFAULT NULL,
  CREATED_BY              VARCHAR(200) NOT NULL,
  CREATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_BY              VARCHAR(200) NOT NULL,
  UPDATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Make sure to create unique value for each column for filter testing
INSERT INTO ROLES(ID,AUTHORITY,CREATED_BY,CREATED_AT,UPDATED_BY,UPDATED_AT) VALUES
('RO000001', 'target:get,target2:update', 'system', '2025-01-01 09:00:00',  'user','2025-02-01 09:00:00'),
('RO000002', 'target:get,target2:update',   'user', '2025-01-03 10:00:00','system','2025-02-03 10:00:00'),
('RO000003', 'target:get,target2:update', 'system', '2025-02-02 11:00:00',  'user','2025-03-02 11:00:00'),
('RO000004', 'target:get,target2:update',   'user', '2025-01-02 12:00:00','system','2025-02-02 12:00:00'),
('RO000005', 'target:get,target2:update',   'user', '2025-02-03 13:00:00','system','2025-03-03 13:00:00'),
('RO000006', 'target:get,target2:update', 'system', '2025-02-01 14:00:00',  'user','2025-03-01 14:00:00'),
--
('ADMIN', '*:*',                                                                                  'system', '2025-02-01 14:00:00',  'user','2025-03-01 14:00:00'),
('MEMBER','user:refreshToken,student:search,student:get,studentAudit:search,studentAudit:get',    'system', '2025-02-01 14:00:00',  'user','2025-03-01 14:00:00'),
('GUEST', 'student:search,student:get,user:login',                                                'system', '2025-02-01 14:00:00',  'user','2025-03-01 14:00:00');

DROP TABLE IF EXISTS ROLES_AUDIT;
CREATE TABLE ROLES_AUDIT(
  AUDIT_ID                BIGINT AUTO_INCREMENT,
  CONSTRAINT PK_ROLES_AUDIT PRIMARY KEY (AUDIT_ID),
  ACTION                  VARCHAR(1) NOT NULL,
  ID                      VARCHAR(50) NOT NULL,
  INDEX IX_ROLES_AUDIT_ID (ID),
  AUTHORITY               TEXT DEFAULT NULL,
  CREATED_BY              VARCHAR(200) NOT NULL,
  CREATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_BY              VARCHAR(200) NOT NULL,
  UPDATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Make sure to create unique value for each column for filter testing
INSERT INTO ROLES_AUDIT(ACTION,ID,AUTHORITY,CREATED_BY,CREATED_AT,UPDATED_BY,UPDATED_AT) VALUES
  ('A','RO000001', 'target:get,target2:update', 'system', '2025-01-01 09:00:00',  'user','2025-02-01 09:00:00'),
  ('A','RO000002', 'target:get,target2:update',   'user', '2025-01-03 10:00:00','system','2025-02-03 10:00:00'),
  ('A','RO000003', 'target:get,target2:update', 'system', '2025-02-02 11:00:00',  'user','2025-03-02 11:00:00'),
  ('A','RO000004', 'target:get,target2:update',   'user', '2025-01-02 12:00:00','system','2025-02-02 12:00:00'),
  ('A','RO000005', 'target:get,target2:update',   'user', '2025-02-03 13:00:00','system','2025-03-03 13:00:00'),
  ('A','RO000006', 'target:get,target2:update', 'system', '2025-02-01 14:00:00',  'user','2025-03-01 14:00:00'),
  --
  ('A','ADMIN', '*:*',                                                                        'system', '2025-02-01 14:00:00',  'user','2025-03-01 14:00:00'),
  ('A','MEMBER', 'user:refreshToken,student:search,student:get,studentAudit:search,studentAudit:get', 'system', '2025-02-01 14:00:00',  'user','2025-03-01 14:00:00'),
  ('A','GUEST', 'student:search,student:get,user:login',                                                       'system', '2025-02-01 14:00:00',  'user','2025-03-01 14:00:00');
